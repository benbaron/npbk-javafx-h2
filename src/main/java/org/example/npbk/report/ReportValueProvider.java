package org.example.npbk.report;

/** Supplies dynamic H2-backed values for a semantic report template. */
public interface ReportValueProvider {
    ReportValueSet loadValues(ReportContext context);
}
