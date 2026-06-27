package org.example.npbk.validation;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.model.TransactionEditorViewModel;
import org.example.npbk.model.TransactionLineViewModel;

/** Business validation for transaction headers and balanced split lines. */
public class NormalizedTransactionValidationService
{
    public List<String> validate(TransactionEditorViewModel model)
    {
        List<String> messages = new ArrayList<>();
        if (model.getTransactionDate() == null)
            messages.add("Transaction date is required.");
        if (model.getLegalName() == null || model.getLegalName().isBlank())
            messages.add("Legal name / payee is required.");
        if (model.getDescription() == null || model.getDescription().isBlank())
            messages.add("Description is required.");
        if (model.getBankAccount() == null || !model.getBankAccount().isPresent())
            messages.add("Bank account is required.");

        int meaningfulLines = 0;
        int displayLine = 0;
        for (TransactionLineViewModel line : model.getLines())
        {
            displayLine++;
            if (!isMeaningful(line))
                continue;
            meaningfulLines++;
            validateLine(line, displayLine, messages);
        }
        if (meaningfulLines < 2)
            messages.add("A transaction requires at least two accounting lines.");

        BigDecimal debits = model.totalDebits();
        BigDecimal credits = model.totalCredits();
        if (debits.compareTo(BigDecimal.ZERO) <= 0 || credits.compareTo(BigDecimal.ZERO) <= 0)
            messages.add("Total debits and total credits must both be greater than zero.");
        if (debits.compareTo(credits) != 0)
            messages.add("Transaction is out of balance by " + debits.subtract(credits).toPlainString() + ".");
        return messages;
    }

    private void validateLine(TransactionLineViewModel line, int displayLine, List<String> messages)
    {
        String prefix = "Line " + displayLine + ": ";
        if (line.getAccount() == null || !line.getAccount().isPresent())
            messages.add(prefix + "account is required.");
        if (line.getFund() == null || !line.getFund().isPresent())
            messages.add(prefix + "fund is required.");

        BigDecimal debit = line.getDebitAmount();
        BigDecimal credit = line.getCreditAmount();
        if (debit.compareTo(BigDecimal.ZERO) < 0 || credit.compareTo(BigDecimal.ZERO) < 0)
            messages.add(prefix + "debit and credit amounts cannot be negative.");
        boolean hasDebit = debit.compareTo(BigDecimal.ZERO) > 0;
        boolean hasCredit = credit.compareTo(BigDecimal.ZERO) > 0;
        if (hasDebit == hasCredit)
            messages.add(prefix + "enter either a debit or a credit, but not both.");
    }

    private boolean isMeaningful(TransactionLineViewModel line)
    {
        return line.getAccount() != null
            || line.getFund() != null
            || line.getBudgetCategory() != null
            || line.getDebitAmount().compareTo(BigDecimal.ZERO) != 0
            || line.getCreditAmount().compareTo(BigDecimal.ZERO) != 0
            || (line.getMemo() != null && !line.getMemo().isBlank());
    }
}
