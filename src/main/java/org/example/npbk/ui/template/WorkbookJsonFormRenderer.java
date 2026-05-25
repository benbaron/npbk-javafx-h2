package org.example.npbk.ui.template;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

/** Renders an embedded workbook-form JSON template as a JavaFX GridPane. */
public class WorkbookJsonFormRenderer {
    private static final int MAX_RENDER_ROWS = 650;

    public Node render(String sheetName) {
        JsonNode template = EmbeddedJsonWorkbookTemplates.template(sheetName);
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        grid.setHgap(0);
        grid.setVgap(0);

        applyColumns(grid, template.path("colWidths"));
        applyRows(grid, template.path("rowHeights"));

        for (JsonNode cell : template.path("cells")) {
            int row = cell.path("r").asInt();
            if (row >= MAX_RENDER_ROWS) {
                continue;
            }
            int col = cell.path("c").asInt();
            int styleId = cell.path("style").asInt(0);
            Label label = new Label(cell.path("text").asText(""));
            label.getStyleClass().add("excel-cell");
            String css = template.path("styles").path(String.valueOf(styleId)).path("css").asText("");
            if (!css.isBlank()) {
                label.setStyle(css);
            }
            label.setAlignment(Pos.CENTER_LEFT);
            label.setMinHeight(18);
            label.setWrapText(css.contains("wrap-text") || label.getText().length() > 35);
            if (cell.hasNonNull("formula")) {
                Tooltip.install(label, new Tooltip("=" + cell.path("formula").asText()));
            }
            grid.add(label, col, row);
        }

        for (JsonNode merge : template.path("merges")) {
            // Merged regions are already represented by the top-left cell. JavaFX
            // GridPane cannot retroactively merge an existing child, so this renderer
            // records spans by finding the matching child and updating constraints.
            int row = merge.path("r").asInt();
            if (row >= MAX_RENDER_ROWS) {
                continue;
            }
            int col = merge.path("c").asInt();
            int rs = merge.path("rs").asInt(1);
            int cs = merge.path("cs").asInt(1);
            for (Node child : grid.getChildren()) {
                Integer childRow = GridPane.getRowIndex(child);
                Integer childCol = GridPane.getColumnIndex(child);
                if ((childRow == null ? 0 : childRow) == row && (childCol == null ? 0 : childCol) == col) {
                    GridPane.setRowSpan(child, rs);
                    GridPane.setColumnSpan(child, cs);
                    break;
                }
            }
        }

        return grid;
    }

    private void applyColumns(GridPane grid, JsonNode widths) {
        int count = Math.min(widths.size(), 80);
        for (int i = 0; i < count; i++) {
            double px = widths.get(i).asDouble(64);
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(px <= 0 ? 0 : Math.max(18, px));
            cc.setPrefWidth(px <= 0 ? 0 : Math.max(18, px));
            cc.setMaxWidth(px <= 0 ? 0 : Math.max(18, px));
            grid.getColumnConstraints().add(cc);
        }
    }

    private void applyRows(GridPane grid, JsonNode heights) {
        int count = Math.min(heights.size(), MAX_RENDER_ROWS);
        for (int i = 0; i < count; i++) {
            double px = heights.get(i).asDouble(22);
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(Math.max(10, px));
            rc.setPrefHeight(Math.max(10, px));
            grid.getRowConstraints().add(rc);
        }
    }
}
