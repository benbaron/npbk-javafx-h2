package org.example.npbk.repo;

import java.sql.*; import java.util.*;
import org.example.npbk.db.Database; import org.example.npbk.model.*;

public class SupplyRepository {
    private final Database database;
    public SupplyRepository(Database database){ this.database=database; }
    public List<SupplyItemViewModel> findAll(){
        List<SupplyItemViewModel> rows=new ArrayList<>();
        try(var conn=database.getConnection(); Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM supply_items ORDER BY description, id")){
            while(rs.next()) rows.add(map(rs));
        } catch(SQLException ex){ throw new IllegalStateException("Could not load supplies", ex); }
        return rows;
    }
    public void save(SupplyItemViewModel r){ if(r.getId()==null) insert(r); else update(r); r.setDirty(false); }
    public void saveAll(Collection<SupplyItemViewModel> rows){ rows.forEach(this::save); }
    private void insert(SupplyItemViewModel r){
        String sql=
        	"""
        	INSERT INTO supply_items (description, quantity, unit_cost, total_value, used_for, location, guardian_name, notes, validation_state) VALUES (?,?,?,?,?,?,?,?,?)""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ bind(ps,r); ps.executeUpdate(); try(ResultSet keys=ps.getGeneratedKeys()){ if(keys.next()) r.setId(keys.getLong(1));}}
        catch(SQLException ex){ throw new IllegalStateException("Could not insert supply item", ex); }
    }
    private void update(SupplyItemViewModel r){
        String sql=
        	"""
        	UPDATE supply_items SET description=?, quantity=?, unit_cost=?, total_value=?, used_for=?, location=?, guardian_name=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP WHERE id=?""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){ bind(ps,r); ps.setLong(10,r.getId()); ps.executeUpdate(); }
        catch(SQLException ex){ throw new IllegalStateException("Could not update supply item", ex); }
    }
    private void bind(PreparedStatement ps, SupplyItemViewModel r) throws SQLException{
        ps.setString(1,r.getDescription()); ps.setBigDecimal(2,r.getQuantity()); ps.setBigDecimal(3,r.getUnitCost()); ps.setBigDecimal(4,r.getTotalValue()); ps.setString(5,r.getUsedFor()); ps.setString(6,r.getLocation()); ps.setString(7,r.getGuardianName()); ps.setString(8,r.getNotes()); ps.setString(9,r.getValidationState().name());
    }
    private SupplyItemViewModel map(ResultSet rs) throws SQLException{
        SupplyItemViewModel r=new SupplyItemViewModel(); r.setId(rs.getLong("id")); r.setDescription(rs.getString("description")); r.setQuantity(SqlSupport.money(rs,"quantity")); r.setUnitCost(SqlSupport.money(rs,"unit_cost")); r.setTotalValue(SqlSupport.money(rs,"total_value")); r.setUsedFor(rs.getString("used_for")); r.setLocation(rs.getString("location")); r.setGuardianName(rs.getString("guardian_name")); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
