
package org.example.npbk.repo;

import java.sql.*;
import java.util.*;
import org.example.npbk.db.Database;
import org.example.npbk.model.*;

// TODO: Auto-generated Javadoc
/**
 * The Class InventoryRepository.
 */
public class InventoryRepository
{
	
	/** The database. */
	private final Database database;
	
	/**
	 * Instantiates a new inventory repository.
	 *
	 * @param database the database
	 */
	public InventoryRepository(Database database)
	{
		this.database = database;
		
	}
	
	/**
	 * Find all.
	 *
	 * @return the list
	 */
	public List<InventoryItemViewModel> findAll()
	{
		List<InventoryItemViewModel> rows = new ArrayList<>();
		
		try (var conn = database.getConnection();
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(
				"SELECT * FROM inventory_items ORDER BY item_number, id"))
		{
			while (rs.next())
				rows.add(map(rs));
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException("Could not load inventory", ex);
		}
		
		return rows;
		
	}
	
	/**
	 * Save.
	 *
	 * @param r the r
	 */
	public void save(InventoryItemViewModel r)
	{
		if (r.getId() == null)
			insert(r);
		else
			update(r);
		r.setDirty(false);
		
	}
	
	/**
	 * Save all.
	 *
	 * @param rows the rows
	 */
	public void saveAll(Collection<InventoryItemViewModel> rows)
	{
		rows.forEach(this::save);
		
	}
	
	/**
	 * Insert.
	 *
	 * @param r the r
	 */
	private void insert(InventoryItemViewModel r)
	{
		String sql =
			"""
				INSERT INTO inventory_items (item_number, acquired_date, description, quantity, total_value, item_type, used_for, guardian_name, confirmed_date, notes, validation_state) VALUES (?,?,?,?,?,?,?,?,?,?,?)
				""";
		
		try (var conn = database.getConnection(); PreparedStatement ps =
			conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
		{
			bind(ps, r);
			ps.executeUpdate();
			
			try (ResultSet keys = ps.getGeneratedKeys())
			{
				if (keys.next())
					r.setId(keys.getLong(1));
			}
			
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException("Could not insert inventory item",
				ex);
		}
		
	}
	
	/**
	 * Update.
	 *
	 * @param r the r
	 */
	private void update(InventoryItemViewModel r)
	{
		String sql =
			"""
				UPDATE inventory_items SET item_number=?, acquired_date=?, description=?, quantity=?, total_value=?, item_type=?, used_for=?, guardian_name=?, confirmed_date=?, notes=?, validation_state=?, updated_at=CURRENT_TIMESTAMP WHERE id=?
				""";
		
		try (var conn = database.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql))
		{
			bind(ps, r);
			ps.setLong(12, r.getId());
			ps.executeUpdate();
		}
		catch (SQLException ex)
		{
			throw new IllegalStateException("Could not update inventory item",
				ex);
		}
		
	}
	
	/**
	 * Bind.
	 *
	 * @param ps the ps
	 * @param r the r
	 * @throws SQLException the SQL exception
	 */
	private static void bind(PreparedStatement ps, InventoryItemViewModel r)
		throws SQLException
	{
		ps.setString(1, r.getItemNumber());
		ps.setDate(2, SqlSupport.sqlDate(r.getAcquiredDate()));
		ps.setString(3, r.getDescription());
		ps.setBigDecimal(4, r.getQuantity());
		ps.setBigDecimal(5, r.getTotalValue());
		ps.setString(6, r.getItemType());
		ps.setString(7, r.getUsedFor());
		ps.setString(8, r.getGuardianName());
		ps.setDate(9, SqlSupport.sqlDate(r.getConfirmedDate()));
		ps.setString(10, r.getNotes());
		ps.setString(11, r.getValidationState().name());
		
	}
	
	/**
	 * Map.
	 *
	 * @param rs the rs
	 * @return the inventory item view model
	 * @throws SQLException the SQL exception
	 */
	private InventoryItemViewModel map(ResultSet rs) throws SQLException
	{
		InventoryItemViewModel r = new InventoryItemViewModel();
		r.setId(rs.getLong("id"));
		r.setItemNumber(rs.getString("item_number"));
		r.setAcquiredDate(SqlSupport.localDate(rs, "acquired_date"));
		r.setDescription(rs.getString("description"));
		r.setQuantity(SqlSupport.money(rs, "quantity"));
		r.setTotalValue(SqlSupport.money(rs, "total_value"));
		r.setItemType(rs.getString("item_type"));
		r.setUsedFor(rs.getString("used_for"));
		r.setGuardianName(rs.getString("guardian_name"));
		r.setConfirmedDate(SqlSupport.localDate(rs, "confirmed_date"));
		r.setNotes(rs.getString("notes"));
		r.setValidationState(
			ValidationState.valueOf(rs.getString("validation_state")));
		r.setDirty(false);
		return r;
		
	}
	
}
