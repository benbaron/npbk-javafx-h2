package org.example.npbk.repo;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.db.Database;
import org.example.npbk.model.*;

public class TransactionRepository {
    private final Database database;
    public TransactionRepository(Database database) { this.database = database; }

    public List<LedgerRowViewModel> findAllLedgerRows() {
        List<LedgerRowViewModel> rows = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY transaction_date NULLS LAST, id";
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(map(rs));
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load transactions", ex);
        }
        return rows;
    }

    public LedgerRowViewModel save(LedgerRowViewModel row) {
        if (row.getId() == null) return insert(row);
        update(row);
        row.setDirty(false);
        return row;
    }


    public java.math.BigDecimal sumBankEffect() {
        return sum("SELECT COALESCE(SUM(bank_effect),0) FROM transactions");
    }

    public java.math.BigDecimal sumBudgetEffect() {
        return sum("SELECT COALESCE(SUM(budget_effect),0) FROM transactions");
    }

    public java.math.BigDecimal sumBudgetEffect(String fund, String budgetCategory) {
        String sql = "SELECT COALESCE(SUM(budget_effect),0) FROM transactions WHERE fund=? AND budget_category=?";
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fund);
            ps.setString(2, budgetCategory);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO; }
        } catch (SQLException ex) { throw new IllegalStateException("Could not sum budget effect", ex); }
    }

    public long countErrors() {
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM transactions WHERE validation_state='ERROR'")) {
            return rs.next() ? rs.getLong(1) : 0;
        } catch (SQLException ex) { throw new IllegalStateException("Could not count ledger errors", ex); }
    }

    private java.math.BigDecimal sum(String sql) {
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getBigDecimal(1) : java.math.BigDecimal.ZERO;
        } catch (SQLException ex) { throw new IllegalStateException("Could not calculate transaction sum", ex); }
    }

    private LedgerRowViewModel insert(LedgerRowViewModel row) {
        String sql = """
            INSERT INTO transactions
            (transaction_date, reference_number, legal_name, details, bank_account, affects_bank,
             budget_category, affects_budget, fund, amount, bank_effect, budget_effect, validation_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(row, ps);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) row.setId(keys.getLong(1));
            }
            row.setDirty(false);
            return row;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not insert transaction", ex);
        }
    }

    private void update(LedgerRowViewModel row) {
        String sql = """
            UPDATE transactions SET
              transaction_date=?, reference_number=?, legal_name=?, details=?, bank_account=?, affects_bank=?,
              budget_category=?, affects_budget=?, fund=?, amount=?, bank_effect=?, budget_effect=?,
              validation_state=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(row, ps);
            ps.setLong(14, row.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not update transaction", ex);
        }
    }

    private void bind(LedgerRowViewModel row, PreparedStatement ps) throws SQLException {
        if (row.getTransactionDate() == null) ps.setDate(1, null); else ps.setDate(1, Date.valueOf(row.getTransactionDate()));
        ps.setString(2, row.getReferenceNumber());
        ps.setString(3, row.getLegalName());
        ps.setString(4, row.getDetails());
        ps.setString(5, row.getBankAccount());
        ps.setString(6, row.getAffectsBank().name());
        ps.setString(7, row.getBudgetCategory());
        ps.setString(8, row.getAffectsBudget().name());
        ps.setString(9, row.getFund());
        ps.setBigDecimal(10, row.getAmount());
        ps.setBigDecimal(11, row.getBankEffect());
        ps.setBigDecimal(12, row.getBudgetEffect());
        ps.setString(13, row.getValidationState().name());
    }

    private LedgerRowViewModel map(ResultSet rs) throws SQLException {
        LedgerRowViewModel row = new LedgerRowViewModel();
        row.setId(rs.getLong("id"));
        Date d = rs.getDate("transaction_date");
        row.setTransactionDate(d == null ? null : d.toLocalDate());
        row.setReferenceNumber(rs.getString("reference_number"));
        row.setLegalName(rs.getString("legal_name"));
        row.setDetails(rs.getString("details"));
        row.setBankAccount(rs.getString("bank_account"));
        row.setAffectsBank(BankTiming.valueOf(rs.getString("affects_bank")));
        row.setBudgetCategory(rs.getString("budget_category"));
        row.setAffectsBudget(BudgetTiming.valueOf(rs.getString("affects_budget")));
        row.setFund(rs.getString("fund"));
        row.setAmount(rs.getBigDecimal("amount"));
        row.setBankEffect(rs.getBigDecimal("bank_effect"));
        row.setBudgetEffect(rs.getBigDecimal("budget_effect"));
        row.setValidationState(ValidationState.valueOf(rs.getString("validation_state")));
        row.setDirty(false);
        return row;
    }
}
