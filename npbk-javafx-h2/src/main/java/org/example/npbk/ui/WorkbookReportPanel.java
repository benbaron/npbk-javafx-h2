package org.example.npbk.ui;

import org.example.npbk.db.Database;
import org.example.npbk.ui.template.JsonWorkbookTemplateLoader;
import org.example.npbk.ui.template.WorkbookTemplateRenderer;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Read-only workbook-style financial report panel backed by embedded JSON templates. */
public class WorkbookReportPanel implements AppPanel {
    private final String title;
    private final String workbookSheetName;
    private final BorderPane root = new BorderPane();
    private final QueryTablePanel backingTable;
    private final WorkbookTemplateRenderer renderer = new WorkbookTemplateRenderer();

    public WorkbookReportPanel(Database database, String workbookSheetName, String title, String viewName) {
        this.title = title;
        this.workbookSheetName = workbookSheetName;
        this.backingTable = new QueryTablePanel(database, title + " Data", viewName);
        build();
    }

    private void build() {
        Label heading = new Label(title);
        heading.getStyleClass().add("report-title");
        Label sheet = new Label("Workbook sheet model: " + workbookSheetName + " — layout rendered from embedded JSON extracted from the report workbook.");
        sheet.getStyleClass().add("muted");
        VBox top = new VBox(8, heading, sheet);
        top.setPadding(new Insets(12));
        root.setTop(top);
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
        try {
            Node form = renderer.render(JsonWorkbookTemplateLoader.load(workbookSheetName));
            VBox content = new VBox(16, form, backingTable.root());
            content.setPadding(new Insets(12));
            root.setCenter(content);
            backingTable.onRefresh();
        } catch (RuntimeException ex) {
            Label missing = new Label("Embedded template for " + workbookSheetName + " is not available yet: " + ex.getMessage());
            missing.setWrapText(true);
            missing.getStyleClass().add("muted");
            root.setCenter(missing);
        }
    }
}