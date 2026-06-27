package org.example.npbk.model;

import java.math.BigDecimal;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Editable debit/credit line backed by transaction_lines. */
public class TransactionLineViewModel
{
    private final ObjectProperty<Long> id = new SimpleObjectProperty<>();
    private final ObjectProperty<LookupOption> account = new SimpleObjectProperty<>();
    private final ObjectProperty<LookupOption> fund = new SimpleObjectProperty<>();
    private final ObjectProperty<LookupOption> budgetCategory = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> debitAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> creditAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty memo = new SimpleStringProperty("");
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    public TransactionLineViewModel()
    {
        installDirtyListeners();
    }

    private void installDirtyListeners()
    {
        var listener = (javafx.beans.value.ChangeListener<Object>) (obs, oldValue, newValue) -> dirty.set(true);
        account.addListener(listener);
        fund.addListener(listener);
        budgetCategory.addListener(listener);
        debitAmount.addListener(listener);
        creditAmount.addListener(listener);
        memo.addListener(listener);
    }

    public ObjectProperty<Long> idProperty()
    {
        return id;
    }

    public Long getId()
    {
        return id.get();
    }

    public void setId(Long value)
    {
        id.set(value);
    }

    public ObjectProperty<LookupOption> accountProperty()
    {
        return account;
    }

    public LookupOption getAccount()
    {
        return account.get();
    }

    public void setAccount(LookupOption value)
    {
        account.set(value);
    }

    public ObjectProperty<LookupOption> fundProperty()
    {
        return fund;
    }

    public LookupOption getFund()
    {
        return fund.get();
    }

    public void setFund(LookupOption value)
    {
        fund.set(value);
    }

    public ObjectProperty<LookupOption> budgetCategoryProperty()
    {
        return budgetCategory;
    }

    public LookupOption getBudgetCategory()
    {
        return budgetCategory.get();
    }

    public void setBudgetCategory(LookupOption value)
    {
        budgetCategory.set(value);
    }

    public ObjectProperty<BigDecimal> debitAmountProperty()
    {
        return debitAmount;
    }

    public BigDecimal getDebitAmount()
    {
        return nonNull(debitAmount.get());
    }

    public void setDebitAmount(BigDecimal value)
    {
        debitAmount.set(nonNull(value));
    }

    public ObjectProperty<BigDecimal> creditAmountProperty()
    {
        return creditAmount;
    }

    public BigDecimal getCreditAmount()
    {
        return nonNull(creditAmount.get());
    }

    public void setCreditAmount(BigDecimal value)
    {
        creditAmount.set(nonNull(value));
    }

    public StringProperty memoProperty()
    {
        return memo;
    }

    public String getMemo()
    {
        return memo.get();
    }

    public void setMemo(String value)
    {
        memo.set(value == null ? "" : value);
    }

    public boolean isDirty()
    {
        return dirty.get();
    }

    public void setDirty(boolean value)
    {
        dirty.set(value);
    }

    public void markClean()
    {
        setDirty(false);
    }

    private BigDecimal nonNull(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
