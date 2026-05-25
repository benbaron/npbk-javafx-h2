package org.example.npbk.ui.template;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

/** Minimal JavaFX renderer for embedded workbook-form JSON templates. */
public class WorkbookTemplateRenderer {
    public Node render(JsonObject template) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");

        for (JsonElement colEl : template.getAsJsonArray("columns")) {
            double width = colEl.getAsJsonObject().get("widthPx").getAsDouble();
            grid.getColumnConstraints().add(new ColumnConstraints(width, width, width));
        }
        for (JsonElement rowEl : template.getAsJsonArray("rows")) {
            double height = rowEl.getAsJsonObject().get("heightPx").getAsDouble();
            grid.getRowConstraints().add(new RowConstraints(height, height, height));
        }

        int firstRow = template.getAsJsonArray("rows").get(0).getAsJsonObject().get("index").getAsInt();
        int firstCol = template.getAsJsonArray("columns").get(0).getAsJsonObject().get("index").getAsInt();
        JsonObject styles = template.getAsJsonObject("styles");

        for (JsonElement cellEl : template.getAsJsonArray("cells")) {
            JsonObject cell = cellEl.getAsJsonObject();
            Label label = new Label(cell.has("text") && !cell.get("text").isJsonNull() ? cell.get("text").getAsString() : "");
            label.getStyleClass().add("excel-cell");
            label.setWrapText(true);
            label.setMinSize(0, 0);
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            String styleId = String.valueOf(cell.get("styleId").getAsInt());
            if (styles.has(styleId)) {
                label.setStyle(css(styles.getAsJsonObject(styleId)));
            }
            int row = cell.get("row").getAsInt() - firstRow;
            int col = cell.get("col").getAsInt() - firstCol;
            grid.add(label, col, row);
        }
        return grid;
    }

    private String css(JsonObject style) {
        StringBuilder css = new StringBuilder("-fx-padding: 2 4 2 4;-fx-border-color:#d0d0d0;-fx-border-width:0.25;");
        JsonObject font = style.getAsJsonObject("font");
        if (font != null) {
            if (font.has("bold") && font.get("bold").getAsBoolean()) css.append("-fx-font-weight:bold;");
            if (font.has("italic") && font.get("italic").getAsBoolean()) css.append("-fx-font-style:italic;");
            if (font.has("size")) css.append("-fx-font-size:").append(font.get("size").getAsDouble()).append("pt;");
        }
        JsonObject fill = style.getAsJsonObject("fill");
        if (fill != null && fill.has("fgColor") && !fill.get("fgColor").isJsonNull()) {
            String color = fill.get("fgColor").getAsString();
            if (!"#000000".equals(color)) css.append("-fx-background-color:").append(color).append(';');
        }
        return css.toString();
    }
}
