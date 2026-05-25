package org.example.npbk.repo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.npbk.db.Database;
import org.example.npbk.model.BankTiming;
import org.example.npbk.model.BudgetTiming;
import org.example.npbk.model.LedgerRowViewModel;
import org.example.npbk.model.ValidationState;

/**
 * Repository bridge between the spreadsheet-like LedgerRowViewModel and the
 * normalized accounting schema.  The UI still edits a familiar ledger row, but
 * persistence is now transaction header + balanced transaction lines.
 */
public class TransactionRepository {
    private static final String CASH_ACCOUNT_NAME = "Undep. & non-interest cash";
    private static final String SAVINGS_ACCOUNT_NAME = "Cash Earning Interest";
    private static final String DEFAULT_INCOME_ACCOUNT_NAME = "Direct Contributions/Donations";
    private static final String DEFAULT_EXPENSE_ACCOUNT_NAME = "21b Occupancy - Activity Rel";

    private final Database database;

    public TransactionRepository(Database database) {
        this.database = database;
    }

    public List<LedgerRowViewModel> findAllLedgerRows() {
        Map<Long, LedgerAccumulator> byTransaction = new LinkedHashMap<>();
        String sql = """
            SELECT
                t.id,
                t.transaction_date,
                t.reference_number,
                CAST(t.description AS VARCHAR(4096)) AS details,
                t.affects_bank,
                t.affects_budget,
                p.legal_name,
                ba.name AS bank_account_name,
                a.account_type,
                f.name AS fund_name,
                bc.name AS budget_category_name,
                l.debit_amount,
                l.credit_amount,
                l.line_order
            FROM transactions t
            LEFT JOIN people p ON p.id = t.person_id
            LEFT JOIN bank_accounts ba ON ba.id = t.bank_account_id
            LEFT JOIN transaction_lines l ON l.transaction_id = t.id
            LEFT JOIN accounts a ON a.id = l.account_id
            LEFT JOIN funds f ON f.id = l.fund_id
            LEFT JOIN budget_categories bc ON bc.id = l.budget_category_id
            ORDER BY t.transaction_date NULLS LAST, t.id, l.line_order
            """;
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                LedgerAccumulator acc = byTransaction.computeIfAbsent(id, k -> mapHeader(rs));
                acc.acceptLine(rs);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load transactions", ex);
        }
        List<LedgerRowViewModel> rows = new ArrayList<>();
        for (LedgerAccumulator acc : byTransaction.values()) {
            rows.add(acc.finish());
        }
        return rows;
    }

    public LedgerRowViewModel save(LedgerRowViewModel row) {
        try (var conn = database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (row.getId() == null) {
                    insert(conn, row);
                } else {
                    update(conn, row);
                }
                replaceLines(conn, row);
                conn.commit();
                row.setDirty(false);
                return row;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not save transaction", ex);
        }
    }

    public BigDecimal sumBankEffect() {
        return sum("""
            SELECT COALESCE(SUM(
                CASE WHEN t.affects_bank = 'NOW' AND a.account_type = 'ASSET'
                     THEN l.debit_amount - l.credit_amount
                     ELSE 0 END), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            """);
    }

    public BigDecimal sumBudgetEffect() {
        return sum("""
            SELECT COALESCE(SUM(
                CASE WHEN t.affects_budget = 'NOW' AND a.account_type IN ('INCOME', 'EXPENSE')
                     THEN l.credit_amount - l.debit_amount
                     ELSE 0 END), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            """);
    }

    public BigDecimal sumBudgetEffect(String fund, String budgetCategory) {
        String sql = """
            SELECT COALESCE(SUM(
                CASE WHEN t.affects_budget = 'NOW' AND a.account_type IN ('INCOME', 'EXPENSE')
                     THEN l.credit_amount - l.debit_amount
                     ELSE 0 END), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            LEFT JOIN funds f ON f.id = l.fund_id
            LEFT JOIN budget_categories bc ON bc.id = l.budget_category_id
            WHERE f.name = ? AND bc.name = ?
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fund);
            ps.setString(2, budgetCategory);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not sum budget effect", ex);
        }
    }

    public long countErrors() {
        // Validation state is currently a view-model concern.  Persisted validation
        // issues will move to a dedicated validation/audit table in a later slice.
        return 0L;
    }

    private BigDecimal sum(String sql) {
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not calculate transaction sum", ex);
        }
    }

    private void insert(java.sql.Connection conn, LedgerRowViewModel row) throws SQLException {
        String sql = """
            INSERT INTO transactions
            (transaction_date, reference_number, description, person_id, bank_account_id, affects_bank, affects_budget, workbook_source_sheet)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'TransactionEditor')
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindHeader(conn, row, ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    row.setId(keys.getLong(1));
                }
            }
        }
    }

    private void update(java.sql.Connection conn, LedgerRowViewModel row) throws SQLException {
        String sql = """
            UPDATE transactions SET
                transaction_date = ?,
                reference_number = ?,
                description = ?,
                person_id = ?,
                bank_account_id = ?,
                affects_bank = ?,
                affects_budget = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bindHeader(conn, row, ps);
            ps.setLong(8, row.getId());
            ps.executeUpdate();
        }
    }

    private void bindHeader(java.sql.Connection conn, LedgerRowViewModel row, PreparedStatement ps) throws SQLException {
        if (row.getTransactionDate() == null) {
            ps.setDate(1, null);
        } else {
            ps.setDate(1, Date.valueOf(row.getTransactionDate()));
        }
        ps.setString(2, blankToNull(row.getReferenceNumber()));
        ps.setString(3, blankToNull(row.getDetails()));
        ps.setObject(4, findOrCreatePerson(conn, row.getLegalName()));
        ps.setObject(5, findOrCreateBankAccount(conn, row.getBankAccount()));
        ps.setString(6, row.getAffectsBank().name());
        ps.setString(7, row.getAffectsBudget().name());
    }

    private void replaceLines(java.sql.Connection conn, LedgerRowViewModel row) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM transaction_lines WHERE transaction_id = ?")) {
            ps.setLong(1, row.getId());
            ps.executeUpdate();
        }

        BigDecimal amount = row.getAmount() == null ? BigDecimal.ZERO : row.getAmount();
        BigDecimal abs = amount.abs();
        if (abs.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        Long fundId = findOrCreateFund(conn, row.getFund());
        Long budgetCategoryId = findOrCreateBudgetCategory(conn, row.getBudgetCategory());
        Long cashAccountId = findOrCreateAccount(conn, cashAccountName(row.getBankAccount()), "ASSET", "DEBIT", "BalanceStmt");

        boolean moneyIn = amount.compareTo(BigDecimal.ZERO) >= 0;
        Long operatingAccountId = moneyIn
                ? findOrCreateAccount(conn, DEFAULT_INCOME_ACCOUNT_NAME, "INCOME", "CREDIT", "IncomeStmt")
                : findOrCreateAccount(conn, DEFAULT_EXPENSE_ACCOUNT_NAME, "EXPENSE", "DEBIT", "IncomeStmt");

        if (moneyIn) {
            insertLine(conn, row.getId(), cashAccountId, fundId, null, abs, BigDecimal.ZERO, "Bank/cash side", 10);
            insertLine(conn, row.getId(), operatingAccountId, fundId, budgetCategoryId, BigDecimal.ZERO, abs, "Budget/report side", 20);
        } else {
            insertLine(conn, row.getId(), operatingAccountId, fundId, budgetCategoryId, abs, BigDecimal.ZERO, "Budget/report side", 10);
            insertLine(conn, row.getId(), cashAccountId, fundId, null, BigDecimal.ZERO, abs, "Bank/cash side", 20);
        }
    }

    private void insertLine(java.sql.Connection conn, Long transactionId, Long accountId, Long fundId, Long budgetCategoryId,
                            BigDecimal debit, BigDecimal credit, String memo, int order) throws SQLException {
        String sql = """
            INSERT INTO transaction_lines
            (transaction_id, account_id, fund_id, budget_category_id, debit_amount, credit_amount, memo, line_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, transactionId);
            ps.setLong(2, accountId);
            if (fundId == null) ps.setNull(3, java.sql.Types.BIGINT); else ps.setLong(3, fundId);
            if (budgetCategoryId == null) ps.setNull(4, java.sql.Types.BIGINT); else ps.setLong(4, budgetCategoryId);
            ps.setBigDecimal(5, debit);
            ps.setBigDecimal(6, credit);
            ps.setString(7, memo);
            ps.setInt(8, order);
            ps.executeUpdate();
        }
    }

    private Long findOrCreatePerson(java.sql.Connection conn, String legalName) throws SQLException {
        String name = blankToNull(legalName);
        if (name == null) {
            return null;
        }
        Long existing = findId(conn, "people", "legal_name", name);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO people (legal_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private Long findOrCreateBankAccount(java.sql.Connection conn, String name) throws SQLException {
        return findOrCreateSimpleLookup(conn, "bank_accounts", "name", defaultIfBlank(name, "Checking"));
    }

    private Long findOrCreateFund(java.sql.Connection conn, String name) throws SQLException {
        return findOrCreateSimpleLookup(conn, "funds", "name", defaultIfBlank(name, "General Fund"));
    }

    private Long findOrCreateBudgetCategory(java.sql.Connection conn, String name) throws SQLException {
        String value = blankToNull(name);
        if (value == null) {
            return null;
        }
        Long existing = findId(conn, "budget_categories", "name", value);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO budget_categories (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, value);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private Long findOrCreateAccount(java.sql.Connection conn, String name, String type, String normalBalance, String reportSection) throws SQLException {
        Long existing = findId(conn, "accounts", "name", name);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement ps = conn.prepareStatement("""
                INSERT INTO accounts (name, account_type, normal_balance, report_section)
                VALUES (?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.setString(3, normalBalance);
            ps.setString(4, reportSection);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private Long findOrCreateSimpleLookup(java.sql.Connection conn, String table, String column, String value) throws SQLException {
        Long existing = findId(conn, table, column, value);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO " + table + " (" + column + ") VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, value);
            ps.executeUpdate();
            return generatedId(ps);
        }
    }

    private Long findId(java.sql.Connection conn, String table, String column, String value) throws SQLException {
        String sql = "SELECT id FROM " + table + " WHERE " + column + " = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : null;
            }
        }
    }

    private Long generatedId(PreparedStatement ps) throws SQLException {
        try (ResultSet keys = ps.getGeneratedKeys()) {
            if (!keys.next()) {
                throw new SQLException("Insert did not return a generated id.");
            }
            return keys.getLong(1);
        }
    }

    private LedgerAccumulator mapHeader(ResultSet rs) {
        try {
            LedgerRowViewModel row = new LedgerRowViewModel();
            row.setId(rs.getLong("id"));
            Date d = rs.getDate("transaction_date");
            row.setTransactionDate(d == null ? null : d.toLocalDate());
            row.setReferenceNumber(rs.getString("reference_number"));
            row.setLegalName(rs.getString("legal_name"));
            row.setDetails(rs.getString("details"));
            row.setBankAccount(defaultIfBlank(rs.getString("bank_account_name"), "Checking"));
            row.setAffectsBank(enumOrDefault(rs.getString("affects_bank"), BankTiming.NOW));
            row.setAffectsBudget(enumOrDefault(rs.getString("affects_budget"), BudgetTiming.NOW));
            return new LedgerAccumulator(row);
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not map transaction row", ex);
        }
    }

    private String cashAccountName(String bankAccount) {
        return "Savings".equalsIgnoreCase(blankToNull(bankAccount)) ? SAVINGS_ACCOUNT_NAME : CASH_ACCOUNT_NAME;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String defaultIfBlank(String value, String defaultValue) {
        String trimmed = blankToNull(value);
        return trimmed == null ? defaultValue : trimmed;
    }

    private <E extends Enum<E>> E enumOrDefault(String value, E defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        @SuppressWarnings("unchecked")
        Class<E> enumType = (Class<E>) defaultValue.getDeclaringClass();
        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException ex) {
            return defaultValue;
        }
    }

    private static final class LedgerAccumulator {
        private final LedgerRowViewModel row;
        private BigDecimal displayAmount = BigDecimal.ZERO;
        private boolean sawOperatingLine;

        private LedgerAccumulator(LedgerRowViewModel row) {
            this.row = row;
        }

        void acceptLine(ResultSet rs) throws SQLException {
            String accountType = rs.getString("account_type");
            if (accountType == null) {
                return;
            }
            String fund = rs.getString("fund_name");
            if ((row.getFund() == null || row.getFund().isBlank()) && fund != null) {
                row.setFund(fund);
            }
            String budget = rs.getString("budget_category_name");
            if ((row.getBudgetCategory() == null || row.getBudgetCategory().isBlank()) && budget != null) {
                row.setBudgetCategory(budget);
            }
            BigDecimal debit = rs.getBigDecimal("debit_amount");
            BigDecimal credit = rs.getBigDecimal("credit_amount");
            debit = debit == null ? BigDecimal.ZERO : debit;
            credit = credit == null ? BigDecimal.ZERO : credit;

            if ("INCOME".equals(accountType) || "EXPENSE".equals(accountType)) {
                displayAmount = displayAmount.add(credit.subtract(debit));
                sawOperatingLine = true;
            } else if (!sawOperatingLine && "ASSET".equals(accountType)) {
                displayAmount = displayAmount.add(debit.subtract(credit));
            }
        }

        LedgerRowViewModel finish() {
            row.setAmount(displayAmount);
            row.setBankEffect(row.getAffectsBank() == BankTiming.NOW ? displayAmount : BigDecimal.ZERO);
            row.setBudgetEffect(row.getAffectsBudget() == BudgetTiming.NOW ? displayAmount : BigDecimal.ZERO);
            row.setValidationState(ValidationState.OK);
            row.setDirty(false);
            return row;
        }
    }
}
