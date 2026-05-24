package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.*;

public class SupplementalLineViewModel extends BaseRowViewModel {
    private final ObjectProperty<Long> linkedTransactionId = new SimpleObjectProperty<>();
    private final ObjectProperty<SupplementalLineKind> lineKind = new SimpleObjectProperty<>(SupplementalLineKind.RECEIVABLE);
    private final StringProperty counterparty = new SimpleStringProperty("");
    private final StringProperty description = new SimpleStringProperty("");
    private final StringProperty reference = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> amount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<LocalDate> dueDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> startDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> endDate = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> remainingAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty notes = new SimpleStringProperty("");

    public SupplementalLineViewModel() { markDirtyOnChange(linkedTransactionId, lineKind, counterparty, description, reference, amount, dueDate, startDate, endDate, notes); }
    public ObjectProperty<Long> linkedTransactionIdProperty() { return linkedTransactionId; }
    public Long getLinkedTransactionId() { return linkedTransactionId.get(); }
    public void setLinkedTransactionId(Long v) { linkedTransactionId.set(v); }
    public ObjectProperty<SupplementalLineKind> lineKindProperty() { return lineKind; }
    public SupplementalLineKind getLineKind() { return lineKind.get(); }
    public void setLineKind(SupplementalLineKind v) { lineKind.set(v == null ? SupplementalLineKind.RECEIVABLE : v); }
    public StringProperty counterpartyProperty() { return counterparty; }
    public String getCounterparty() { return counterparty.get(); }
    public void setCounterparty(String v) { counterparty.set(v == null ? "" : v); }
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v == null ? "" : v); }
    public StringProperty referenceProperty() { return reference; }
    public String getReference() { return reference.get(); }
    public void setReference(String v) { reference.set(v == null ? "" : v); }
    public ObjectProperty<BigDecimal> amountProperty() { return amount; }
    public BigDecimal getAmount() { return amount.get() == null ? BigDecimal.ZERO : amount.get(); }
    public void setAmount(BigDecimal v) { amount.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<LocalDate> dueDateProperty() { return dueDate; }
    public LocalDate getDueDate() { return dueDate.get(); }
    public void setDueDate(LocalDate v) { dueDate.set(v); }
    public ObjectProperty<LocalDate> startDateProperty() { return startDate; }
    public LocalDate getStartDate() { return startDate.get(); }
    public void setStartDate(LocalDate v) { startDate.set(v); }
    public ObjectProperty<LocalDate> endDateProperty() { return endDate; }
    public LocalDate getEndDate() { return endDate.get(); }
    public void setEndDate(LocalDate v) { endDate.set(v); }
    public ObjectProperty<BigDecimal> remainingAmountProperty() { return remainingAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount.get() == null ? BigDecimal.ZERO : remainingAmount.get(); }
    public void setRemainingAmount(BigDecimal v) { remainingAmount.set(v == null ? BigDecimal.ZERO : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
