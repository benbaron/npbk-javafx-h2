package org.example.npbk.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.model.LedgerRowViewModel;
import org.example.npbk.model.ValidationState;

public class LedgerValidationService {
    public List<String> validate(LedgerRowViewModel row) {
        List<String> messages = new ArrayList<>();
        if (row.getTransactionDate() == null) messages.add("Date is required before saving.");
        if (row.getLegalName() == null || row.getLegalName().isBlank()) messages.add("Name / payee is required before saving.");
        if (row.getBankAccount() == null || row.getBankAccount().isBlank()) messages.add("Bank Account is required.");
        if (row.getFund() == null || row.getFund().isBlank()) messages.add("Fund is required.");
        if (row.getBudgetCategory() == null || row.getBudgetCategory().isBlank()) messages.add("Budget Category is required.");
        if (row.getAmount() == null || row.getAmount().compareTo(BigDecimal.ZERO) == 0) messages.add("Amount should not be zero.");
        return messages;
    }

    public ValidationState stateFor(List<String> messages) {
        return messages.isEmpty() ? ValidationState.OK : ValidationState.ERROR;
    }
}
