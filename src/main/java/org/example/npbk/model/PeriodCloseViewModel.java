package org.example.npbk.model;

import java.time.LocalDate;
import javafx.beans.property.*;

public class PeriodCloseViewModel extends BaseRowViewModel {
    private final ObjectProperty<LocalDate> periodStart = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> periodEnd = new SimpleObjectProperty<>();
    private final BooleanProperty bankReconciled = new SimpleBooleanProperty(false);
    private final BooleanProperty schedulesReviewed = new SimpleBooleanProperty(false);
    private final BooleanProperty reportsGenerated = new SimpleBooleanProperty(false);
    private final ObjectProperty<PeriodCloseStatus> closeStatus = new SimpleObjectProperty<>(PeriodCloseStatus.DRAFT);
    private final StringProperty notes = new SimpleStringProperty("");

    public PeriodCloseViewModel() { markDirtyOnChange(periodStart, periodEnd, bankReconciled, schedulesReviewed, reportsGenerated, closeStatus, notes); }
    public ObjectProperty<LocalDate> periodStartProperty() { return periodStart; }
    public LocalDate getPeriodStart() { return periodStart.get(); }
    public void setPeriodStart(LocalDate v) { periodStart.set(v); }
    public ObjectProperty<LocalDate> periodEndProperty() { return periodEnd; }
    public LocalDate getPeriodEnd() { return periodEnd.get(); }
    public void setPeriodEnd(LocalDate v) { periodEnd.set(v); }
    public BooleanProperty bankReconciledProperty() { return bankReconciled; }
    public boolean isBankReconciled() { return bankReconciled.get(); }
    public void setBankReconciled(boolean v) { bankReconciled.set(v); }
    public BooleanProperty schedulesReviewedProperty() { return schedulesReviewed; }
    public boolean isSchedulesReviewed() { return schedulesReviewed.get(); }
    public void setSchedulesReviewed(boolean v) { schedulesReviewed.set(v); }
    public BooleanProperty reportsGeneratedProperty() { return reportsGenerated; }
    public boolean isReportsGenerated() { return reportsGenerated.get(); }
    public void setReportsGenerated(boolean v) { reportsGenerated.set(v); }
    public ObjectProperty<PeriodCloseStatus> closeStatusProperty() { return closeStatus; }
    public PeriodCloseStatus getCloseStatus() { return closeStatus.get(); }
    public void setCloseStatus(PeriodCloseStatus v) { closeStatus.set(v == null ? PeriodCloseStatus.DRAFT : v); }
    public StringProperty notesProperty() { return notes; }
    public String getNotes() { return notes.get(); }
    public void setNotes(String v) { notes.set(v == null ? "" : v); }
}
