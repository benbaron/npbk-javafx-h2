package org.example.npbk.repo;

import java.sql.*;
import java.util.*;
import org.example.npbk.db.Database;
import org.example.npbk.model.*;

public class SupplementalLineRepository {
    private final Database database;
    public SupplementalLineRepository(Database database) { this.database = database; }
    public List<SupplementalLineViewModel> findByAssetSide(boolean assetSide) {
        List<SupplementalLineViewModel> all = findAll();
        return all.stream().filter(r -> assetSide == r.getLineKind().isAssetKind()).toList();
    }
    public List<SupplementalLineViewModel> findAll() {
        List<SupplementalLineViewModel> rows = new ArrayList<>();
        try (var conn=database.getConnection(); Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM supplemental_lines ORDER BY line_kind, due_date NULLS LAST, id")) {
            while (rs.next()) rows.add(map(rs));
        } catch(SQLException ex){ throw new IllegalStateException("Could not load supplemental lines", ex); }
        return rows;
    }
    public void save(SupplementalLineViewModel row){ if(row.getId()==null) insert(row); else update(row); row.setDirty(false); }
    public void saveAll(Collection<SupplementalLineViewModel> rows){ rows.forEach(this::save); }
    private void insert(SupplementalLineViewModel r){
        String sql="""
            INSERT INTO supplemental_lines (linked_transaction_id, line_kind, counterparty, description, reference, amount, due_date, start_date, end_date, remaining_amount, notes, validation_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){ bind(ps,r); ps.executeUpdate(); try(ResultSet keys=ps.getGeneratedKeys()){ if(keys.next()) r.setId(keys.getLong(1));}}
        catch(SQLException ex){ throw new IllegalStateException("Could not insert supplemental line", ex); }
    }
    private void update(SupplementalLineViewModel r){
        String sql="""
            UPDATE supplemental_lines SET linked_transaction_id=?, line_kind=?, counterparty=?, description=?, reference=?, amount=?, due_date=?, start_date=?, end_date=?, remaining_amount=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){ bind(ps,r); ps.setLong(13,r.getId()); ps.executeUpdate(); }
        catch(SQLException ex){ throw new IllegalStateException("Could not update supplemental line", ex); }
    }
    private void bind(PreparedStatement ps, SupplementalLineViewModel r) throws SQLException {
        SqlSupport.setLongOrNull(ps,1,r.getLinkedTransactionId()); ps.setString(2,r.getLineKind().name()); ps.setString(3,r.getCounterparty()); ps.setString(4,r.getDescription()); ps.setString(5,r.getReference()); ps.setBigDecimal(6,r.getAmount());
        ps.setDate(7,SqlSupport.sqlDate(r.getDueDate())); ps.setDate(8,SqlSupport.sqlDate(r.getStartDate())); ps.setDate(9,SqlSupport.sqlDate(r.getEndDate())); ps.setBigDecimal(10,r.getRemainingAmount()); ps.setString(11,r.getNotes()); ps.setString(12,r.getValidationState().name());
    }
    private SupplementalLineViewModel map(ResultSet rs) throws SQLException {
        SupplementalLineViewModel r=new SupplementalLineViewModel(); r.setId(rs.getLong("id")); long linked=rs.getLong("linked_transaction_id"); r.setLinkedTransactionId(rs.wasNull()?null:linked);
        r.setLineKind(SupplementalLineKind.valueOf(rs.getString("line_kind"))); r.setCounterparty(rs.getString("counterparty")); r.setDescription(rs.getString("description")); r.setReference(rs.getString("reference")); r.setAmount(SqlSupport.money(rs,"amount"));
        r.setDueDate(SqlSupport.localDate(rs,"due_date")); r.setStartDate(SqlSupport.localDate(rs,"start_date")); r.setEndDate(SqlSupport.localDate(rs,"end_date")); r.setRemainingAmount(SqlSupport.money(rs,"remaining_amount")); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
