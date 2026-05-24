package org.example.npbk.model;

public enum BankStatementLineStatus {
    IMPORTED("Imported"), MATCHED("Matched"), RECONCILED("Reconciled"), IGNORED("Ignored");
    private final String label;
    BankStatementLineStatus(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
