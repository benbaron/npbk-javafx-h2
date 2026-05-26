package org.example.npbk.report;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.npbk.db.Database;

final class SqlReportSupport {
    private final Database database;

    SqlReportSupport(Database database) {
        this.database = database;
    }

    BigDecimal balanceForAccountName(String accountName, ReportContext context) {
        String sql = """
            SELECT COALESCE(SUM(l.debit_amount - l.credit_amount), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            WHERE a.name = ? AND t.transaction_date <= ?
            """;
        return queryMoney(sql, accountName, java.sql.Date.valueOf(context.periodEnd()));
    }

    BigDecimal activityForAccountName(String accountName, ReportContext context) {
        String sql = """
            SELECT COALESCE(SUM(l.credit_amount - l.debit_amount), 0)
            FROM transaction_lines l
            JOIN transactions t ON t.id = l.transaction_id
            JOIN accounts a ON a.id = l.account_id
            WHERE a.name = ? AND t.transaction_date BETWEEN ? AND ?
            """;
        return queryMoney(sql, accountName, java.sql.Date.valueOf(context.periodStart()), java.sql.Date.valueOf(context.periodEnd()));
    }

    private BigDecimal queryMoney(String sql, Object... args) {
        try (var conn = database.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not calculate report value", ex);
        }
    }
}
