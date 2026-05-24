package org.example.npbk.model;

public enum SupplementalLineKind {
    RECEIVABLE("Receivable"),
    PREPAID_EXPENSE("Prepaid Expense"),
    OTHER_ASSET("Other Asset"),
    DEFERRED_REVENUE("Deferred Revenue"),
    PAYABLE("Payable"),
    OTHER_LIABILITY("Other Liability");

    private final String label;
    SupplementalLineKind(String label) { this.label = label; }
    public boolean isAssetKind() {
        return this == RECEIVABLE || this == PREPAID_EXPENSE || this == OTHER_ASSET;
    }
    public boolean isLiabilityKind() { return !isAssetKind(); }
    @Override public String toString() { return label; }
}
