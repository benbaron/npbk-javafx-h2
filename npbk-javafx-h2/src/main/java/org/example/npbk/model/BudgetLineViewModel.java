package org.example.npbk.model;

import java.math.BigDecimal;
import javafx.beans.property.*;

public class BudgetLineViewModel extends BaseRowViewModel {
    private final StringProperty fund = new SimpleStringProperty("General Fund");
    private final StringProperty budgetCategory = new SimpleStringProperty("");
    private final ObjectProperty<BudgetLineType> lineType = new SimpleObjectProperty<>(BudgetLineType.EXPENSE);
    private final ObjectProperty<BigDecimal> plannedAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> actualAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> varianceAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty notes = new SimpleStringProperty("");

    public BudgetLineViewModel() { markDirtyOnChange(fund, budgetCategory, lineType, plannedAmount, notes); }
    public StringProperty fundProperty() { return fund; }
    public String getFund() { return fund.get(); }
    public void setFund(String v) { fund.set(v == null ? "" : v); }
    public StringProperty budgetCategoryProperty() { return budgetCategory; }
    public String getBudgetCategory() { return budgetCategory.get(); }
    public void setBudgetCategory(String v) { budgetCategory.set(v == null ? "" : v); }
    public ObjectProperty<BudgetLineType> lineTypeProperty() { return lineType; }
    public BudgetLineType getLineType() { return lineType.get(); }
    public void setLineType(BudgetLineType v) { lineType.set(v == null ? BudgetLineType.EXPENSE : v); }
    public ObjectProperty<BigDecimal> plannedAmountProperty() { return plannedAmount; }
    public BigDecimal getPlannedAmount() { return plannedAmount.get() == null ? BigDecimal.ZERO : plannedAmount.get(); }
    public void setPlannedAmount(BigDecimal v) { plannedAmount.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> actualAmountProperty() { return actualAmount; }
    public BigDecimal getActualAmount() { return actualAmount.get() == null ? BigDecimal.ZERO : actualAmount.get(); }
    public void setActualAmount(BigDecimal v) { actualAmount.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> varianceAmountProperty() { return varianceAmount; }
    public BigDecimal getVarianceAmount() { return varianceAmount.get() == null ? BigDecimal.ZERO : varianceAmount.get(); }
    public void setVarianceAmount(BigDecimal v) { varianceAmount.set(v == null ? BigDecimal.ZERO : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
