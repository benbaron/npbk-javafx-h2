package org.example.npbk.ui;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Supplies pane modeled after the workbook Supplies page. */
public class SuppliesWorkbookPanel implements AppPanel {
    private final BorderPane root = new BorderPane();
    private final QueryTablePanel table;

    public SuppliesWorkbookPanel(Database database) {
        this.table = new QueryTablePanel(database, "Supplies Records", "supplies");
        build();
    }

    private void build() {
        Label heading = new Label("Supplies");
        heading.getStyleClass().add("h1");
        Label note = new Label("Modeled after the workbook Supplies page. This is a stored table because supplies are user-maintained records, distinct from durable inventory assets.");
        note.setWrapText(true);
        note.getStyleClass().add("muted");

        GridPane header = new GridPane();
        header.getStyleClass().add("excel-report-grid");
        addCell(header, 0, 0, "Item Num", "report-header-cell");
        addCell(header, 1, 0, "Date Acquired", "report-header-cell");
        addCell(header, 2, 0, "Description", "report-header-cell");
        addCell(header, 3, 0, "Count", "report-header-cell");
        addCell(header, 4, 0, "Approx. Value", "report-header-cell");
        addCell(header, 5, 0, "Guardian", "report-header-cell");

        VBox top = new VBox(8, heading, note, header);
        top.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(table.root());
    }

    private void addCell(GridPane grid, int col, int row, String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("excel-cell", styleClass);
        label.setMinWidth(col == 2 ? 260 : 130);
        label.setMinHeight(28);
        grid.add(label, col, row);
    }

    @Override
    public String title() {
        return "Supplies";
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void onRefresh() {
        table.onRefresh();
    }
}
