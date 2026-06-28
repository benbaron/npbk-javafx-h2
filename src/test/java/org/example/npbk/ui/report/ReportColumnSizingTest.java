package org.example.npbk.ui.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ReportColumnSizingTest
{
    private static final double EPSILON = 0.0001;

    @Test
    void sectionReportMinimumWidthFitsNormalCenterViewport()
    {
        ReportColumnSizing.ColumnSize line = ReportColumnSizing.sectionColumn(80.0);
        ReportColumnSizing.ColumnSize description = ReportColumnSizing.sectionColumn(420.0);
        ReportColumnSizing.ColumnSize amount = ReportColumnSizing.sectionColumn(140.0);
        ReportColumnSizing.ColumnSize notes = ReportColumnSizing.sectionColumn(280.0);

        double totalMinimum = ReportColumnSizing.totalMinimumWidth(
            line,
            description,
            amount,
            notes);

        assertTrue(totalMinimum <= 500.0, "Section report should fit a 500px center viewport");
        assertTrue(description.minimumWidth() < description.preferredWidth());
        assertTrue(notes.minimumWidth() < notes.preferredWidth());
    }

    @Test
    void typicalTransactionListColumnsRemainCompact()
    {
        ReportColumnSizing.ColumnSize date = ReportColumnSizing.tableColumn("Date", "transaction_date");
        ReportColumnSizing.ColumnSize reference = ReportColumnSizing.tableColumn("Reference", "reference_number");
        ReportColumnSizing.ColumnSize bank = ReportColumnSizing.tableColumn("Bank Account", "bank_account");
        ReportColumnSizing.ColumnSize legalName = ReportColumnSizing.tableColumn("Legal Name", "legal_name");
        ReportColumnSizing.ColumnSize amount = ReportColumnSizing.tableColumn("Amount", "amount");

        double totalMinimum = ReportColumnSizing.totalMinimumWidth(
            date,
            reference,
            bank,
            legalName,
            amount);

        assertTrue(totalMinimum <= 500.0, "Five common report columns should fit a normal center viewport");
    }

    @Test
    void extremelyLongColumnNamesAreCapped()
    {
        ReportColumnSizing.ColumnSize size = ReportColumnSizing.tableColumn(
            "An Extremely Long Workbook Column Heading That Must Not Force The Whole Window Wide",
            "an_extremely_long_database_field_name_that_should_also_be_capped");

        assertEquals(220.0, size.preferredWidth(), EPSILON);
        assertTrue(size.minimumWidth() < size.preferredWidth());
    }

    @Test
    void rejectsInvalidSectionPreferredWidth()
    {
        assertThrows(
            IllegalArgumentException.class,
            () -> ReportColumnSizing.sectionColumn(0.0));
        assertThrows(
            IllegalArgumentException.class,
            () -> ReportColumnSizing.sectionColumn(Double.NaN));
    }
}
