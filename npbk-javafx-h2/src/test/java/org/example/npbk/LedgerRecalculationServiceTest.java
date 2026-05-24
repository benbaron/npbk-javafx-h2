package org.example.npbk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.example.npbk.model.BankTiming;
import org.example.npbk.model.BudgetTiming;
import org.example.npbk.model.LedgerRowViewModel;
import org.example.npbk.service.LedgerRecalculationService;
import org.example.npbk.validation.LedgerValidationService;
import org.junit.jupiter.api.Test;

class LedgerRecalculationServiceTest {
    @Test
    void nowTimingCopiesAmountIntoEffects() {
        LedgerRowViewModel row = new LedgerRowViewModel();
        row.setAmount(new BigDecimal("123.45"));
        row.setAffectsBank(BankTiming.NOW);
        row.setAffectsBudget(BudgetTiming.NOW);
        new LedgerRecalculationService(new LedgerValidationService()).recalculate(row);
        assertEquals(new BigDecimal("123.45"), row.getBankEffect());
        assertEquals(new BigDecimal("123.45"), row.getBudgetEffect());
    }

    @Test
    void nonNowTimingZerosEffects() {
        LedgerRowViewModel row = new LedgerRowViewModel();
        row.setAmount(new BigDecimal("123.45"));
        row.setAffectsBank(BankTiming.PREVIOUSLY);
        row.setAffectsBudget(BudgetTiming.FUTURE);
        new LedgerRecalculationService(new LedgerValidationService()).recalculate(row);
        assertEquals(BigDecimal.ZERO, row.getBankEffect());
        assertEquals(BigDecimal.ZERO, row.getBudgetEffect());
    }
}
