package org.example.npbk.report;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.example.npbk.db.Database;

/** Small JDBC helpers for report value providers. */
final class SqlReportSupport {
    private final Database database;

    SqlReportSupport(Database database) {
        this.database = database;
    }

    BigDecimal accountBalance(String accountName, ReportContext context) {
        String sql = """
            SELECT COALESCE(SUM(l.debit_amount - l.credit_amount), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            WHERE a.name = ? AND t.transaction_date <= ?
            """;
        return decimal(sql, accountName, Date.valueOf(context.periodEnd()));
    }

    BigDecimal accountActivity(String accountName, ReportContext context) {
        String sql = """
            SELECT COALESCE(SUM(l.credit_amount - l.debit_amount), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            WHERE a.name = ? AND t.transaction_date BETWEEN ? AND ?
            """;
        return decimal(sql, accountName, Date.valueOf(context.periodStart()), Date.valueOf(context.periodEnd()));
    }

    List<Map<String, Object>> rows(String sql, Object... args) {
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                java.sql.ResultSetMetaData md = rs.getMetaData();
                int count = md.getColumnCount();
                java.util.ArrayList<Map<String, Object>> rows = new java.util.ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= count; i++) {
                        row.put(md.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load report rows", ex);
        }
    }

    private BigDecimal decimal(String sql, Object... args) {
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, args);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBigDecimal(1) != null ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not calculate report value", ex);
        }
    }

    private void bind(PreparedStatement ps, Object... args) throws SQLException {
        for (int i = 0; i < args.length; i++) {
            ps.setObject(i + 1, args[i]);
        }
    }
}
