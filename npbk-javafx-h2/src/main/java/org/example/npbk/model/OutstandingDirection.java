package org.example.npbk.model;

public enum OutstandingDirection {
    INCOMING("Incoming deposit/check"), OUTGOING("Outgoing check/transfer");
    private final String label;
    OutstandingDirection(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
