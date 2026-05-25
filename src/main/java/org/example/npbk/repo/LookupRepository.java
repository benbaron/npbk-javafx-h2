package org.example.npbk.repo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.npbk.db.Database;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LookupRepository {
    private final Database database;
    public LookupRepository(Database database) { this.database = database; }

    public ObservableList<String> findActiveDisplayNames(String type) {
        ObservableList<String> values = FXCollections.observableArrayList();
        String sql = """
            SELECT display_name FROM lookup_values
            WHERE lookup_type = ? AND active = TRUE
            ORDER BY sort_order, display_name
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) values.add(rs.getString(1));
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load lookup values for " + type, ex);
        }
        return values;
    }

    public ObservableList<String> bankAccounts() { return findActiveDisplayNames("BANK_ACCOUNT"); }
    public ObservableList<String> funds() { return findActiveDisplayNames("FUND"); }
    public ObservableList<String> budgetCategories() { return findActiveDisplayNames("BUDGET_CATEGORY"); }
    public ObservableList<String> itemTypes() { return findActiveDisplayNames("ITEM_TYPE"); }
}
