package org.example.npbk.repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.db.Database;
import org.example.npbk.model.BankTiming;
import org.example.npbk.model.BudgetTiming;
import org.example.npbk.model.LookupOption;
import org.example.npbk.model.TransactionEditorViewModel;
import org.example.npbk.model.TransactionLineViewModel;

/** Persists transaction headers and their balanced transaction_lines. */
public class NormalizedTransactionRepository
{
    private final Database database;

    public NormalizedTransactionRepository(Database database)
    {
        this.database = database;
    }

    public List<LookupOption> findTransactionChoices()
    {
        String sql = """
            SELECT id, transaction_date, reference_number,
                   CAST(description AS VARCHAR(240)) AS description
            FROM transactions
            ORDER BY transaction_date DESC, id DESC
            """;
        List<LookupOption> values = new ArrayList<>();
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            while (rs.next())
            {
                StringBuilder display = new StringBuilder();
                Date date = rs.getDate("transaction_date");
                if (date != null)
                    display.append(date.toLocalDate());
                String reference = rs.getString("reference_number");
                if (reference != null && !reference.isBlank())
                    display.append(" — ").append(reference);
                String description = rs.getString("description");
                if (description != null && !description.isBlank())
                    display.append(" — ").append(description);
                values.add(new LookupOption(rs.getLong("id"), display.toString()));
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not load transactions", ex);
        }
        return values;
    }

    public TransactionEditorViewModel findById(long transactionId)
    {
        TransactionEditorViewModel model = loadHeader(transactionId);
        loadLines(model);
        if (model.getLines().isEmpty())
            model.getLines().addAll(new TransactionLineViewModel(), new TransactionLineViewModel());
        model.markClean();
        return model;
    }

