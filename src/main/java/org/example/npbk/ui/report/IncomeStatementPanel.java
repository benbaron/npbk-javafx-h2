package org.example.npbk.ui.report;

import java.math.BigDecimal;
import java.util.Map;

import org.example.npbk.db.Database;
import org.example.npbk.ui.AppPanel;
import org.example.npbk.ui.QueryTablePanel;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Workbook-inspired read-only IncomeStmt panel with explicit typed fields. */
public class IncomeStatementPanel implements AppPanel {
    private final Database database;
    private final BorderPane root = new BorderPane();
    private final VBox formHost = new VBox();
    private final QueryTablePanel backingTable;

    public IncomeStatementPanel(Database database) {
        this.database = database;
        this.backingTable = new QueryTablePanel(database, "Income Statement Data", "income_stmt_view");
        Label heading = new Label("Income Statement");
        heading.getStyleClass().add("report-title");
        Label note = new Label("Workbook-inspired read-only pane modeled on IncomeStmt. Static report chrome is JavaFX; calculated fields are typed values from H2.");
        note.getStyleClass().add("muted");
        note.setWrapText(true);
        VBox top = new VBox(8, heading, note, formHost);
        top.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(backingTable.root());
    }

    @Override public String title() { return "Income Statement"; }
    @Override public Node root() { return root; }

    @Override
    public void onRefresh() {
        formHost.getChildren().setAll(renderForm());
        backingTable.onRefresh();
    }

    private Node renderForm() {
        Map<String, BigDecimal> v = ReportPanelSupport.loadAmounts(database, "income_stmt_view", "account_name", "activity_amount");
        GridPane g = new GridPane();
        ReportPanelSupport.configureWorkbookGrid(g);
        int r = 0;
        ReportPanelSupport.addSpan(g, 0, r++, 6, "STATEMENT OF ACTIVITIES", "report-title-cell");
        ReportPanelSupport.addSpan(g, 0, r++, 6, "Modeled on workbook page: IncomeStmt", "report-subtitle-cell");
        ReportPanelSupport.header(g, r++, "Line", "Description", "Current Period", "Prior Period", "Source", "Notes");
        r = section(g, r, "INCOME");
        r = line(g, r, "1a", "Fundraising: Non-medieval activities to earn income (car washes, bake sales, etc.)", v, "Fundraising: Non-medieval activities to earn income (car washes, bake sales, etc.)", "Income account");
        r = line(g, r, "2.0", "Direct Contributions/Donations", v, "Direct Contributions/Donations", "Income account");
        r = line(g, r, "3a", "Activity Related: Medieval activities to earn income (events, demos, heraldry fees)", v, "Activity Related: Medieval activities to earn income (events, demos, heraldry fees)", "Activity income");
        BigDecimal income = ReportPanelSupport.sum(v, "Fundraising: Non-medieval activities to earn income (car washes, bake sales, etc.)", "Direct Contributions/Donations", "Activity Related: Medieval activities to earn income (events, demos, heraldry fees)");
        r = total(g, r, "TOTAL INCOME", income);
        r = section(g, r, "EXPENSES");
        r = line(g, r, "21b", "21b Occupancy - Activity Rel", v, "21b Occupancy - Activity Rel", "Expense account");
        BigDecimal expenses = ReportPanelSupport.sum(v, "21b Occupancy - Activity Rel");
        r = total(g, r, "TOTAL EXPENSES", expenses);
        r = section(g, r, "CHANGE IN NET ASSETS");
        total(g, r, "NET INCOME / (LOSS)", income.subtract(expenses));
        return g;
    }

    private int section(GridPane g, int row, String label) {
        ReportPanelSupport.addSpan(g, 0, row, 6, label, "report-section-cell");
        return row + 1;
    }

    private int line(GridPane g, int row, String code, String label, Map<String, BigDecimal> values, String key, String note) {
        ReportPanelSupport.add(g, 0, row, code, "report-code-cell");
        ReportPanelSupport.add(g, 1, row, label, "report-label-cell", "wide-cell");
        ReportPanelSupport.addCurrency(g, 2, row, ReportPanelSupport.amount(values, key), "report-currency-cell");
        ReportPanelSupport.addCurrency(g, 3, row, BigDecimal.ZERO, "report-value-cell");
        ReportPanelSupport.add(g, 4, row, "income_stmt_view", "report-value-cell");
        ReportPanelSupport.add(g, 5, row, note, "report-note-cell");
        return row + 1;
    }

    private int total(GridPane g, int row, String label, BigDecimal value) {
        ReportPanelSupport.add(g, 0, row, "", "report-total-cell");
        ReportPanelSupport.add(g, 1, row, label, "report-total-cell", "wide-cell");
        ReportPanelSupport.addCurrency(g, 2, row, value, "report-total-currency-cell");
        ReportPanelSupport.add(g, 3, row, "", "report-total-cell");
        ReportPanelSupport.add(g, 4, row, "", "report-total-cell");
        ReportPanelSupport.add(g, 5, row, "", "report-total-cell");
        return row + 1;
    }
}
