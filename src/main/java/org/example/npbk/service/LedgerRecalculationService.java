package org.example.npbk.service;

import java.math.BigDecimal;
import java.util.List;

import org.example.npbk.model.BankTiming;
import org.example.npbk.model.BudgetTiming;
import org.example.npbk.model.LedgerRowViewModel;
import org.example.npbk.validation.LedgerValidationService;

public class LedgerRecalculationService {
    private final LedgerValidationService validationService;
    public LedgerRecalculationService(LedgerValidationService validationService) { this.validationService = validationService; }

    public void recalculate(LedgerRowViewModel row) {
        BigDecimal amount = row.getAmount();
        row.setBankEffect(row.getAffectsBank() == BankTiming.NOW ? amount : BigDecimal.ZERO);
        row.setBudgetEffect(row.getAffectsBudget() == BudgetTiming.NOW ? amount : BigDecimal.ZERO);
        List<String> messages = validationService.validate(row);
        row.getValidationMessages().setAll(messages);
        row.setValidationState(validationService.stateFor(messages));
    }
}
