package org.example.npbk.repo;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.db.Database;
import org.example.npbk.model.LookupOption;

/** Loads editable accounting choices while preserving their database IDs. */
public class AccountingLookupRepository
{
    private final Database database;

    public AccountingLookupRepository(Database database)
    {
        this.database = database;
    }

    public List<LookupOption> accounts()
    {
        String sql = "SELECT id, code, name FROM accounts WHERE active = TRUE ORDER BY sort_order, code, name";
        List<LookupOption> values = new ArrayList<>();
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            while (rs.next())
            {
                String code = rs.getString("code");
                String name = rs.getString("name");
                String display = code == null || code.isBlank() ? name : code + " — " + name;
                values.add(new LookupOption(rs.getLong("id"), display));
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not load chart of accounts", ex);
        }
        return values;
    }

    public List<LookupOption> funds()
    {
        return namedOptions("funds");
    }

    public List<LookupOption> budgetCategories()
    {
        return namedOptions("budget_categories");
    }

    public List<LookupOption> bankAccounts()
    {
        return namedOptions("bank_accounts");
    }

    private List<LookupOption> namedOptions(String tableName)
    {
        String safe = safeIdentifier(tableName);
        String sql = "SELECT id, name FROM " + safe + " WHERE active = TRUE ORDER BY sort_order, name";
        List<LookupOption> values = new ArrayList<>();
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql))
        {
            while (rs.next())
                values.add(new LookupOption(rs.getLong("id"), rs.getString("name")));
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Could not load lookup options from " + tableName, ex);
        }
        return values;
    }

    private String safeIdentifier(String value)
    {
        if (value == null || !value.matches("[A-Za-z_][A-Za-z0-9_]*"))
            throw new IllegalArgumentException("Unsafe SQL identifier: " + value);
        return value;
    }
}
