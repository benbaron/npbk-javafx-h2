package org.example.npbk.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/** Right-side context pane for selected navigation/workspace items. */
public class InspectorPane extends BorderPane {
    private final Label heading = new Label("Inspector");
    private final TextArea body = new TextArea();

    public InspectorPane() {
        heading.getStyleClass().add("h2");
        body.setEditable(false);
        body.setWrapText(true);
        VBox box = new VBox(8, heading, body);
        box.setPadding(new Insets(10));
        setCenter(box);
    }

    public void showPanel(AppPanelId id, String title) {
        heading.setText(title);
        body.setText("Panel ID: " + id
                + "\n\nThis pane is a database-backed workspace. Workbook-like pages approximate the Excel sheet layout; reporting panes are read-only and can later export to text, .xlsx, or PDF."
                + "\n\nRoot data model: chart_of_accounts/accounts, transactions, transaction_lines, funds, budget categories, bank accounts, people, supplemental lines, supplies, and reporting views.");
    }
}
