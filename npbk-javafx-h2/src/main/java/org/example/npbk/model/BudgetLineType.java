package org.example.npbk.model;

public enum BudgetLineType {
    INCOME("Income"), EXPENSE("Expense");
    private final String label;
    BudgetLineType(String label) { this.label = label; }
    @Override public String toString() { return label; }
}
