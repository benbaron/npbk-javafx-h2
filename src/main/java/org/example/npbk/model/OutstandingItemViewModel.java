package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.*;

public class OutstandingItemViewModel extends BaseRowViewModel {
    private final ObjectProperty<Long> sourceTransactionId = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> dateIssued = new SimpleObjectProperty<>();
    private final StringProperty referenceNumber = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty details = new SimpleStringProperty("");
    private final StringProperty bankAccount = new SimpleStringProperty("Checking");
    private final ObjectProperty<OutstandingDirection> direction = new SimpleObjectProperty<>(OutstandingDirection.OUTGOING);
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<LocalDate> clearedDate = new SimpleObjectProperty<>();
    private final ObjectProperty<OutstandingStatus> status = new SimpleObjectProperty<>(OutstandingStatus.OPEN);
    private final StringProperty notes = new SimpleStringProperty("");

    public OutstandingItemViewModel() { markDirtyOnChange(sourceTransactionId, dateIssued, referenceNumber, name, details, bankAccount, direction, amount, clearedDate, status, notes); }
    public ObjectProperty<Long> sourceTransactionIdProperty() { return sourceTransactionId; }
    public Long getSourceTransactionId() { return sourceTransactionId.get(); }
    public void setSourceTransactionId(Long v) { sourceTransactionId.set(v); }
    public ObjectProperty<LocalDate> dateIssuedProperty() { return dateIssued; }
    public LocalDate getDateIssued() { return dateIssued.get(); }
    public void setDateIssued(LocalDate v) { dateIssued.set(v); }
    public StringProperty referenceNumberProperty() { return referenceNumber; }
    public String getReferenceNumber() { return referenceNumber.get(); }
    public void setReferenceNumber(String v) { referenceNumber.set(v == null ? "" : v); }
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String v) { name.set(v == null ? "" : v); }
    public StringProperty detailsProperty() { return details; }
    public String getDetails() { return details.get(); }
    public void setDetails(String v) { details.set(v == null ? "" : v); }
    public StringProperty bankAccountProperty() { return bankAccount; }
    public String getBankAccount() { return bankAccount.get(); }
    public void setBankAccount(String v) { bankAccount.set(v == null ? "" : v); }
    public ObjectProperty<OutstandingDirection> directionProperty() { return direction; }
    public OutstandingDirection getDirection() { return direction.get(); }
    public void setDirection(OutstandingDirection v) { direction.set(v == null ? OutstandingDirection.OUTGOING : v); }
    public ObjectProperty<BigDecimal> amountProperty() { return amount; }
    public BigDecimal getAmount() { return amount.get() == null ? BigDecimal.ZERO : amount.get(); }
    public void setAmount(BigDecimal v) { amount.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<LocalDate> clearedDateProperty() { return clearedDate; }
    public LocalDate getClearedDate() { return clearedDate.get(); }
    public void setClearedDate(LocalDate v) { clearedDate.set(v); }
    public ObjectProperty<OutstandingStatus> statusProperty() { return status; }
    public OutstandingStatus getStatus() { return status.get(); }
    public void setStatus(OutstandingStatus v) { status.set(v == null ? OutstandingStatus.OPEN : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
