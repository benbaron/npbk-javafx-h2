package org.example.npbk.model;

public enum OutstandingStatus {
    OPEN("Open"), CLEARED("Cleared"), VOIDED("Voided"), REVERSED("Reversed");
    private final String label;
    OutstandingStatus(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
