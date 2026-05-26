package org.example.npbk.report;

import org.example.npbk.db.Database;

/** Combines high-level values used by the WorkbookSummary template. */
public class WorkbookSummaryValueProvider implements ReportValueProvider {
    private final Database database;

    public WorkbookSummaryValueProvider(Database database) {
        this.database = database;
    }

    @Override
    public ReportValueSet loadValues(ReportContext context) {
        ReportValueSet values = new ReportValueSet();
        values.put("context.organizationName", context.organizationName());
        values.put("context.periodStart", context.periodStart());
        values.put("context.periodEnd", context.periodEnd());

        merge(values, new BalanceStatementValueProvider(database).loadValues(context));
        merge(values, new IncomeStatementValueProvider(database).loadValues(context));
        return values;
    }

    private void merge(ReportValueSet target, ReportValueSet source) {
        source.scalars().forEach(target::put);
        source.tables().forEach(target::putTable);
    }
}
