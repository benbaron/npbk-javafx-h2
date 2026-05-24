package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LedgerRowViewModel {
    private final ObjectProperty<Long> id = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> transactionDate = new SimpleObjectProperty<>();
    private final StringProperty referenceNumber = new SimpleStringProperty("");
    private final StringProperty legalName = new SimpleStringProperty("");
    private final StringProperty details = new SimpleStringProperty("");
    private final StringProperty bankAccount = new SimpleStringProperty("Checking");
    private final ObjectProperty<BankTiming> affectsBank = new SimpleObjectProperty<>(BankTiming.NOW);
    private final StringProperty budgetCategory = new SimpleStringProperty("");
    private final ObjectProperty<BudgetTiming> affectsBudget = new SimpleObjectProperty<>(BudgetTiming.NOW);
    private final StringProperty fund = new SimpleStringProperty("General Fund");
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> bankEffect = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> budgetEffect = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<ValidationState> validationState = new SimpleObjectProperty<>(ValidationState.OK);
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final ObservableList<String> validationMessages = FXCollections.observableArrayList();

    public LedgerRowViewModel() { installDirtyListeners(); }
    private void installDirtyListeners() {
        var listener = (javafx.beans.value.ChangeListener<Object>) (obs, oldVal, newVal) -> dirty.set(true);
        transactionDate.addListener(listener); referenceNumber.addListener(listener); legalName.addListener(listener);
        details.addListener(listener); bankAccount.addListener(listener); affectsBank.addListener(listener);
        budgetCategory.addListener(listener); affectsBudget.addListener(listener); fund.addListener(listener); amount.addListener(listener);
    }
    public ObjectProperty<Long> idProperty() { return id; }
    public Long getId() { return id.get(); }
    public void setId(Long value) { id.set(value); }
    public ObjectProperty<LocalDate> transactionDateProperty() { return transactionDate; }
    public LocalDate getTransactionDate() { return transactionDate.get(); }
    public void setTransactionDate(LocalDate value) { transactionDate.set(value); }
    public StringProperty referenceNumberProperty() { return referenceNumber; }
    public String getReferenceNumber() { return referenceNumber.get(); }
    public void setReferenceNumber(String value) { referenceNumber.set(value == null ? "" : value); }
    public StringProperty legalNameProperty() { return legalName; }
    public String getLegalName() { return legalName.get(); }
    public void setLegalName(String value) { legalName.set(value == null ? "" : value); }
    public StringProperty detailsProperty() { return details; }
    public String getDetails() { return details.get(); }
    public void setDetails(String value) { details.set(value == null ? "" : value); }
    public StringProperty bankAccountProperty() { return bankAccount; }
    public String getBankAccount() { return bankAccount.get(); }
    public void setBankAccount(String value) { bankAccount.set(value == null ? "" : value); }
    public ObjectProperty<BankTiming> affectsBankProperty() { return affectsBank; }
    public BankTiming getAffectsBank() { return affectsBank.get(); }
    public void setAffectsBank(BankTiming value) { affectsBank.set(value == null ? BankTiming.NOW : value); }
    public StringProperty budgetCategoryProperty() { return budgetCategory; }
    public String getBudgetCategory() { return budgetCategory.get(); }
    public void setBudgetCategory(String value) { budgetCategory.set(value == null ? "" : value); }
    public ObjectProperty<BudgetTiming> affectsBudgetProperty() { return affectsBudget; }
    public BudgetTiming getAffectsBudget() { return affectsBudget.get(); }
    public void setAffectsBudget(BudgetTiming value) { affectsBudget.set(value == null ? BudgetTiming.NOW : value); }
    public StringProperty fundProperty() { return fund; }
    public String getFund() { return fund.get(); }
    public void setFund(String value) { fund.set(value == null ? "" : value); }
    public ObjectProperty<BigDecimal> amountProperty() { return amount; }
    public BigDecimal getAmount() { return amount.get() == null ? BigDecimal.ZERO : amount.get(); }
    public void setAmount(BigDecimal value) { amount.set(value == null ? BigDecimal.ZERO : value); }
    public ObjectProperty<BigDecimal> bankEffectProperty() { return bankEffect; }
    public BigDecimal getBankEffect() { return bankEffect.get(); }
    public void setBankEffect(BigDecimal value) { bankEffect.set(value == null ? BigDecimal.ZERO : value); }
    public ObjectProperty<BigDecimal> budgetEffectProperty() { return budgetEffect; }
    public BigDecimal getBudgetEffect() { return budgetEffect.get(); }
    public void setBudgetEffect(BigDecimal value) { budgetEffect.set(value == null ? BigDecimal.ZERO : value); }
    public ObjectProperty<ValidationState> validationStateProperty() { return validationState; }
    public ValidationState getValidationState() { return validationState.get(); }
    public void setValidationState(ValidationState value) { validationState.set(value == null ? ValidationState.OK : value); }
    public BooleanProperty dirtyProperty() { return dirty; }
    public boolean isDirty() { return dirty.get(); }
    public void setDirty(boolean value) { dirty.set(value); }
    public ObservableList<String> getValidationMessages() { return validationMessages; }
}
