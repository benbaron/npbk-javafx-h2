package org.example.npbk.ui;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.example.npbk.db.Database;

import javafx.scene.layout.BorderPane;

/** Lazily creates and hosts the center workspace panel. */
public class PanelHost extends BorderPane {
    private final Database database;
    private final Map<AppPanelId, Supplier<AppPanel>> factories = new EnumMap<>(AppPanelId.class);
    private final Map<AppPanelId, AppPanel> panels = new EnumMap<>(AppPanelId.class);
    private AppPanelId activeId;

    public PanelHost(Database database) {
        this.database = database;
        registerFactories();
    }

    private void registerFactories() {
        factories.put(AppPanelId.DASHBOARD, () -> new SimpleInfoPanel("Dashboard", "Workbook-modeled accounting workspace", "Use the navigation tree to open workbook pages, reports, reference tables, banking, and period close workflows."));
        factories.put(AppPanelId.WORKBOOK_SUMMARY, () -> new WorkbookPagePanel(database, AppPanelId.WORKBOOK_SUMMARY, "WorkbookSummary", false));
        factories.put(AppPanelId.WORKBOOK_TABLES, () -> new WorkbookPagePanel(database, AppPanelId.WORKBOOK_TABLES, "WorkbookTables", false));
        factories.put(AppPanelId.SUPPLIES, () -> new SuppliesWorkbookPanel(database));
        factories.put(AppPanelId.TRANSACTION_EDITOR, () -> new SimpleInfoPanel("Transaction Editor", "Real accounting-record editor", "Next slice: enter a transaction header and balanced transaction lines while preserving the spreadsheet-like Ledger data-entry flow."));
        factories.put(AppPanelId.TRANSACTIONS_LIST, () -> new QueryTablePanel(database, "Transactions List", "transactions_list_view"));
        factories.put(AppPanelId.ALL_CHECKS_TFRS, () -> new QueryTablePanel(database, "All Checks & Transfers", "all_checks_tfrs_view"));
        factories.put(AppPanelId.FUND_TRANSFERS, () -> new QueryTablePanel(database, "Fund Transfers", "fund_transfers_view"));
        factories.put(AppPanelId.BALANCE_STMT, () -> new WorkbookReportPanel(database, "BalanceStmt", "Balance Statement", "balance_stmt_view"));
        factories.put(AppPanelId.INCOME_STMT, () -> new WorkbookReportPanel(database, "IncomeStmt", "Income Statement", "income_stmt_view"));
        factories.put(AppPanelId.CHART_OF_ACCOUNTS, () -> new ReferenceTablePanel(database, "Chart of Accounts", "accounts"));
        factories.put(AppPanelId.FUNDS, () -> new ReferenceTablePanel(database, "Funds", "funds"));
        factories.put(AppPanelId.BUDGET_CATEGORIES, () -> new ReferenceTablePanel(database, "Budget Categories", "budget_categories"));
        factories.put(AppPanelId.BANK_ACCOUNTS, () -> new ReferenceTablePanel(database, "Bank Accounts", "bank_accounts"));
        factories.put(AppPanelId.BANKING, () -> new ReferenceTablePanel(database, "Banking / Statement Lines", "bank_statement_lines"));
        factories.put(AppPanelId.PERIOD_CLOSE, () -> new ReferenceTablePanel(database, "Period Close", "period_close_records"));
        factories.put(AppPanelId.SETTINGS, () -> new SimpleInfoPanel("Settings", "Prototype settings", "Configuration will move here as company profiles, database selection, and workbook export options are added."));
        factories.put(AppPanelId.HELP, () -> new SimpleInfoPanel("Help", "Workbook-to-application model", "The spreadsheet is the familiar data-entry and report reference. The database stores real accounting records and the panes render workbook-like views."));
    }

    public void show(AppPanelId id) {
        AppPanel panel = panels.computeIfAbsent(id, this::create);
        activeId = id;
        setCenter(panel.root());
        panel.onRefresh();
    }

    public AppPanelId activePanelId() {
        return activeId;
    }

    public String activeTitle() {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        return panel == null ? "(none)" : panel.title();
    }

    public void refreshActive() {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        if (panel != null) {
            panel.onRefresh();
        }
    }

    public void saveActive() {
        AppPanel panel = activeId == null ? null : panels.get(activeId);
        if (panel != null) {
            panel.onSave();
        }
    }

    private AppPanel create(AppPanelId id) {
        Supplier<AppPanel> factory = factories.get(id);
        if (factory == null) {
            throw new IllegalArgumentException("Unsupported panel id: " + id);
        }
        return factory.get();
    }
}
