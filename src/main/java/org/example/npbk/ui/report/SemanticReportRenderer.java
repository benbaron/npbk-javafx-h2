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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Renders compact semantic report templates with H2-backed values. */
public class SemanticReportRenderer {
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);

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
        label.setMinWidth(760);
        grid.add(label, 0, row, 4, 1);
    }

    private void addCell(GridPane grid, int col, int row, String text, String... styleClasses) {
        Label label = label(text, styleClasses);
        label.setMinWidth(switch (col) {
            case 0 -> 80;
            case 1 -> 420;
            case 2 -> 140;
            default -> 280;
        });
        grid.add(label, col, row);
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
