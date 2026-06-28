package org.example.npbk.ui.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.example.npbk.report.ReportValueSet;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

class SemanticReportRendererTest
{
    private static final double EPSILON = 0.0001;
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void tableReportUsesResponsivePercentageColumns() throws Exception
    {
        JsonNode template = mapper.readTree("""
            {
              "templateId": "GeometryTest",
              "title": "Geometry Test",
              "type": "tableReport",
              "tableKey": "rows",
              "columns": [
                {"label": "Date", "field": "date"},
                {"label": "Reference", "field": "reference"},
                {"label": "Bank Account", "field": "bankAccount"},
                {"label": "Legal Name", "field": "legalName"},
                {"label": "Amount", "field": "amount"}
              ]
            }
            """);

        GridPane grid = renderedGrid(template);

        assertEquals(5, grid.getColumnConstraints().size());
        assertEquals(100.0, percentWidthTotal(grid), EPSILON);
        for (ColumnConstraints column : grid.getColumnConstraints())
        {
            assertTrue(column.getPercentWidth() > 0.0);
            assertTrue(column.getMinWidth() <= 64.0);
        }
        assertCellsCanShrink(grid);
    }

    @Test
    void sectionReportUsesViewportFriendlyColumns() throws Exception
    {
        JsonNode template = mapper.readTree("""
            {
              "templateId": "SectionGeometryTest",
              "title": "Section Geometry Test",
              "type": "sectionReport",
              "sections": [
                {
                  "title": "Assets",
                  "rows": [
                    {"line": "1", "label": "Cash", "valueKey": "cash", "format": "currency"}
                  ]
                }
              ]
            }
            """);

        GridPane grid = renderedGrid(template);

        assertEquals(4, grid.getColumnConstraints().size());
        assertEquals(100.0, percentWidthTotal(grid), EPSILON);
        assertCellsCanShrink(grid);
    }

    private GridPane renderedGrid(JsonNode template)
    {
        Node rendered = new SemanticReportRenderer().render(template, new ReportValueSet());
        VBox root = (VBox) rendered;
        return (GridPane) root.getChildren().get(root.getChildren().size() - 1);
    }

    private double percentWidthTotal(GridPane grid)
    {
        return grid.getColumnConstraints().stream()
            .mapToDouble(ColumnConstraints::getPercentWidth)
            .sum();
    }

    private void assertCellsCanShrink(GridPane grid)
    {
        for (Node node : grid.getChildren())
        {
            if (node instanceof Label label)
                assertEquals(0.0, label.getMinWidth(), EPSILON);
        }
    }
}
