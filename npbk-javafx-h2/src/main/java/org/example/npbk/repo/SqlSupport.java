package org.example.npbk.repo;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

final class SqlSupport {
    private SqlSupport() {}
    static Date sqlDate(LocalDate value) { return value == null ? null : Date.valueOf(value); }
    static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column); return d == null ? null : d.toLocalDate();
    }
    static void setLongOrNull(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) ps.setNull(index, java.sql.Types.BIGINT); else ps.setLong(index, value);
    }
    static BigDecimal money(ResultSet rs, String column) throws SQLException {
        BigDecimal value = rs.getBigDecimal(column); return value == null ? BigDecimal.ZERO : value;
    }
}
