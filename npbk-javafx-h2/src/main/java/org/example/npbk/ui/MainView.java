package org.example.npbk.ui;

import org.example.npbk.db.Database;
import org.example.npbk.repo.*;
import org.example.npbk.service.*;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

public class MainView {
    private final BorderPane root = new BorderPane();

    public MainView(Database database) {
        LookupRepository lookups = new LookupRepository(database);
        TransactionRepository transactionRepository = new TransactionRepository(database);
        OutstandingRepository outstandingRepository = new OutstandingRepository(database);
        SupplementalLineRepository supplementalRepository = new SupplementalLineRepository(database);

        LedgerRecalculationService ledgerRecalculationService = new LedgerRecalculationService(new org.example.npbk.validation.LedgerValidationService());
        TransactionService transactionService = new TransactionService(transactionRepository, new org.example.npbk.validation.LedgerValidationService());
        SummaryService summaryService = new SummaryService(new SummaryRepository(database), transactionRepository, outstandingRepository);
        BudgetService budgetService = new BudgetService(new BudgetRepository(database), transactionRepository);
        OutstandingService outstandingService = new OutstandingService(outstandingRepository);
        SupplementalLineService supplementalLineService = new SupplementalLineService(supplementalRepository);
        InventoryService inventoryService = new InventoryService(new InventoryRepository(database));
        SupplyService supplyService = new SupplyService(new SupplyRepository(database));
        BankingService bankingService = new BankingService(new BankingRepository(database));
        PeriodCloseService periodCloseService = new PeriodCloseService(new PeriodCloseRepository(database), transactionRepository, outstandingRepository, supplementalRepository);

        TabPane tabs = new TabPane();
        tabs.getTabs().add(tab("Summary", new SummaryView(summaryService).getRoot()));
        tabs.getTabs().add(tab("Ledger", new LedgerView(lookups, transactionService, ledgerRecalculationService).getRoot()));
        tabs.getTabs().add(tab("Outstanding", new OutstandingView(lookups, outstandingService).getRoot()));
        tabs.getTabs().add(tab("Budget", new BudgetView(lookups, budgetService).getRoot()));
        tabs.getTabs().add(tab("Asset Details", new SupplementalLinesView("Asset Details", true, supplementalLineService).getRoot()));
        tabs.getTabs().add(tab("Liability Details", new SupplementalLinesView("Liability Details", false, supplementalLineService).getRoot()));
        tabs.getTabs().add(tab("Assets & Inventory", new InventoryView(lookups, inventoryService).getRoot()));
        tabs.getTabs().add(tab("Supplies", new SuppliesView(supplyService).getRoot()));
        tabs.getTabs().add(tab("Banking", new BankingView(lookups, bankingService).getRoot()));
        tabs.getTabs().add(tab("Period Close", new PeriodCloseView(periodCloseService).getRoot()));
        root.setCenter(tabs);
    }

    public Parent getRoot() { return root; }

    private Tab tab(String title, Node node) {
        Tab t = new Tab(title, node);
        t.setClosable(false);
        return t;
    }
}
