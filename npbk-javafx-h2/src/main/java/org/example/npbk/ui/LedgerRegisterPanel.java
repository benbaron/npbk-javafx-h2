package org.example.npbk.ui;

import org.example.npbk.db.Database;
import org.example.npbk.repo.LookupRepository;
import org.example.npbk.repo.TransactionRepository;
import org.example.npbk.service.LedgerRecalculationService;
import org.example.npbk.service.TransactionService;
import org.example.npbk.validation.LedgerValidationService;

import javafx.scene.Node;

/** AppPanel wrapper for the spreadsheet-style ledger editor. */
public class LedgerRegisterPanel implements AppPanel {
    private final LedgerView ledgerView;

    public LedgerRegisterPanel(Database database) {
        LedgerValidationService validator = new LedgerValidationService();
        TransactionRepository transactionRepository = new TransactionRepository(database);
        this.ledgerView = new LedgerView(
                new LookupRepository(database),
                new TransactionService(transactionRepository, validator),
                new LedgerRecalculationService(validator)
        );
    }

    @Override
    public String title() {
        return "Transaction Editor";
    }

    @Override
    public Node root() {
        return ledgerView.getRoot();
    }
}
