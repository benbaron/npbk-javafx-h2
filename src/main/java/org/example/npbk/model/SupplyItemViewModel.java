package org.example.npbk.model;

import java.math.BigDecimal;
import javafx.beans.property.*;

public class SupplyItemViewModel extends BaseRowViewModel {
    private final StringProperty description = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> quantity = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> unitCost = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> totalValue = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty usedFor = new SimpleStringProperty("");
    private final StringProperty location = new SimpleStringProperty("");
    private final StringProperty guardianName = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");

    public SupplyItemViewModel() { markDirtyOnChange(description, quantity, unitCost, usedFor, location, guardianName, notes); }
    public StringProperty descriptionProperty() { return description; }
    public String getDescription() { return description.get(); }
    public void setDescription(String v) { description.set(v == null ? "" : v); }
    public ObjectProperty<BigDecimal> quantityProperty() { return quantity; }
    public BigDecimal getQuantity() { return quantity.get() == null ? BigDecimal.ZERO : quantity.get(); }
    public void setQuantity(BigDecimal v) { quantity.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> unitCostProperty() { return unitCost; }
    public BigDecimal getUnitCost() { return unitCost.get() == null ? BigDecimal.ZERO : unitCost.get(); }
    public void setUnitCost(BigDecimal v) { unitCost.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> totalValueProperty() { return totalValue; }
    public BigDecimal getTotalValue() { return totalValue.get() == null ? BigDecimal.ZERO : totalValue.get(); }
    public void setTotalValue(BigDecimal v) { totalValue.set(v == null ? BigDecimal.ZERO : v); }
    public StringProperty usedForProperty() { return usedFor; }
    public String getUsedFor() { return usedFor.get(); }
    public void setUsedFor(String v) { usedFor.set(v == null ? "" : v); }
    public StringProperty locationProperty() { return location; }
    public String getLocation() { return location.get(); }
    public void setLocation(String v) { location.set(v == null ? "" : v); }
    public StringProperty guardianNameProperty() { return guardianName; }
    public String getGuardianName() { return guardianName.get(); }
    public void setGuardianName(String v) { guardianName.set(v == null ? "" : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