    public TransactionEditorViewModel save(TransactionEditorViewModel model)
    {
        try (Connection conn = database.getConnection())
        {
            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try
            {
                Long personId = ensurePerson(conn, model.getLegalName());
                long transactionId = model.getId() == null
                    ? insertHeader(conn, model, personId)
                    : updateHeader(conn, model, personId);
                replaceLines(conn, transactionId, model.getLines());
                conn.commit();
                conn.setAutoCommit(previousAutoCommit);
                model.setId(transactionId);
                model.markClean();
                return model;
            }
            catch (SQLException | RuntimeException ex)
            {
                conn.rollback();
                conn.setAutoCommit(previousAutoCommit);
                throw ex;
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not save normalized transaction", ex);
        }
    }

    private TransactionEditorViewModel loadHeader(long transactionId)
    {
        String sql = """
            SELECT t.id, t.transaction_date, t.posting_date, t.reference_number,
                   CAST(t.description AS VARCHAR(4096)) AS description,
                   p.legal_name,
                   ba.id AS bank_account_id, ba.name AS bank_account_name,
                   t.affects_bank, t.affects_budget
            FROM transactions t
            LEFT JOIN people p ON p.id = t.person_id
            LEFT JOIN bank_accounts ba ON ba.id = t.bank_account_id
            WHERE t.id = ?
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, transactionId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (!rs.next())
                    throw new IllegalArgumentException("Transaction not found: " + transactionId);
                TransactionEditorViewModel model = new TransactionEditorViewModel();
                model.setId(rs.getLong("id"));
                model.setTransactionDate(localDate(rs, "transaction_date"));
                model.setPostingDate(localDate(rs, "posting_date"));
                model.setReferenceNumber(rs.getString("reference_number"));
                model.setDescription(rs.getString("description"));
                model.setLegalName(rs.getString("legal_name"));
                Long bankId = nullableLong(rs, "bank_account_id");
                model.setBankAccount(bankId == null ? null : new LookupOption(bankId, rs.getString("bank_account_name")));
                model.setAffectsBank(BankTiming.valueOf(rs.getString("affects_bank")));
                model.setAffectsBudget(BudgetTiming.valueOf(rs.getString("affects_budget")));
                return model;
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not load transaction " + transactionId, ex);
        }
    }

    private void loadLines(TransactionEditorViewModel model)
    {
        String sql = """
            SELECT l.id,
                   a.id AS account_id, a.code AS account_code, a.name AS account_name,
                   f.id AS fund_id, f.name AS fund_name,
                   bc.id AS budget_category_id, bc.name AS budget_category_name,
                   l.debit_amount, l.credit_amount,
                   CAST(l.memo AS VARCHAR(4096)) AS memo
            FROM transaction_lines l
            JOIN accounts a ON a.id = l.account_id
            LEFT JOIN funds f ON f.id = l.fund_id
            LEFT JOIN budget_categories bc ON bc.id = l.budget_category_id
            WHERE l.transaction_id = ?
            ORDER BY l.line_order, l.id
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setLong(1, model.getId());
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    TransactionLineViewModel line = new TransactionLineViewModel();
                    line.setId(rs.getLong("id"));
                    String accountCode = rs.getString("account_code");
                    String accountName = rs.getString("account_name");
                    String accountDisplay = accountCode == null || accountCode.isBlank()
                        ? accountName
                        : accountCode + " — " + accountName;
                    line.setAccount(new LookupOption(rs.getLong("account_id"), accountDisplay));
                    Long fundId = nullableLong(rs, "fund_id");
                    line.setFund(fundId == null ? null : new LookupOption(fundId, rs.getString("fund_name")));
                    Long categoryId = nullableLong(rs, "budget_category_id");
                    line.setBudgetCategory(categoryId == null ? null : new LookupOption(categoryId, rs.getString("budget_category_name")));
                    line.setDebitAmount(rs.getBigDecimal("debit_amount"));
                    line.setCreditAmount(rs.getBigDecimal("credit_amount"));
                    line.setMemo(rs.getString("memo"));
                    line.markClean();
                    model.getLines().add(line);
                }
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not load transaction lines for " + model.getId(), ex);
        }
    }

    private long insertHeader(Connection conn, TransactionEditorViewModel model, Long personId) throws SQLException
    {
        String sql = """
            INSERT INTO transactions
                (transaction_date, posting_date, description, reference_number,
                 person_id, bank_account_id, affects_bank, affects_budget)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            bindHeader(ps, model, personId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys())
            {
                if (!keys.next())
                    throw new SQLException("No generated transaction id returned");
                return keys.getLong(1);
            }
        }
    }

    private long updateHeader(Connection conn, TransactionEditorViewModel model, Long personId) throws SQLException
    {
        String sql = """
            UPDATE transactions SET
                transaction_date = ?, posting_date = ?, description = ?, reference_number = ?,
                person_id = ?, bank_account_id = ?, affects_bank = ?, affects_budget = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql))
        {
            bindHeader(ps, model, personId);
            ps.setLong(9, model.getId());
            if (ps.executeUpdate() != 1)
                throw new SQLException("Transaction not found for update: " + model.getId());
            return model.getId();
        }
    }

    private void bindHeader(PreparedStatement ps, TransactionEditorViewModel model, Long personId) throws SQLException
    {
        ps.setDate(1, Date.valueOf(model.getTransactionDate()));
        if (model.getPostingDate() == null)
            ps.setDate(2, null);
        else
            ps.setDate(2, Date.valueOf(model.getPostingDate()));
        ps.setString(3, model.getDescription());
        ps.setString(4, model.getReferenceNumber());
        setNullableLong(ps, 5, personId);
        setNullableLong(ps, 6, model.getBankAccount() == null ? null : model.getBankAccount().id());
        ps.setString(7, model.getAffectsBank().name());
        ps.setString(8, model.getAffectsBudget().name());
    }

    private void replaceLines(Connection conn, long transactionId, List<TransactionLineViewModel> lines) throws SQLException
    {
        try (PreparedStatement delete = conn.prepareStatement("DELETE FROM transaction_lines WHERE transaction_id = ?"))
        {
            delete.setLong(1, transactionId);
            delete.executeUpdate();
        }

        String sql = """
            INSERT INTO transaction_lines
                (transaction_id, account_id, fund_id, budget_category_id,
                 debit_amount, credit_amount, memo, line_order)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement insert = conn.prepareStatement(sql))
        {
            int order = 0;
            for (TransactionLineViewModel line : lines)
            {
                if (!isMeaningful(line))
                    continue;
                insert.setLong(1, transactionId);
                insert.setLong(2, line.getAccount().id());
                setNullableLong(insert, 3, line.getFund() == null ? null : line.getFund().id());
                setNullableLong(insert, 4, line.getBudgetCategory() == null ? null : line.getBudgetCategory().id());
                insert.setBigDecimal(5, line.getDebitAmount());
                insert.setBigDecimal(6, line.getCreditAmount());
                insert.setString(7, line.getMemo());
                insert.setInt(8, order++);
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private Long ensurePerson(Connection conn, String legalName) throws SQLException
    {
        if (legalName == null || legalName.isBlank())
            return null;
        String normalized = legalName.trim();
        try (PreparedStatement select = conn.prepareStatement(
            "SELECT id FROM people WHERE LOWER(legal_name) = LOWER(?) ORDER BY id LIMIT 1"))
        {
            select.setString(1, normalized);
            try (ResultSet rs = select.executeQuery())
            {
                if (rs.next())
                    return rs.getLong(1);
            }
        }
        try (PreparedStatement insert = conn.prepareStatement(
            "INSERT INTO people (legal_name) VALUES (?)", Statement.RETURN_GENERATED_KEYS))
        {
            insert.setString(1, normalized);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys())
            {
                if (!keys.next())
                    throw new SQLException("No generated person id returned");
                return keys.getLong(1);
            }
        }
    }

    private boolean isMeaningful(TransactionLineViewModel line)
    {
        return line.getAccount() != null
            || line.getFund() != null
            || line.getBudgetCategory() != null
            || line.getDebitAmount().compareTo(BigDecimal.ZERO) != 0
            || line.getCreditAmount().compareTo(BigDecimal.ZERO) != 0
            || (line.getMemo() != null && !line.getMemo().isBlank());
    }

    private LocalDate localDate(ResultSet rs, String column) throws SQLException
    {
        Date value = rs.getDate(column);
        return value == null ? null : value.toLocalDate();
    }

    private Long nullableLong(ResultSet rs, String column) throws SQLException
    {
        Object value = rs.getObject(column);
        return value == null ? null : ((Number) value).longValue();
    }

    private void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException
    {
        if (value == null)
            ps.setObject(index, null);
        else
            ps.setLong(index, value);
    }
}
