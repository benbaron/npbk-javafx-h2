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

/** Workbook-inspired read-only BalanceStmt panel with explicit typed fields. */
public class BalanceStatementPanel implements AppPanel {
    private final Database database;
    private final BorderPane root = new BorderPane();
    private final VBox formHost = new VBox();
    private final QueryTablePanel backingTable;

    public BalanceStatementPanel(Database database) {
        this.database = database;
        this.backingTable = new QueryTablePanel(database, "Balance Statement Data", "balance_stmt_view");
        Label heading = new Label("Balance Statement");
        heading.getStyleClass().add("report-title");
        Label note = new Label("Workbook-inspired read-only pane modeled on BalanceStmt. Static report chrome is JavaFX; calculated fields are typed values from H2.");
        note.getStyleClass().add("muted");
        note.setWrapText(true);
        VBox top = new VBox(8, heading, note, formHost);
        top.setPadding(new Insets(12));
        root.setTop(top);
        root.setCenter(backingTable.root());
    }

    @Override public String title() { return "Balance Statement"; }
    @Override public Node root() { return root; }

    @Override
    public void onRefresh() {
        formHost.getChildren().setAll(renderForm());
        backingTable.onRefresh();
    }

    private Node renderForm() {
        Map<String, BigDecimal> v = ReportPanelSupport.loadAmounts(database, "balance_stmt_view", "account_name", "ending_balance");
        GridPane g = new GridPane();
        ReportPanelSupport.configureWorkbookGrid(g);
        int r = 0;
        ReportPanelSupport.addSpan(g, 0, r++, 6, "COMPARATIVE BALANCE STATEMENT", "report-title-cell");
        ReportPanelSupport.addSpan(g, 0, r++, 6, "Modeled on workbook page: BalanceStmt", "report-subtitle-cell");
        ReportPanelSupport.header(g, r++, "Line", "Description", "Current Period", "Prior Period", "Source", "Notes");
        r = section(g, r, "I. ASSETS");
        r = line(g, r, "I.a", "Undep. & non-interest cash", v, "Undep. & non-interest cash", "Asset account");
        r = line(g, r, "I.b", "Cash Earning Interest", v, "Cash Earning Interest", "Asset account");
        r = line(g, r, "I.c", "Receivables", v, "Receivables", "Schedule-backed asset");
        r = line(g, r, "I.d", "Inventory", v, "Inventory", "Inventory records");
        r = line(g, r, "I.e", "Regalia", v, "Regalia", "Property category");
        r = line(g, r, "I.f", "Purchased Property & Equipment", v, "Purchased Property & Equipment", "Durable property");
        r = line(g, r, "I.g", "Less Accum. Depreciation", v, "Less Accum. Depreciation", "Contra-asset");
        r = line(g, r, "I.h", "Prepaid Expenses", v, "Prepaid Expenses", "Schedule-backed asset");
        r = line(g, r, "I.i", "Other Assets", v, "Other Assets", "Other asset detail");
        r = total(g, r, "TOTAL ASSETS", ReportPanelSupport.sum(v, "Undep. & non-interest cash", "Cash Earning Interest", "Receivables", "Inventory", "Regalia", "Purchased Property & Equipment", "Less Accum. Depreciation", "Prepaid Expenses", "Other Assets"));
        r = section(g, r, "II. LIABILITIES");
        r = line(g, r, "II.a", "Newsletter Subs. Due", v, "Newsletter Subs. Due", "Liability account");
        r = line(g, r, "II.b", "Deferred Revenue", v, "Deferred Revenue", "Schedule-backed liability");
        r = line(g, r, "II.c", "Payables", v, "Payables", "Schedule-backed liability");
        r = line(g, r, "II.d", "Other Liabilities", v, "Other Liabilities", "Other liability detail");
        BigDecimal liabilities = ReportPanelSupport.sum(v, "Newsletter Subs. Due", "Deferred Revenue", "Payables", "Other Liabilities");
        r = total(g, r, "TOTAL LIABILITIES", liabilities);
        r = section(g, r, "III. NET ASSETS");
        r = line(g, r, "III", "Net Assets", v, "Net Assets", "Equity / accumulated activity");
        total(g, r, "TOTAL LIABILITIES AND NET ASSETS", liabilities.add(ReportPanelSupport.amount(v, "Net Assets")));
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
        ReportPanelSupport.add(g, 4, row, "balance_stmt_view", "report-value-cell");
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
