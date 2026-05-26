package org.example.npbk.ui.report;

import org.example.npbk.db.Database;
import org.example.npbk.report.ReportContext;
import org.example.npbk.report.ReportValueProviderRegistry;
import org.example.npbk.report.ReportValueSet;
import org.example.npbk.report.SemanticReportTemplateLoader;
import org.example.npbk.ui.AppPanel;

import com.fasterxml.jackson.databind.JsonNode;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/** AppPanel for compact semantic report JSON templates. */
public class SemanticReportPanel implements AppPanel {
    private final Database database;
    private final String templateId;
    private final String title;
    private final BorderPane root = new BorderPane();

    public SemanticReportPanel(Database database, String templateId, String title) {
        this.database = database;
        this.templateId = templateId;
        this.title = title;
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
            ReportContext context = ReportContext.currentYearToDate();
            JsonNode template = SemanticReportTemplateLoader.load(templateId);
            ReportValueSet values = new ReportValueProviderRegistry(database).providerFor(templateId).loadValues(context);
            Node rendered = new SemanticReportRenderer().render(template, values);
            root.setCenter(rendered);
        } catch (RuntimeException ex) {
            Label error = new Label("Could not render report " + templateId + ": " + ex.getMessage());
            error.setWrapText(true);
            error.setPadding(new Insets(16));
            root.setCenter(error);
        }
    }
}
