package org.example.npbk.report;

import java.time.LocalDate;

/** Runtime context used when calculating report values from H2. */
public record ReportContext(
        LocalDate periodStart,
        LocalDate periodEnd,
        String organizationName
) {
    public static ReportContext currentYearToDate() {
        LocalDate today = LocalDate.now();
        return new ReportContext(LocalDate.of(today.getYear(), 1, 1), today, "Organization");
    }
}
