package org.example.npbk.ui;

import org.example.npbk.db.Database;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Workbook-page approximation for support pages that are not official reports. */
public class WorkbookPagePanel implements AppPanel {
    private final String title;
    private final String workbookSheetName;
    private final BorderPane root = new BorderPane();

    public WorkbookPagePanel(Database database, AppPanelId id, String workbookSheetName, boolean editable) {
        this.title = labelFor(id);
        this.workbookSheetName = workbookSheetName;
        build(editable);
    }

    private void build(boolean editable) {
        Label heading = new Label(title);
        heading.getStyleClass().add("h1");
        Label note = new Label("Workbook sheet model: " + workbookSheetName + ". "
                + (editable ? "Editable database-backed pane." : "Support/reporting pane; direct cell editing is disabled."));
        note.setWrapText(true);
        note.getStyleClass().add("muted");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("excel-report-grid");
        addCell(grid, 0, 0, "Workbook page", "report-header-cell");
        addCell(grid, 1, 0, "Database role", "report-header-cell");
        addCell(grid, 2, 0, "Implementation note", "report-header-cell");
        addCell(grid, 0, 1, workbookSheetName, "report-label-cell");
        addCell(grid, 1, 1, editable ? "stored records" : "derived support view", "report-value-cell");
        addCell(grid, 2, 1, "First pass approximates Excel borders/positioning; later slices will map more cell regions.", "report-value-cell wide-cell");

        VBox box = new VBox(10, heading, note, grid);
        box.setPadding(new Insets(16));
        root.setCenter(box);
    }

    private void addCell(GridPane grid, int col, int row, String text, String styleClass) {
        Label label = new Label(text == null ? "" : text);
        label.getStyleClass().addAll("excel-cell", styleClass.split(" "));
        label.setWrapText(true);
        label.setMinWidth(col == 2 ? 420 : 180);
        label.setMinHeight(32);
        grid.add(label, col, row);
    }

    private String labelFor(AppPanelId id) {
        return switch (id) {
            case WORKBOOK_SUMMARY -> "Workbook Summary";
            case WORKBOOK_TABLES -> "Workbook Tables";
            default -> id.name();
        };
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public Node root() {
        return root;
    }
}
