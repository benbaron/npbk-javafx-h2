package org.example.npbk.repo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.example.npbk.db.Database;
import org.example.npbk.model.SummarySettingsViewModel;

public class SummaryRepository {
    private final Database database;
    public SummaryRepository(Database database) { this.database = database; }

    public SummarySettingsViewModel load() {
        String sql = "SELECT * FROM summary_settings WHERE id=1";
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return map(rs);
            SummarySettingsViewModel model = new SummarySettingsViewModel();
            model.setId(1L); model.setDirty(false); return model;
        } catch (SQLException ex) { throw new IllegalStateException("Could not load summary settings", ex); }
    }

    public void save(SummarySettingsViewModel m) {
        String sql = """
            MERGE INTO summary_settings
            (id, organization_name, branch_name, kingdom_name, period_start, period_end, prepared_by, notes, updated_at)
            KEY(id) VALUES (1, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getOrganizationName()); ps.setString(2, m.getBranchName()); ps.setString(3, m.getKingdomName());
            ps.setDate(4, SqlSupport.sqlDate(m.getPeriodStart())); ps.setDate(5, SqlSupport.sqlDate(m.getPeriodEnd()));
            ps.setString(6, m.getPreparedBy()); ps.setString(7, m.getNotes()); ps.executeUpdate();
            m.setId(1L); m.setDirty(false);
        } catch (SQLException ex) { throw new IllegalStateException("Could not save summary settings", ex); }
    }

    private SummarySettingsViewModel map(ResultSet rs) throws SQLException {
        SummarySettingsViewModel m = new SummarySettingsViewModel();
        m.setId(rs.getLong("id")); m.setOrganizationName(rs.getString("organization_name")); m.setBranchName(rs.getString("branch_name"));
        m.setKingdomName(rs.getString("kingdom_name")); m.setPeriodStart(SqlSupport.localDate(rs, "period_start")); m.setPeriodEnd(SqlSupport.localDate(rs, "period_end"));
        m.setPreparedBy(rs.getString("prepared_by")); m.setNotes(rs.getString("notes")); m.setDirty(false); return m;
    }
}
