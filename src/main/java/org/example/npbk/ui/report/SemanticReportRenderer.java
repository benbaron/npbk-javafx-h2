package org.example.npbk.ui.report;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.example.npbk.report.ReportValueSet;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Renders compact semantic report templates with H2-backed values. */
public class SemanticReportRenderer {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);
    private static final double MIN_COLUMN_WIDTH_CHARS = 10;
    private static final double APPROX_CHAR_WIDTH_PX = 8;
    private static final double CELL_PADDING_PX = 24;
    private static final double MIN_READABLE_COLUMN_WIDTH = MIN_COLUMN_WIDTH_CHARS * APPROX_CHAR_WIDTH_PX + CELL_PADDING_PX;

    public Node render(JsonNode template, ReportValueSet values) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        Label title = new Label(template.path("title").asText(template.path("templateId").asText("Report")));
        title.getStyleClass().add("report-title");
        root.getChildren().add(title);
        if (template.hasNonNull("subtitle")) {
            Label sub = new Label(template.path("subtitle").asText());
            sub.getStyleClass().add("muted");
            root.getChildren().add(sub);
        }
        String type = template.path("type").asText("sectionReport");
        if ("tableReport".equals(type)) {
            root.getChildren().add(renderTable(template, values));
        } else {
            root.getChildren().add(renderSections(template, values));
        }
        return root;
    }

    private Node renderSections(JsonNode template, ReportValueSet values) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        setColumnWidths(grid, 80, 420, 140, 280);
        int row = 0;
        addHeader(grid, row++, "Line", "Description", "Amount", "Notes");
        for (JsonNode section : template.path("sections")) {
            addMerged(grid, row++, section.path("title").asText(), "report-section-cell");
            for (JsonNode r : section.path("rows")) {
                String type = r.path("type").asText("valueRow");
                if ("spacer".equals(type)) {
                    row++;
                } else if ("totalRow".equals(type)) {
                    addRow(grid, row++, r, values, "report-total-cell");
                } else {
                    addRow(grid, row++, r, values, "report-value-cell");
                }
            }
        }
        return grid;
    }

    private Node renderTable(JsonNode template, ReportValueSet values) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        JsonNode columns = template.path("columns");
        setTableColumnWidths(grid, columns);
        int row = 0;
        for (int c = 0; c < columns.size(); c++) {
            addCell(grid, c, row, columns.get(c).path("label").asText(), "report-header-cell");
        }
        row++;
        List<Map<String, Object>> rows = values.table(template.path("tableKey").asText());
        for (Map<String, Object> data : rows) {
            for (int c = 0; c < columns.size(); c++) {
                JsonNode col = columns.get(c);
                Object raw = data.get(col.path("field").asText());
                addCell(grid, c, row, format(raw, col.path("format").asText("text")), "report-value-cell");
            }
            row++;
        }
        if (rows.isEmpty()) {
            addCell(grid, 0, row, "No rows for the selected reporting period.", "report-note-cell");
        }
        return grid;
    }

    private void setColumnWidths(GridPane grid, double... widths) {
        grid.getColumnConstraints().clear();
        for (double width : widths) {
            ColumnConstraints cc = new ColumnConstraints();
            double adjusted = Math.max(MIN_READABLE_COLUMN_WIDTH, width);
            cc.setMinWidth(adjusted);
            cc.setPrefWidth(adjusted);
            cc.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(cc);
        }
    }

    private void setTableColumnWidths(GridPane grid, JsonNode columns) {
        grid.getColumnConstraints().clear();
        for (JsonNode column : columns) {
            String label = column.path("label").asText("");
            String field = column.path("field").asText("");
            int chars = Math.max(label.length(), field.length());
            double width = Math.max(MIN_READABLE_COLUMN_WIDTH, Math.min(320, chars * APPROX_CHAR_WIDTH_PX + CELL_PADDING_PX));
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(width);
            cc.setPrefWidth(width);
            cc.setHgrow(Priority.NEVER);
            grid.getColumnConstraints().add(cc);
        }
    }

    private void addHeader(GridPane grid, int row, String... labels) {
        for (int c = 0; c < labels.length; c++) addCell(grid, c, row, labels[c], "report-header-cell");
    }

    private void addRow(GridPane grid, int row, JsonNode spec, ReportValueSet values, String styleClass) {
        addCell(grid, 0, row, spec.path("line").asText(""), styleClass);
        addCell(grid, 1, row, spec.path("label").asText(""), styleClass, "wide-cell");
        String value = "";
        if (spec.hasNonNull("valueKey")) {
            value = format(values.get(spec.path("valueKey").asText()), spec.path("format").asText("text"));
        }
        addCell(grid, 2, row, value, styleClass, "report-currency-cell");
        addCell(grid, 3, row, spec.path("note").asText(""), styleClass, "wide-cell");
    }

    private void addMerged(GridPane grid, int row, String text, String styleClass) {
        Label label = label(text, styleClass);
        label.setMinWidth(totalGridWidth(grid));
        grid.add(label, 0, row, Math.max(1, grid.getColumnConstraints().size()), 1);
    }

    private double totalGridWidth(GridPane grid) {
        double total = 0;
        for (ColumnConstraints cc : grid.getColumnConstraints()) {
            total += Math.max(MIN_READABLE_COLUMN_WIDTH, cc.getPrefWidth());
        }
        return total;
    }

    private void addCell(GridPane grid, int col, int row, String text, String... styleClasses) {
        Label label = label(text, styleClasses);
        label.setMinWidth(columnWidth(grid, col));
        grid.add(label, col, row);
    }

    private double columnWidth(GridPane grid, int col) {
        if (col >= 0 && col < grid.getColumnConstraints().size()) {
            return Math.max(MIN_READABLE_COLUMN_WIDTH, grid.getColumnConstraints().get(col).getPrefWidth());
        }
        return MIN_READABLE_COLUMN_WIDTH;
    }

    private Label label(String text, String... styleClasses) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add("excel-cell");
        label.getStyleClass().addAll(styleClasses);
        label.setMinHeight(26);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    private String format(Object value, String format) {
        if (value == null) return "currency".equals(format) ? "-" : "";
        if ("currency".equals(format)) {
            if (value instanceof BigDecimal bd) return bd.compareTo(BigDecimal.ZERO) == 0 ? "-" : MONEY.format(bd);
            if (value instanceof Number n) return n.doubleValue() == 0 ? "-" : MONEY.format(n.doubleValue());
        }
        if ("date".equals(format) && value instanceof LocalDate d) return d.toString();
        return String.valueOf(value);
    }
}