package org.example.npbk.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.example.npbk.db.Database;
import org.example.npbk.ui.report.SemanticReportPanel;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;

/** Lazily creates and hosts the center workspace panel. */
public class PanelHost extends BorderPane
{
    private final Database database;
    private final Map<AppPanelId, Supplier<AppPanel>> factories = new EnumMap<>(AppPanelId.class);
    private final Map<AppPanelId, AppPanel> panels = new EnumMap<>(AppPanelId.class);
    private AppPanelId activeId;

    public PanelHost(Database database)
    {
        this.database = database;
        setMinSize(0, 0);
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        registerFactories();
    }

    private void registerFactories()
    {
        factories.put(AppPanelId.DASHBOARD, () -> new SimpleInfoPanel("Dashboard", "Workbook-modeled accounting workspace", "Use the navigation tree to open workbook pages, reports, reference tables, banking, and period close workflows."));
        factories.put(AppPanelId.WORKBOOK_SUMMARY, () -> new SemanticReportPanel(database, "WorkbookSummary", "Workbook Summary"));
        factories.put(AppPanelId.WORKBOOK_TABLES, () -> new WorkbookPagePanel(database, AppPanelId.WORKBOOK_TABLES, "WorkbookTables", false));
        factories.put(AppPanelId.SUPPLIES, () -> new SuppliesWorkbookPanel(database));
        factories.put(AppPanelId.TRANSACTION_EDITOR, () -> new TransactionEditorPanel(database));
        factories.put(AppPanelId.TRANSACTIONS_LIST, () -> new SemanticReportPanel(database, "TransactionsList", "Transactions List"));
        factories.put(AppPanelId.ALL_CHECKS_TFRS, () -> new SemanticReportPanel(database, "AllChecksTfrs", "All Checks & Transfers"));
        factories.put(AppPanelId.FUND_TRANSFERS, () -> new SemanticReportPanel(database, "FundTransfers", "Fund Transfers"));
        factories.put(AppPanelId.BALANCE_STMT, () -> new SemanticReportPanel(database, "BalanceStmt", "Balance Statement"));
        factories.put(AppPanelId.INCOME_STMT, () -> new SemanticReportPanel(database, "IncomeStmt", "Income Statement"));
        factories.put(AppPanelId.CHART_OF_ACCOUNTS, () -> new ReferenceTablePanel(database, "Chart of Accounts", "accounts"));
        factories.put(AppPanelId.FUNDS, () -> new ReferenceTablePanel(database, "Funds", "funds"));
        factories.put(AppPanelId.BUDGET_CATEGORIES, () -> new ReferenceTablePanel(database, "Budget Categories", "budget_categories"));
        factories.put(AppPanelId.BANK_ACCOUNTS, () -> new ReferenceTablePanel(database, "Bank Accounts", "bank_accounts"));
        factories.put(AppPanelId.BANKING, () -> new ReferenceTablePanel(database, "Banking / Statement Lines", "bank_statement_lines"));
        factories.put(AppPanelId.PERIOD_CLOSE, () -> new ReferenceTablePanel(database, "Period Close", "period_close_records"));
        factories.put(AppPanelId.SETTINGS, () -> new SimpleInfoPanel("Settings", "Prototype settings", "Configuration will move here as company profiles, database selection, and workbook export options are added."));
        factories.put(AppPanelId.HELP, () -> new SimpleInfoPanel("Help", "Workbook-to-application model", "The spreadsheet is the familiar data-entry and report reference. The database stores real accounting records and the panes render workbook-like views."));
    }

    public void show(AppPanelId id)
    {
        AppPanel panel = panels.computeIfAbsent(id, this::create);
        activeId = id;
        Node panelRoot = panel.root();
        normalizePanelRoot(panelRoot);
        setCenter(panelRoot);
        panel.onRefresh();
    }

    private void normalizePanelRoot(Node panelRoot)
    {
        if (panelRoot instanceof Region region)
        {
            region.setMinSize(0, 0);
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
    }

    public AppPanelId activePanelId()
    {
        return activeId;
    }

    public String activeTitle()
    {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        return panel == null ? "(none)" : panel.title();
    }

    public void refreshActive()
    {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        if (panel != null)
            panel.onRefresh();
    }

    public void saveActive()
    {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        if (panel != null)
            panel.onSave();
    }

    private AppPanel create(AppPanelId id)
    {
        Supplier<AppPanel> factory = factories.get(id);
        if (factory == null)
            throw new IllegalArgumentException("Unsupported panel id: " + id);
        return factory.get();
    }
}
