package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import javafx.beans.property.*;

public class InventoryItemViewModel extends BaseRowViewModel {
    private final StringProperty itemNumber = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> acquiredDate = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> quantity = new SimpleObjectProperty<>(BigDecimal.ONE);
    private final ObjectProperty<BigDecimal> totalValue = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty itemType = new SimpleStringProperty("");
    private final StringProperty usedFor = new SimpleStringProperty("");
    private final StringProperty guardianName = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> confirmedDate = new SimpleObjectProperty<>();
    private final StringProperty notes = new SimpleStringProperty("");

    public InventoryItemViewModel() { markDirtyOnChange(itemNumber, acquiredDate, description, quantity, totalValue, itemType, usedFor, guardianName, confirmedDate, notes); }
    public StringProperty itemNumberProperty() { return itemNumber; }
    public String getItemNumber() { return itemNumber.get(); }
    public void setItemNumber(String v) { itemNumber.set(v == null ? "" : v); }
    public ObjectProperty<LocalDate> acquiredDateProperty() { return acquiredDate; }
    public LocalDate getAcquiredDate() { return acquiredDate.get(); }
    public void setAcquiredDate(LocalDate v) { acquiredDate.set(v); }
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v == null ? "" : v); }
    public ObjectProperty<BigDecimal> quantityProperty() { return quantity; }
    public BigDecimal getQuantity() { return quantity.get() == null ? BigDecimal.ZERO : quantity.get(); }
    public void setQuantity(BigDecimal v) { quantity.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> totalValueProperty() { return totalValue; }
    public BigDecimal getTotalValue() { return totalValue.get() == null ? BigDecimal.ZERO : totalValue.get(); }
    public void setTotalValue(BigDecimal v) { totalValue.set(v == null ? BigDecimal.ZERO : v); }
    public StringProperty itemTypeProperty() { return itemType; }
    public String getItemType() { return itemType.get(); }
    public void setItemType(String v) { itemType.set(v == null ? "" : v); }
    public StringProperty usedForProperty() { return usedFor; }
    public String getUsedFor() { return usedFor.get(); }
    public void setUsedFor(String v) { usedFor.set(v == null ? "" : v); }
    public StringProperty guardianNameProperty() { return guardianName; }
    public String getGuardianName() { return guardianName.get(); }
    public void setGuardianName(String v) { guardianName.set(v == null ? "" : v); }
    public ObjectProperty<LocalDate> confirmedDateProperty() { return confirmedDate; }
    public LocalDate getConfirmedDate() { return confirmedDate.get(); }
    public void setConfirmedDate(LocalDate v) { confirmedDate.set(v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
