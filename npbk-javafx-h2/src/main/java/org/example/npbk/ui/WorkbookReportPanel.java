package org.example.npbk.ui;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Read-only workbook-style financial report panel. */
public class WorkbookReportPanel implements AppPanel {
    private final Database database;
    private final String workbookSheetName;
    private final String title;
    private final String viewName;
    private final BorderPane root = new BorderPane();
    private final QueryTablePanel backingTable;

    public WorkbookReportPanel(Database database, String workbookSheetName, String title, String viewName) {
        this.database = database;
        this.workbookSheetName = workbookSheetName;
        this.title = title;
        this.viewName = viewName;
        this.backingTable = new QueryTablePanel(database, title + " Data", viewName);
        build();
    }

    private void build() {
        Label heading = new Label(title);
        heading.getStyleClass().add("report-title");
        Label sheet = new Label("Workbook sheet model: " + workbookSheetName + " — read-only; export targets: text, .xlsx, PDF.");
        sheet.getStyleClass().add("muted");

        GridPane preview = new GridPane();
        preview.getStyleClass().add("excel-report-grid");
        preview.setHgap(0);
        preview.setVgap(0);
        addCell(preview, 0, 0, "Section", "report-header-cell");
        addCell(preview, 1, 0, "Code", "report-header-cell");
        addCell(preview, 2, 0, "Account", "report-header-cell");
        addCell(preview, 3, 0, title.contains("Income") ? "Activity" : "Ending Balance", "report-header-cell");
        addCell(preview, 0, 1, "Derived from", "report-label-cell");
        addCell(preview, 1, 1, viewName, "report-value-cell");
        addCell(preview, 2, 1, "Database-backed view", "report-value-cell wide-cell");
        addCell(preview, 3, 1, "Read only", "report-value-cell");

        VBox top = new VBox(8, heading, sheet, preview);
        top.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(backingTable.root());
    }

    private void addCell(GridPane grid, int col, int row, String text, String styleClass) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().addAll("excel-cell", styleClass.split(" "));
        label.setMinWidth(col == 2 ? 320 : 140);
        label.setMinHeight(28);
        grid.add(label, col, row);
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Node root() {
        return root;
    }

    @Override
    public void onRefresh() {
        backingTable.onRefresh();
    }
}
