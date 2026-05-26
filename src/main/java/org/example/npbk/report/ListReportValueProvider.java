package org.example.npbk.report;

import java.sql.Date;

import org.example.npbk.db.Database;

/** H2-backed rows for workbook list/report pages. */
public class ListReportValueProvider implements ReportValueProvider {
    private final String templateId;
    private final SqlReportSupport sql;

    public ListReportValueProvider(Database database, String templateId) {
        this.templateId = templateId;
        this.sql = new SqlReportSupport(database);
    }

    @Override
    public ReportValueSet loadValues(ReportContext context) {
        ReportValueSet values = new ReportValueSet();
        switch (templateId) {
            case "TransactionsList" -> values.putTable("transactionsList.rows", rows("transactions_list_view", context));
            case "AllChecksTfrs" -> values.putTable("allChecksTfrs.rows", rows("all_checks_tfrs_view", context));
            case "FundTransfers" -> values.putTable("fundTransfers.rows", rows("fund_transfers_view", context));
            default -> { }
        }
        return values;
    }

    private java.util.List<java.util.Map<String, Object>> rows(String viewName, ReportContext context) {
        String sqlText = "SELECT * FROM " + viewName + " WHERE transaction_date BETWEEN ? AND ? ORDER BY transaction_date, transaction_id LIMIT 500";
        return sql.rows(sqlText, Date.valueOf(context.periodStart()), Date.valueOf(context.periodEnd()));
    }
}
