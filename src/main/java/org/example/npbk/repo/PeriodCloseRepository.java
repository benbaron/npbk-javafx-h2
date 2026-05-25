package org.example.npbk.repo;

import java.sql.*; import java.util.*;
import org.example.npbk.db.Database; import org.example.npbk.model.*;

public class PeriodCloseRepository {
    private final Database database;
    public PeriodCloseRepository(Database database){ this.database=database; }
    public List<PeriodCloseViewModel> findAll(){
        List<PeriodCloseViewModel> rows=new ArrayList<>();
        try(var conn=database.getConnection(); Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM period_close_records ORDER BY period_end NULLS LAST, id")){
            while(rs.next()) rows.add(map(rs));
        } catch(SQLException ex){ throw new IllegalStateException("Could not load period close records", ex); }
        return rows;
    }
    public void save(PeriodCloseViewModel r){ if(r.getId()==null) insert(r); else update(r); r.setDirty(false); }
    public void saveAll(Collection<PeriodCloseViewModel> rows){ rows.forEach(this::save); }
    private void insert(PeriodCloseViewModel r){
        String sql=
        	"""
        	INSERT INTO period_close_records (period_start, period_end, bank_reconciled, schedules_reviewed, reports_generated, close_status, notes, validation_state) VALUES (?,?,?,?,?,?,?,?)""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ bind(ps,r); ps.executeUpdate(); try(ResultSet keys=ps.getGeneratedKeys()){ if(keys.next()) r.setId(keys.getLong(1));}}
        catch(SQLException ex){ throw new IllegalStateException("Could not insert close record", ex); }
    }
    private void update(PeriodCloseViewModel r){
        String sql="""
        	UPDATE period_close_records SET period_start=?, period_end=?, bank_reconciled=?, schedules_reviewed=?, reports_generated=?, close_status=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP WHERE id=?""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){ bind(ps,r); ps.setLong(9,r.getId()); ps.executeUpdate(); }
        catch(SQLException ex){ throw new IllegalStateException("Could not update close record", ex); }
    }
    private void bind(PreparedStatement ps, PeriodCloseViewModel r) throws SQLException{
        ps.setDate(1,SqlSupport.sqlDate(r.getPeriodStart())); ps.setDate(2,SqlSupport.sqlDate(r.getPeriodEnd())); ps.setBoolean(3,r.isBankReconciled()); ps.setBoolean(4,r.isSchedulesReviewed()); ps.setBoolean(5,r.isReportsGenerated()); ps.setString(6,r.getCloseStatus().name()); ps.setString(7,r.getNotes()); ps.setString(8,r.getValidationState().name());
    }
    private PeriodCloseViewModel map(ResultSet rs) throws SQLException{
        PeriodCloseViewModel r=new PeriodCloseViewModel(); r.setId(rs.getLong("id")); r.setPeriodStart(SqlSupport.localDate(rs,"period_start")); r.setPeriodEnd(SqlSupport.localDate(rs,"period_end")); r.setBankReconciled(rs.getBoolean("bank_reconciled")); r.setSchedulesReviewed(rs.getBoolean("schedules_reviewed")); r.setReportsGenerated(rs.getBoolean("reports_generated")); r.setCloseStatus(PeriodCloseStatus.valueOf(rs.getString("close_status"))); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
