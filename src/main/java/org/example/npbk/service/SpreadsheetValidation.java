package org.example.npbk.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.npbk.model.BaseRowViewModel;
import org.example.npbk.model.ValidationState;

public final class SpreadsheetValidation {
    private SpreadsheetValidation() {}
    public static boolean blank(String value) { return value == null || value.isBlank(); }
    public static void require(List<String> messages, String label, String value) { if (blank(value)) messages.add("ERROR: " + label + " is required."); }
    public static void require(List<String> messages, String label, LocalDate value) { if (value == null) messages.add("ERROR: " + label + " is required."); }
    public static void nonNegative(List<String> messages, String label, BigDecimal value) { if (value != null && value.signum() < 0) messages.add("ERROR: " + label + " must not be negative."); }
    public static void nonZero(List<String> messages, String label, BigDecimal value) { if (value == null || value.compareTo(BigDecimal.ZERO) == 0) messages.add("ERROR: " + label + " must not be zero."); }
    public static void startBeforeEnd(List<String> messages, LocalDate start, LocalDate end) { if (start != null && end != null && start.isAfter(end)) messages.add("ERROR: Start date must be on or before end date."); }
    public static ValidationState stateFor(List<String> messages) {
        return messages.stream().anyMatch(m -> m.startsWith("ERROR")) ? ValidationState.ERROR : messages.isEmpty() ? ValidationState.OK : ValidationState.WARNING;
    }
    public static List<String> apply(BaseRowViewModel row, List<String> messages) {
        row.getValidationMessages().setAll(messages);
        row.setValidationState(stateFor(messages));
        return messages;
    }
    public static List<String> okMessage() {
        List<String> messages = new ArrayList<>(); messages.add("OK: No validation issues."); return messages;
    }
}
