package org.example.npbk.service;

import java.util.List;

import org.example.npbk.model.LookupOption;
import org.example.npbk.model.TransactionEditorViewModel;
import org.example.npbk.repo.NormalizedTransactionRepository;
import org.example.npbk.validation.NormalizedTransactionValidationService;

/** Coordinates loading, validation, and persistence for normalized transactions. */
public class NormalizedTransactionService
{
    private final NormalizedTransactionRepository repository;
    private final NormalizedTransactionValidationService validator;

    public NormalizedTransactionService(
        NormalizedTransactionRepository repository,
        NormalizedTransactionValidationService validator)
    {
        this.repository = repository;
        this.validator = validator;
    }

    public List<LookupOption> transactionChoices()
    {
        return repository.findTransactionChoices();
    }

    public TransactionEditorViewModel newTransaction()
    {
        return TransactionEditorViewModel.blank();
    }

    public TransactionEditorViewModel load(long transactionId)
    {
        return repository.findById(transactionId);
    }

    public SaveResult save(TransactionEditorViewModel model)
    {
        List<String> messages = validator.validate(model);
        model.getValidationMessages().setAll(messages);
        if (!messages.isEmpty())
            return new SaveResult(false, model, messages);
        repository.save(model);
        return new SaveResult(true, model, List.of("Saved balanced transaction."));
    }

    public record SaveResult(
        boolean saved,
        TransactionEditorViewModel transaction,
        List<String> messages)
    {
    }
}
