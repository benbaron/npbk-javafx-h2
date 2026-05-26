package org.example.npbk.report;

import java.math.BigDecimal;

import org.example.npbk.db.Database;

/** H2-backed values for the IncomeStmt semantic template. */
public class IncomeStatementValueProvider implements ReportValueProvider {
    private final SqlReportSupport sql;

    public IncomeStatementValueProvider(Database database) {
        this.sql = new SqlReportSupport(database);
    }

    @Override
    public ReportValueSet loadValues(ReportContext context) {
        ReportValueSet values = new ReportValueSet();
        put(values, "incomeStmt.income.fundraisingNonMedieval", sql.accountActivity("Fundraising: Non-medieval activities to earn income (car washes, bake sales, etc.)", context));
        put(values, "incomeStmt.income.directContributionsDonations", sql.accountActivity("Direct Contributions/Donations", context));
        put(values, "incomeStmt.income.activityRelated", sql.accountActivity("Activity Related: Medieval activities to earn income (events, demos, heraldry fees)", context));
        put(values, "incomeStmt.expenses.occupancyActivityRelated", sql.accountActivity("21b Occupancy - Activity Rel", context));

        BigDecimal totalIncome = sum(values,
                "incomeStmt.income.fundraisingNonMedieval",
                "incomeStmt.income.directContributionsDonations",
                "incomeStmt.income.activityRelated");
        BigDecimal totalExpenses = sum(values, "incomeStmt.expenses.occupancyActivityRelated");
        put(values, "incomeStmt.totalIncome", totalIncome);
        put(values, "incomeStmt.totalExpenses", totalExpenses);
        put(values, "incomeStmt.netIncomeLoss", totalIncome.subtract(totalExpenses));
        return values;
    }

    private void put(ReportValueSet values, String key, BigDecimal value) {
        values.put(key, value == null ? BigDecimal.ZERO : value);
    }

    private BigDecimal sum(ReportValueSet values, String... keys) {
        BigDecimal total = BigDecimal.ZERO;
        for (String key : keys) {
            Object v = values.get(key);
            total = total.add(v instanceof BigDecimal bd ? bd : BigDecimal.ZERO);
        }
        return total;
    }
}
