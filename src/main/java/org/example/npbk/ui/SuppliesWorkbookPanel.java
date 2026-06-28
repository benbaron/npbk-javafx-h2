package org.example.npbk.ui;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Supplies pane modeled after the workbook Supplies page. */
public class SuppliesWorkbookPanel implements AppPanel
{
    private final BorderPane root = new BorderPane();
    private final QueryTablePanel table;

    public SuppliesWorkbookPanel(Database database)
    {
        this.table = new QueryTablePanel(database, "Supplies Records", "supplies");
        build();
    }

    private void build()
    {
        root.setMinSize(0, 0);

        Label heading = new Label("Supplies");
        heading.getStyleClass().add("h1");
        Label note = new Label("Modeled after the workbook Supplies page. This is a stored table because supplies are user-maintained records, distinct from durable inventory assets.");
        note.setWrapText(true);
        note.getStyleClass().add("muted");

        GridPane header = new GridPane();
        header.getStyleClass().add("excel-report-grid");
        header.setMinWidth(0);
        header.setMaxWidth(Double.MAX_VALUE);
        addColumn(header, 12);
        addColumn(header, 16);
        addColumn(header, 28);
        addColumn(header, 10);
        addColumn(header, 16);
        addColumn(header, 18);

        addCell(header, 0, 0, "Item Num", "report-header-cell");
        addCell(header, 1, 0, "Date Acquired", "report-header-cell");
        addCell(header, 2, 0, "Description", "report-header-cell");
        addCell(header, 3, 0, "Count", "report-header-cell");
        addCell(header, 4, 0, "Approx. Value", "report-header-cell");
        addCell(header, 5, 0, "Guardian", "report-header-cell");

        VBox top = new VBox(8, heading, note, header);
        top.setPadding(new Insets(12));
        top.setFillWidth(true);
        root.setTop(top);
        root.setCenter(table.root());
    }

    private void addColumn(GridPane grid, double percentWidth)
    {
        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(percentWidth);
        column.setMinWidth(60);
        column.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(column);
    }

    private void addCell(GridPane grid, int col, int row, String text, String styleClass)
    {
        Label label = new Label(text);
        label.getStyleClass().addAll("excel-cell", styleClass);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(28);
        label.setWrapText(true);
        GridPane.setHgrow(label, Priority.ALWAYS);
        grid.add(label, col, row);
    }

    @Override
    public String title()
    {
        return "Supplies";
    }

    @Override
    public Node root()
    {
        return root;
    }

    @Override
    public void onRefresh()
    {
        table.onRefresh();
    }
}
