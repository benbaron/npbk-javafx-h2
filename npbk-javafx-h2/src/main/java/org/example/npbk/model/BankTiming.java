package org.example.npbk.model;

public enum BankTiming {
    NOW("Now"), PREVIOUSLY("Previously"), FUTURE("Future");
    private final String label;
    BankTiming(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
