package org.example.npbk.report;

import org.example.npbk.db.Database;

public class ReportValueProviderRegistry {
    private final Database database;

    public ReportValueProviderRegistry(Database database) {
        this.database = database;
    }

    public ReportValueProvider providerFor(String templateId) {
        return switch (templateId) {
            case "BalanceStmt" -> new BalanceStatementValueProvider(database);
            case "IncomeStmt" -> new IncomeStatementValueProvider(database);
            case "WorkbookSummary" -> new WorkbookSummaryValueProvider(database);
            case "TransactionsList", "AllChecksTfrs", "FundTransfers" -> new ListReportValueProvider(database, templateId);
            default -> context -> new ReportValueSet();
        };
    }
}
