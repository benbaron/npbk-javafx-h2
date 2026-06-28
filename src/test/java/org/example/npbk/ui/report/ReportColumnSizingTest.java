package org.example.npbk.ui.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class ReportColumnSizingTest
{
    private static final double EPSILON = 0.0001;

    @Test
    void percentagesAlwaysSumToOneHundred()
    {
        List<Double> widths = ReportColumnSizing.percentages(
            List.of("Date", "Reference", "Bank Account", "Legal Name", "Amount"),
            List.of("date", "reference", "bankAccount", "legalName", "amount"));

        assertEquals(5, widths.size());
        assertEquals(100.0, widths.stream().mapToDouble(Double::doubleValue).sum(), EPSILON);
        assertTrue(widths.stream().allMatch(width -> width > 0.0));
    }

    @Test
    void longerColumnsReceiveMoreSpaceWithoutDominating()
    {
        List<Double> widths = ReportColumnSizing.percentages(
            List.of("ID", "Very Long Description Column", "Amount"),
            List.of("id", "description", "amount"));

        assertTrue(widths.get(1) > widths.get(0));
        assertTrue(widths.get(1) < 80.0);
        assertEquals(100.0, widths.stream().mapToDouble(Double::doubleValue).sum(), EPSILON);
    }

    @Test
    void emptyInputProducesNoColumns()
    {
        assertTrue(ReportColumnSizing.percentages(List.of(), List.of()).isEmpty());
    }
}
