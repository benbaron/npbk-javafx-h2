package org.example.npbk.service;

import java.util.List;

import org.example.npbk.model.LedgerRowViewModel;
import org.example.npbk.model.ValidationState;
import org.example.npbk.repo.TransactionRepository;
import org.example.npbk.validation.LedgerValidationService;

public class TransactionService {
    private final TransactionRepository repository;
    private final LedgerValidationService validator;
    public TransactionService(TransactionRepository repository, LedgerValidationService validator) {
        this.repository = repository;
        this.validator = validator;
    }

    public List<LedgerRowViewModel> loadLedgerRows() { return repository.findAllLedgerRows(); }

    public SaveResult save(LedgerRowViewModel row) {
        List<String> messages = validator.validate(row);
        row.getValidationMessages().setAll(messages);
        row.setValidationState(validator.stateFor(messages));
        if (row.getValidationState() == ValidationState.ERROR) return new SaveResult(false, messages);
        repository.save(row);
        return new SaveResult(true, List.of("Saved."));
    }

    public record SaveResult(boolean saved, List<String> messages) {}
}
