package org.example.npbk.repo;

import java.sql.*;
import java.util.*;
import org.example.npbk.db.Database;
import org.example.npbk.model.*;

public class OutstandingRepository {
    private final Database database;
    public OutstandingRepository(Database database) { this.database = database; }
    public List<OutstandingItemViewModel> findAll() {
        List<OutstandingItemViewModel> rows = new ArrayList<>();
        try (var conn=database.getConnection(); Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM outstanding_items ORDER BY date_issued NULLS LAST, id")) {
            while (rs.next()) rows.add(map(rs));
        } catch (SQLException ex) { throw new IllegalStateException("Could not load outstanding items", ex); }
        return rows;
    }
    public void save(OutstandingItemViewModel row) { if (row.getId()==null) insert(row); else update(row); row.setDirty(false); }
    public void saveAll(Collection<OutstandingItemViewModel> rows) { rows.forEach(this::save); }
    private void insert(OutstandingItemViewModel r) {
        String sql="""
            INSERT INTO outstanding_items (source_transaction_id, date_issued, reference_number, name, details, bank_account, direction, amount, cleared_date, status, notes, validation_state)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { bind(ps,r); ps.executeUpdate(); try(ResultSet keys=ps.getGeneratedKeys()){ if(keys.next()) r.setId(keys.getLong(1));}}
        catch(SQLException ex){ throw new IllegalStateException("Could not insert outstanding item", ex); }
    }
    private void update(OutstandingItemViewModel r) {
        String sql="""
            UPDATE outstanding_items SET source_transaction_id=?, date_issued=?, reference_number=?, name=?, details=?, bank_account=?, direction=?, amount=?, cleared_date=?, status=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP
            WHERE id=?
            """;
        try (var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) { bind(ps,r); ps.setLong(13,r.getId()); ps.executeUpdate(); }
        catch(SQLException ex){ throw new IllegalStateException("Could not update outstanding item", ex); }
    }
    private void bind(PreparedStatement ps, OutstandingItemViewModel r) throws SQLException {
        SqlSupport.setLongOrNull(ps,1,r.getSourceTransactionId()); ps.setDate(2,SqlSupport.sqlDate(r.getDateIssued())); ps.setString(3,r.getReferenceNumber()); ps.setString(4,r.getName()); ps.setString(5,r.getDetails());
        ps.setString(6,r.getBankAccount()); ps.setString(7,r.getDirection().name()); ps.setBigDecimal(8,r.getAmount()); ps.setDate(9,SqlSupport.sqlDate(r.getClearedDate())); ps.setString(10,r.getStatus().name()); ps.setString(11,r.getNotes()); ps.setString(12,r.getValidationState().name());
    }
    private OutstandingItemViewModel map(ResultSet rs) throws SQLException {
        OutstandingItemViewModel r = new OutstandingItemViewModel(); r.setId(rs.getLong("id")); long src=rs.getLong("source_transaction_id"); r.setSourceTransactionId(rs.wasNull()?null:src);
        r.setDateIssued(SqlSupport.localDate(rs,"date_issued")); r.setReferenceNumber(rs.getString("reference_number")); r.setName(rs.getString("name")); r.setDetails(rs.getString("details")); r.setBankAccount(rs.getString("bank_account"));
        r.setDirection(OutstandingDirection.valueOf(rs.getString("direction"))); r.setAmount(SqlSupport.money(rs,"amount")); r.setClearedDate(SqlSupport.localDate(rs,"cleared_date")); r.setStatus(OutstandingStatus.valueOf(rs.getString("status"))); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
