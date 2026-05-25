package org.example.npbk.ui.report;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

/** Shared helpers for workbook-inspired JavaFX report panels. */
public final class ReportPanelSupport {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);

    private ReportPanelSupport() {
    }

    public static Map<String, BigDecimal> loadAmounts(Database database, String viewName, String keyColumn, String amountColumn) {
        Map<String, BigDecimal> values = new LinkedHashMap<>();
        String sql = "SELECT " + safe(keyColumn) + ", " + safe(amountColumn) + " FROM " + safe(viewName);
        try (var conn = database.getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                BigDecimal value = rs.getBigDecimal(2);
                values.put(rs.getString(1), value == null ? BigDecimal.ZERO : value);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load report amounts from " + viewName, ex);
        }
        return values;
    }

    public static BigDecimal amount(Map<String, BigDecimal> values, String key) {
        return values.getOrDefault(key, BigDecimal.ZERO);
    }

    public static BigDecimal sum(Map<String, BigDecimal> values, String... keys) {
        BigDecimal total = BigDecimal.ZERO;
        for (String key : keys) {
            total = total.add(amount(values, key));
        }
        return total;
    }

    public static String money(BigDecimal value) {
        BigDecimal safe = value == null ? BigDecimal.ZERO : value;
        if (safe.compareTo(BigDecimal.ZERO) == 0) {
            return "-";
        }
        if (safe.signum() < 0) {
            return "(" + MONEY.format(safe.abs()) + ")";
        }
        return MONEY.format(safe);
    }

    public static Label cell(String text, String... styleClasses) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add("excel-cell");
        label.getStyleClass().addAll(styleClasses);
        label.setWrapText(true);
        label.setMinHeight(24);
        label.setPadding(new Insets(3, 6, 3, 6));
        label.setAlignment(Pos.CENTER_LEFT);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    public static Label currency(BigDecimal value, String... styleClasses) {
        Label label = cell(money(value), styleClasses);
        label.setAlignment(Pos.CENTER_RIGHT);
        return label;
    }

    public static void add(GridPane grid, int col, int row, String text, String... styleClasses) {
        grid.add(cell(text, styleClasses), col, row);
    }

    public static void addCurrency(GridPane grid, int col, int row, BigDecimal value, String... styleClasses) {
        grid.add(currency(value, styleClasses), col, row);
    }

    public static void addSpan(GridPane grid, int col, int row, int colSpan, String text, String... styleClasses) {
        grid.add(cell(text, styleClasses), col, row, colSpan, 1);
    }

    public static void header(GridPane grid, int row, String... labels) {
        for (int i = 0; i < labels.length; i++) {
            add(grid, i, row, labels[i], "report-header-cell");
        }
    }

    public static void configureWorkbookGrid(GridPane grid) {
        grid.getStyleClass().add("excel-report-grid");
        grid.setHgap(0);
        grid.setVgap(0);
    }

    private static String safe(String identifier) {
        if (identifier == null || !identifier.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            throw new IllegalArgumentException("Unsafe SQL identifier: " + identifier);
        }
        return identifier;
    }
}
