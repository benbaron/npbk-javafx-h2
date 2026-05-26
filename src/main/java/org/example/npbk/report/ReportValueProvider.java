package org.example.npbk.report;

/** Calculates report values from the database and other application state. */
public interface ReportValueProvider {
    ReportValueSet loadValues(ReportContext context);
}
