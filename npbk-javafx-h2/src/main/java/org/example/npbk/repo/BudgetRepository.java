package org.example.npbk.repo;

import java.sql.*;
import java.util.*;
import org.example.npbk.db.Database;
import org.example.npbk.model.*;

public class BudgetRepository {
    private final Database database;
    public BudgetRepository(Database database) { this.database = database; }

    public List<BudgetLineViewModel> findAll() {
        List<BudgetLineViewModel> rows = new ArrayList<>();
        String sql = "SELECT * FROM budget_lines ORDER BY fund, budget_category, id";
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) rows.add(map(rs));
        } catch (SQLException ex) { throw new IllegalStateException("Could not load budget lines", ex); }
        return rows;
    }

    public void save(BudgetLineViewModel row) {
        if (row.getId() == null) insert(row); else update(row); row.setDirty(false);
    }
    public void saveAll(Collection<BudgetLineViewModel> rows) { rows.forEach(this::save); }

    private void insert(BudgetLineViewModel r) {
        String sql = """
            INSERT INTO budget_lines (fund, budget_category, line_type, planned_amount, actual_amount, variance_amount, notes, validation_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, r); ps.executeUpdate(); try (ResultSet keys=ps.getGeneratedKeys()) { if (keys.next()) r.setId(keys.getLong(1)); }
        } catch (SQLException ex) { throw new IllegalStateException("Could not insert budget line", ex); }
    }
    private void update(BudgetLineViewModel r) {
        String sql = """
            UPDATE budget_lines SET fund=?, budget_category=?, line_type=?, planned_amount=?, actual_amount=?, variance_amount=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;
        try (var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) { bind(ps, r); ps.setLong(9, r.getId()); ps.executeUpdate(); }
        catch (SQLException ex) { throw new IllegalStateException("Could not update budget line", ex); }
    }
    private void bind(PreparedStatement ps, BudgetLineViewModel r) throws SQLException {
        ps.setString(1, r.getFund()); ps.setString(2, r.getBudgetCategory()); ps.setString(3, r.getLineType().name());
        ps.setBigDecimal(4, r.getPlannedAmount()); ps.setBigDecimal(5, r.getActualAmount()); ps.setBigDecimal(6, r.getVarianceAmount());
        ps.setString(7, r.getNotes()); ps.setString(8, r.getValidationState().name());
    }
    private BudgetLineViewModel map(ResultSet rs) throws SQLException {
        BudgetLineViewModel r = new BudgetLineViewModel(); r.setId(rs.getLong("id")); r.setFund(rs.getString("fund")); r.setBudgetCategory(rs.getString("budget_category"));
        r.setLineType(BudgetLineType.valueOf(rs.getString("line_type"))); r.setPlannedAmount(SqlSupport.money(rs,"planned_amount")); r.setActualAmount(SqlSupport.money(rs,"actual_amount"));
        r.setVarianceAmount(SqlSupport.money(rs,"variance_amount")); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
