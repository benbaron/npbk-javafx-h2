package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.*;

public class BankStatementLineViewModel extends BaseRowViewModel {
    private final StringProperty bankAccount = new SimpleStringProperty("Checking");
    private final ObjectProperty<LocalDate> statementDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> postedDate = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BankStatementLineStatus> status = new SimpleObjectProperty<>(BankStatementLineStatus.IMPORTED);
    private final ObjectProperty<Long> matchedTransactionId = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty("");

    public BankStatementLineViewModel() { markDirtyOnChange(bankAccount, statementDate, postedDate, description, amount, status, matchedTransactionId, notes); }
    public StringProperty bankAccountProperty() { return bankAccount; }
    public String getBankAccount() { return bankAccount.get(); }
    public void setBankAccount(String v) { bankAccount.set(v == null ? "" : v); }
    public ObjectProperty<LocalDate> statementDateProperty() { return statementDate; }
    public LocalDate getStatementDate() { return statementDate.get(); }
    public void setStatementDate(LocalDate v) { statementDate.set(v); }
    public ObjectProperty<LocalDate> postedDateProperty() { return postedDate; }
    public LocalDate getPostedDate() { return postedDate.get(); }
    public void setPostedDate(LocalDate v) { postedDate.set(v); }
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v == null ? "" : v); }
    public ObjectProperty<BigDecimal> amountProperty() { return amount; }
    public BigDecimal getAmount() { return amount.get() == null ? BigDecimal.ZERO : amount.get(); }
    public void setAmount(BigDecimal v) { amount.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BankStatementLineStatus> statusProperty() { return status; }
    public BankStatementLineStatus getStatus() { return status.get(); }
    public void setStatus(BankStatementLineStatus v) { status.set(v == null ? BankStatementLineStatus.IMPORTED : v); }
    public ObjectProperty<Long> matchedTransactionIdProperty() { return matchedTransactionId; }
    public Long getMatchedTransactionId() { return matchedTransactionId.get(); }
    public void setMatchedTransactionId(Long v) { matchedTransactionId.set(v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
