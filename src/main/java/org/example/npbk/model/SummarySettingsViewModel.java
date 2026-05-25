package org.example.npbk.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import javafx.beans.property.*;

public class SummarySettingsViewModel extends BaseRowViewModel {
    private final StringProperty organizationName = new SimpleStringProperty("");
    private final StringProperty branchName = new SimpleStringProperty("");
    private final StringProperty kingdomName = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> periodStart = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> periodEnd = new SimpleObjectProperty<>();
    private final StringProperty preparedBy = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> bankTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> budgetTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> openOutstandingTotal = new SimpleObjectProperty<>(BigDecimal.ZERO);

    public SummarySettingsViewModel() {
        markDirtyOnChange(organizationName, branchName, kingdomName, periodStart, periodEnd, preparedBy, notes);
    }
    public StringProperty organizationNameProperty() { return organizationName; }
    public String getOrganizationName() { return organizationName.get(); }
    public void setOrganizationName(String v) { organizationName.set(v == null ? "" : v); }
    public StringProperty branchNameProperty() { return branchName; }
    public String getBranchName() { return branchName.get(); }
    public void setBranchName(String v) { branchName.set(v == null ? "" : v); }
    public StringProperty kingdomNameProperty() { return kingdomName; }
    public String getKingdomName() { return kingdomName.get(); }
    public void setKingdomName(String v) { kingdomName.set(v == null ? "" : v); }
    public ObjectProperty<LocalDate> periodStartProperty() { return periodStart; }
    public LocalDate getPeriodStart() { return periodStart.get(); }
    public void setPeriodStart(LocalDate v) { periodStart.set(v); }
    public ObjectProperty<LocalDate> periodEndProperty() { return periodEnd; }
    public LocalDate getPeriodEnd() { return periodEnd.get(); }
    public void setPeriodEnd(LocalDate v) { periodEnd.set(v); }
    public StringProperty preparedByProperty() { return preparedBy; }
    public String getPreparedBy() { return preparedBy.get(); }
    public void setPreparedBy(String v) { preparedBy.set(v == null ? "" : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
    public ObjectProperty<BigDecimal> bankTotalProperty() { return bankTotal; }
    public BigDecimal getBankTotal() { return bankTotal.get() == null ? BigDecimal.ZERO : bankTotal.get(); }
    public void setBankTotal(BigDecimal v) { bankTotal.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> budgetTotalProperty() { return budgetTotal; }
    public BigDecimal getBudgetTotal() { return budgetTotal.get() == null ? BigDecimal.ZERO : budgetTotal.get(); }
    public void setBudgetTotal(BigDecimal v) { budgetTotal.set(v == null ? BigDecimal.ZERO : v); }
    public ObjectProperty<BigDecimal> openOutstandingTotalProperty() { return openOutstandingTotal; }
    public BigDecimal getOpenOutstandingTotal() { return openOutstandingTotal.get() == null ? BigDecimal.ZERO : openOutstandingTotal.get(); }
    public void setOpenOutstandingTotal(BigDecimal v) { openOutstandingTotal.set(v == null ? BigDecimal.ZERO : v); }
}
