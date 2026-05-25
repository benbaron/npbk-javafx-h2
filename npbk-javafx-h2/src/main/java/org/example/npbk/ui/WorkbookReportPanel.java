package org.example.npbk.ui;

import org.example.npbk.db.Database;
import org.example.npbk.ui.template.WorkbookJsonFormRenderer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Read-only workbook-style financial report panel. */
public class WorkbookReportPanel implements AppPanel {
    private final String title;
    private final String workbookSheetName;
    private final String viewName;
    private final BorderPane root = new BorderPane();
    private final VBox templateHost = new VBox();
    private final QueryTablePanel backingTable;

    public WorkbookReportPanel(Database database, String workbookSheetName, String title, String viewName) {
        this.title = title;
        this.workbookSheetName = workbookSheetName;
        this.viewName = viewName;
        this.backingTable = new QueryTablePanel(database, title + " Data", viewName);
        build();
    }

    private void build() {
        Label heading = new Label(title);
        heading.getStyleClass().add("report-title");
        Label sheet = new Label("Workbook sheet model: " + workbookSheetName + " — read-only; embedded JSON template plus H2 backing view.");
        sheet.getStyleClass().add("muted");

        templateHost.setSpacing(8);
        templateHost.setPadding(new Insets(12));
        VBox top = new VBox(8, heading, sheet, templateHost);
        top.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(backingTable.root());
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
        renderEmbeddedTemplate();
        backingTable.onRefresh();
    }

    private void renderEmbeddedTemplate() {
        templateHost.getChildren().clear();
        try {
            templateHost.getChildren().add(new WorkbookJsonFormRenderer().render(workbookSheetName));
        } catch (RuntimeException ex) {
            Label warning = new Label("Embedded JSON template for " + workbookSheetName + " is not available yet. "
                    + "Run the local workbook-template extraction step and commit the generated JSON resource bundle.\n\n"
                    + "Backing data view: " + viewName + "\n"
                    + "Reason: " + ex.getMessage());
            warning.getStyleClass().addAll("excel-cell", "report-note-cell");
            warning.setWrapText(true);
            warning.setMinWidth(800);
            templateHost.getChildren().add(warning);
        }
    }
}
