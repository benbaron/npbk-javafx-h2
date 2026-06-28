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
public class SemanticReportRenderer
{
    private static final NumberFormat MONEY = NumberFormat.getCurrencyInstance(Locale.US);

    public Node render(JsonNode template, ReportValueSet values)
    {
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setMinWidth(0);
        root.setMaxWidth(Double.MAX_VALUE);

        Label title = new Label(template.path("title").asText(template.path("templateId").asText("Report")));
        title.getStyleClass().add("report-title");
        title.setMaxWidth(Double.MAX_VALUE);
        root.getChildren().add(title);

        if (template.hasNonNull("subtitle"))
        {
            Label sub = new Label(template.path("subtitle").asText());
            sub.getStyleClass().add("muted");
            sub.setMaxWidth(Double.MAX_VALUE);
            root.getChildren().add(sub);
        }

        String type = template.path("type").asText("sectionReport");
        Node reportBody = "tableReport".equals(type)
            ? renderTable(template, values)
            : renderSections(template, values);
        root.getChildren().add(reportBody);
        if (reportBody instanceof GridPane grid)
            VBox.setVgrow(grid, Priority.ALWAYS);
        return root;
    }

    private Node renderSections(JsonNode template, ReportValueSet values)
    {
        GridPane grid = responsiveGrid();
        setColumnWidths(grid, 80, 420, 140, 280);
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
        GridPane grid = responsiveGrid();
        JsonNode columns = template.path("columns");
        setTableColumnWidths(grid, columns);
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
                addCell(
                    grid,
                    c,
                    row,
                    format(raw, col.path("format").asText("text")),
                    "report-value-cell");
            }
            row++;
        }
        if (rows.isEmpty())
        {
            Label empty = label("No rows for the selected reporting period.", "report-note-cell");
            grid.add(empty, 0, row, Math.max(1, columns.size()), 1);
            GridPane.setHgrow(empty, Priority.ALWAYS);
        }
        return grid;
    }

    private GridPane responsiveGrid()
    {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        grid.setMinWidth(0);
        grid.setMaxWidth(Double.MAX_VALUE);
        return grid;
    }

    private void setColumnWidths(GridPane grid, double... preferredWidths)
    {
        grid.getColumnConstraints().clear();
        for (double preferredWidth : preferredWidths)
        {
            ReportColumnSizing.ColumnSize size =
                ReportColumnSizing.sectionColumn(preferredWidth);
            ColumnConstraints constraints = new ColumnConstraints(
                size.minimumWidth(),
                size.preferredWidth(),
                Double.MAX_VALUE);
            constraints.setHgrow(Priority.SOMETIMES);
            constraints.setFillWidth(true);
            grid.getColumnConstraints().add(constraints);
        }
    }

    private void setTableColumnWidths(GridPane grid, JsonNode columns)
    {
        grid.getColumnConstraints().clear();
        for (JsonNode column : columns)
        {
            ReportColumnSizing.ColumnSize size = ReportColumnSizing.tableColumn(
                column.path("label").asText(""),
                column.path("field").asText(""));
            ColumnConstraints constraints = new ColumnConstraints(
                size.minimumWidth(),
                size.preferredWidth(),
                Double.MAX_VALUE);
            constraints.setHgrow(Priority.SOMETIMES);
            constraints.setFillWidth(true);
            grid.getColumnConstraints().add(constraints);
        }
    }

    private void addHeader(GridPane grid, int row, String... labels)
    {
        for (int c = 0; c < labels.length; c++)
            addCell(grid, c, row, labels[c], "report-header-cell");
    }

    private void addRow(
        GridPane grid,
        int row,
        JsonNode spec,
        ReportValueSet values,
        String styleClass)
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
        GridPane.setHgrow(label, Priority.ALWAYS);
    }

    private void addCell(GridPane grid, int col, int row, String text, String... styleClasses)
    {
        Label label = label(text, styleClasses);
        grid.add(label, col, row);
        GridPane.setHgrow(label, Priority.ALWAYS);
        GridPane.setFillWidth(label, true);
    }

    private Label label(String text, String... styleClasses)
    {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().add("excel-cell");
        label.getStyleClass().addAll(styleClasses);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
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
            if (value instanceof BigDecimal decimal)
                return decimal.compareTo(BigDecimal.ZERO) == 0 ? "-" : MONEY.format(decimal);
            if (value instanceof Number number)
                return number.doubleValue() == 0 ? "-" : MONEY.format(number.doubleValue());
        }
        if ("date".equals(format) && value instanceof LocalDate date)
            return date.toString();
        return String.valueOf(value);
    }
}
