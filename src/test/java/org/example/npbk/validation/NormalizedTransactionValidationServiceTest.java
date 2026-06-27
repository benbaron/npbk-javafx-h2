package org.example.npbk.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.npbk.model.LookupOption;
import org.example.npbk.model.TransactionEditorViewModel;
import org.junit.jupiter.api.Test;

class NormalizedTransactionValidationServiceTest
{
    private final NormalizedTransactionValidationService validator =
        new NormalizedTransactionValidationService();

    @Test
    void acceptsBalancedTransactionWithTwoFundedLines()
    {
        TransactionEditorViewModel model = validModel();

        List<String> messages = validator.validate(model);

        assertTrue(messages.isEmpty(), () -> String.join("\n", messages));
    }

    @Test
    void rejectsOutOfBalanceTransaction()
    {
        TransactionEditorViewModel model = validModel();
        model.getLines().get(1).setCreditAmount(new BigDecimal("90.00"));

        List<String> messages = validator.validate(model);

        assertFalse(messages.isEmpty());
        assertTrue(messages.stream().anyMatch(message -> message.contains("out of balance")));
    }

    private TransactionEditorViewModel validModel()
    {
        TransactionEditorViewModel model = TransactionEditorViewModel.blank();
        model.setTransactionDate(LocalDate.of(2026, 1, 15));
        model.setLegalName("Example Payee");
        model.setDescription("Example balanced transaction");
        model.setBankAccount(new LookupOption(1L, "Checking"));

        LookupOption generalFund = new LookupOption(1L, "General Fund");
        model.getLines().get(0).setAccount(new LookupOption(10L, "I.a — Cash"));
        model.getLines().get(0).setFund(generalFund);
        model.getLines().get(0).setDebitAmount(new BigDecimal("100.00"));

        model.getLines().get(1).setAccount(new LookupOption(20L, "2.0 — Donations"));
        model.getLines().get(1).setFund(generalFund);
        model.getLines().get(1).setCreditAmount(new BigDecimal("100.00"));
        return model;
    }
}
