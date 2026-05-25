package org.example.npbk.repo;

import java.sql.*; import java.util.*;
import org.example.npbk.db.Database; import org.example.npbk.model.*;

public class BankingRepository {
    private final Database database;
    public BankingRepository(Database database){ this.database=database; }
    public List<BankStatementLineViewModel> findAll(){
        List<BankStatementLineViewModel> rows=new ArrayList<>();
        try(var conn=database.getConnection(); Statement st=conn.createStatement(); ResultSet rs=st.executeQuery("SELECT * FROM bank_statement_lines ORDER BY statement_date NULLS LAST, posted_date NULLS LAST, id")){
            while(rs.next()) rows.add(map(rs));
        } catch(SQLException ex){ throw new IllegalStateException("Could not load bank statement lines", ex); }
        return rows;
    }
    public void save(BankStatementLineViewModel r){ if(r.getId()==null) insert(r); else update(r); r.setDirty(false); }
    public void saveAll(Collection<BankStatementLineViewModel> rows){ rows.forEach(this::save); }
    private void insert(BankStatementLineViewModel r){
        String sql=
        	"""
        	INSERT INTO bank_statement_lines (bank_account, statement_date, posted_date, description, amount, status, matched_transaction_id, notes, validation_state) 
        	VALUES (?,?,?,?,?,?,?,?,?)
        	""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){ bind(ps,r); ps.executeUpdate(); try(ResultSet keys=ps.getGeneratedKeys()){ if(keys.next()) r.setId(keys.getLong(1));}}
        catch(SQLException ex){ throw new IllegalStateException("Could not insert bank statement line", ex); }
    }
    private void update(BankStatementLineViewModel r){
        String sql="""
        	UPDATE bank_statement_lines SET bank_account=?, statement_date=?, posted_date=?, description=?, amount=?, status=?, matched_transaction_id=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP WHERE id=?
        	""";
        try(var conn=database.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)){ bind(ps,r); ps.setLong(10,r.getId()); ps.executeUpdate(); }
        catch(SQLException ex){ throw new IllegalStateException("Could not update bank statement line", ex); }
    }
    private void bind(PreparedStatement ps, BankStatementLineViewModel r) throws SQLException{
        ps.setString(1,r.getBankAccount()); ps.setDate(2,SqlSupport.sqlDate(r.getStatementDate())); ps.setDate(3,SqlSupport.sqlDate(r.getPostedDate())); ps.setString(4,r.getDescription()); ps.setBigDecimal(5,r.getAmount()); ps.setString(6,r.getStatus().name()); SqlSupport.setLongOrNull(ps,7,r.getMatchedTransactionId()); ps.setString(8,r.getNotes()); ps.setString(9,r.getValidationState().name());
    }
    private BankStatementLineViewModel map(ResultSet rs) throws SQLException{
        BankStatementLineViewModel r=new BankStatementLineViewModel(); r.setId(rs.getLong("id")); r.setBankAccount(rs.getString("bank_account")); r.setStatementDate(SqlSupport.localDate(rs,"statement_date")); r.setPostedDate(SqlSupport.localDate(rs,"posted_date")); r.setDescription(rs.getString("description")); r.setAmount(SqlSupport.money(rs,"amount")); r.setStatus(BankStatementLineStatus.valueOf(rs.getString("status"))); long linked=rs.getLong("matched_transaction_id"); r.setMatchedTransactionId(rs.wasNull()?null:linked); r.setNotes(rs.getString("notes")); r.setValidationState(ValidationState.valueOf(rs.getString("validation_state"))); r.setDirty(false); return r;
    }
}
