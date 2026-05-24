package org.example.npbk.model;

public enum PeriodCloseStatus {
    DRAFT("Draft"), READY_TO_CLOSE("Ready to Close"), LOCKED("Locked");
    private final String label;
    PeriodCloseStatus(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
