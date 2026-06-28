package org.example.npbk.ui.report;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class SemanticReportRenderer
{
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);
    private static final double MIN_TABLE_COLUMN_WIDTH = 64;

    public Node render(JsonNode template, ReportValueSet values)
    {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setFillWidth(true);
        root.setMinWidth(0);
        root.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(template.path("title").asText(template.path("templateId").asText("Report")));
        title.getStyleClass().add("report-title");
        title.setWrapText(true);
        title.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(title);

        if (template.hasNonNull("subtitle"))
        {
            Label sub = new Label(template.path("subtitle").asText());
            sub.getStyleClass().add("muted");
            sub.setWrapText(true);
            sub.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(sub);
        }

        String type = template.path("type").asText("sectionReport");
        Node report = "tableReport".equals(type)
            ? renderTable(template, values)
            : renderSections(template, values);
        root.getChildren().add(report);
        VBox.setVgrow(report, Priority.ALWAYS);
        return root;
    }

    private Node renderSections(JsonNode template, ReportValueSet values)
    {
        GridPane grid = baseGrid();
        setSectionColumnWidths(grid);
        int row = 0;
        addHeader(grid, row++, "Line", "Description", "Amount", "Notes");
        for (JsonNode section : template.path("sections"))
        {
            addMerged(grid, row++, section.path("title").asText(), "report-section-cell");
            for (JsonNode r : section.path("rows"))
            {
                String type = r.path("type").asText("valueRow");
                if ("spacer".equals(type))
                {
                    row++;
                }
                else if ("totalRow".equals(type))
                {
                    addRow(grid, row++, r, values, "report-total-cell");
                }
                else
                {
                    addRow(grid, row++, r, values, "report-value-cell");
                }
            }
        }
        return grid;
    }

    private Node renderTable(JsonNode template, ReportValueSet values)
    {
        GridPane grid = baseGrid();
        JsonNode columns = template.path("columns");
        setResponsiveTableColumnWidths(grid, columns);
        int row = 0;
        for (int c = 0; c < columns.size(); c++)
            addCell(grid, c, row, columns.get(c).path("label").asText(), "report-header-cell");
        row++;

        List<Map<String, Object>> rows = values.table(template.path("tableKey").asText());
        for (Map<String, Object> data : rows)
        {
            for (int c = 0; c < columns.size(); c++)
            {
                JsonNode col = columns.get(c);
                Object raw = data.get(col.path("field").asText());
                addCell(grid, c, row, format(raw, col.path("format").asText("text")), "report-value-cell");
            }
            row++;
        }
        if (rows.isEmpty())
        {
            Label note = label("No rows for the selected reporting period.", "report-note-cell");
            note.setMaxWidth(Double.MAX_VALUE);
            grid.add(note, 0, row, Math.max(1, columns.size()), 1);
        }
        return grid;
    }

    private GridPane baseGrid()
    {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        grid.setMinWidth(0);
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    private void setSectionColumnWidths(GridPane grid)
    {
        grid.getColumnConstraints().clear();
        grid.getColumnConstraints().add(percentColumn(10, 40));
        grid.getColumnConstraints().add(percentColumn(45, 120));
        grid.getColumnConstraints().add(percentColumn(20, 90));
        grid.getColumnConstraints().add(percentColumn(25, 100));
    }

    private void setResponsiveTableColumnWidths(GridPane grid, JsonNode columns)
    {
        grid.getColumnConstraints().clear();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0;
        for (JsonNode column : columns)
        {
            String label = column.path("label").asText("");
            String field = column.path("field").asText("");
            double weight = Math.max(6, Math.min(24, Math.max(label.length(), field.length())));
            weights.add(weight);
            totalWeight += weight;
        }
        if (totalWeight <= 0)
            totalWeight = 1;
        for (double weight : weights)
            grid.getColumnConstraints().add(percentColumn((weight / totalWeight) * 100.0, MIN_TABLE_COLUMN_WIDTH));
    }

    private ColumnConstraints percentColumn(double percentWidth, double minWidth)
    {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setMinWidth(minWidth);
        column.setHgrow(Priority.ALWAYS);
        column.setFillWidth(true);
        return column;
    }

    private void addHeader(GridPane grid, int row, String... labels)
    {
        for (int c = 0; c < labels.length; c++)
            addCell(grid, c, row, labels[c], "report-header-cell");
    }

    private void addRow(GridPane grid, int row, JsonNode spec, ReportValueSet values, String styleClass)
    {
        addCell(grid, 0, row, spec.path("line").asText(""), styleClass);
        addCell(grid, 1, row, spec.path("label").asText(""), styleClass, "wide-cell");
        String value = "";
        if (spec.hasNonNull("valueKey"))
            value = format(values.get(spec.path("valueKey").asText()), spec.path("format").asText("text"));
        addCell(grid, 2, row, value, styleClass, "report-currency-cell");
        addCell(grid, 3, row, spec.path("note").asText(""), styleClass, "wide-cell");
    }

    private void addMerged(GridPane grid, int row, String text, String styleClass)
    {
        Label label = label(text, styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        grid.add(label, 0, row, Math.max(1, grid.getColumnConstraints().size()), 1);
    }

    private void addCell(GridPane grid, int col, int row, String text, String... styleClasses)
    {
        Label label = label(text, styleClasses);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(label, Priority.ALWAYS);
        grid.add(label, col, row);
    }

    private Label label(String text, String... styleClasses)
    {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add("excel-cell");
        label.getStyleClass().addAll(styleClasses);
        label.setMinHeight(26);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    private String format(Object value, String format)
    {
        if (value == null)
            return "currency".equals(format) ? "-" : "";
        if ("currency".equals(format))
        {
            if (value instanceof BigDecimal bd)
                return bd.compareTo(BigDecimal.ZERO) == 0 ? "-" : MONEY.format(bd);
            if (value instanceof Number n)
                return n.doubleValue() == 0 ? "-" : MONEY.format(n.doubleValue());
        }
        if ("date".equals(format) && value instanceof LocalDate d)
            return d.toString();
        return String.valueOf(value);
    }
}
