package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/** Transaction header plus balanced debit/credit lines. */
public class TransactionEditorViewModel
{
    private final ObjectProperty<Long> id = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> transactionDate = new SimpleObjectProperty<>(LocalDate.now());
    private final ObjectProperty<LocalDate> postingDate = new SimpleObjectProperty<>();
    private final StringProperty referenceNumber = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty legalName = new SimpleStringProperty("");
    private final ObjectProperty<LookupOption> bankAccount = new SimpleObjectProperty<>();
    private final ObjectProperty<BankTiming> affectsBank = new SimpleObjectProperty<>(BankTiming.NOW);
    private final ObjectProperty<BudgetTiming> affectsBudget = new SimpleObjectProperty<>(BudgetTiming.NOW);
    private final ObservableList<TransactionLineViewModel> lines = FXCollections.observableArrayList();
    private final ObservableList<String> validationMessages = FXCollections.observableArrayList();
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);

    public TransactionEditorViewModel()
    {
        installDirtyListeners();
    }

    public static TransactionEditorViewModel blank()
    {
        TransactionEditorViewModel model = new TransactionEditorViewModel();
        model.getLines().addAll(new TransactionLineViewModel(), new TransactionLineViewModel());
        model.markClean();
        return model;
    }

    private void installDirtyListeners()
    {
        var listener = (javafx.beans.value.ChangeListener<Object>) (obs, oldValue, newValue) -> dirty.set(true);
        transactionDate.addListener(listener);
        postingDate.addListener(listener);
        referenceNumber.addListener(listener);
        description.addListener(listener);
        legalName.addListener(listener);
        bankAccount.addListener(listener);
        affectsBank.addListener(listener);
        affectsBudget.addListener(listener);
        lines.addListener((javafx.collections.ListChangeListener<TransactionLineViewModel>) change -> dirty.set(true));
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

    public ObjectProperty<LocalDate> transactionDateProperty()
    {
        return transactionDate;
    }

    public LocalDate getTransactionDate()
    {
        return transactionDate.get();
    }

    public void setTransactionDate(LocalDate value)
    {
        transactionDate.set(value);
    }

    public ObjectProperty<LocalDate> postingDateProperty()
    {
        return postingDate;
    }

    public LocalDate getPostingDate()
    {
        return postingDate.get();
    }

    public void setPostingDate(LocalDate value)
    {
        postingDate.set(value);
    }

    public StringProperty referenceNumberProperty()
    {
        return referenceNumber;
    }

    public String getReferenceNumber()
    {
        return referenceNumber.get();
    }

    public void setReferenceNumber(String value)
    {
        referenceNumber.set(value == null ? "" : value);
    }

    public StringProperty descriptionProperty()
    {
        return description;
    }

    public String getDescription()
    {
        return description.get();
    }

    public void setDescription(String value)
    {
        description.set(value == null ? "" : value);
    }

    public StringProperty legalNameProperty()
    {
        return legalName;
    }

    public String getLegalName()
    {
        return legalName.get();
    }

    public void setLegalName(String value)
    {
        legalName.set(value == null ? "" : value);
    }

    public ObjectProperty<LookupOption> bankAccountProperty()
    {
        return bankAccount;
    }

    public LookupOption getBankAccount()
    {
        return bankAccount.get();
    }

    public void setBankAccount(LookupOption value)
    {
        bankAccount.set(value);
    }

    public ObjectProperty<BankTiming> affectsBankProperty()
    {
        return affectsBank;
    }

    public BankTiming getAffectsBank()
    {
        return affectsBank.get();
    }

    public void setAffectsBank(BankTiming value)
    {
        affectsBank.set(value == null ? BankTiming.NOW : value);
    }

    public ObjectProperty<BudgetTiming> affectsBudgetProperty()
    {
        return affectsBudget;
    }

    public BudgetTiming getAffectsBudget()
    {
        return affectsBudget.get();
    }

    public void setAffectsBudget(BudgetTiming value)
    {
        affectsBudget.set(value == null ? BudgetTiming.NOW : value);
    }

    public ObservableList<TransactionLineViewModel> getLines()
    {
        return lines;
    }

    public ObservableList<String> getValidationMessages()
    {
        return validationMessages;
    }

    public boolean isDirty()
    {
        return dirty.get() || lines.stream().anyMatch(TransactionLineViewModel::isDirty);
    }

    public void setDirty(boolean value)
    {
        dirty.set(value);
    }

    public BigDecimal totalDebits()
    {
        return lines.stream().map(TransactionLineViewModel::getDebitAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal totalCredits()
    {
        return lines.stream().map(TransactionLineViewModel::getCreditAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal outOfBalance()
    {
        return totalDebits().subtract(totalCredits());
    }

    public void markClean()
    {
        dirty.set(false);
        lines.forEach(TransactionLineViewModel::markClean);
    }
}
