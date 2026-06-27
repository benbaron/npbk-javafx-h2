
package org.example.npbk.report;

import java.math.BigDecimal;

import org.example.npbk.db.Database;

/** H2-backed values for the BalanceStmt semantic template. */
public class BalanceStatementValueProvider implements ReportValueProvider
{
	private final SqlReportSupport sql;
	
	public BalanceStatementValueProvider(Database database)
	{
		this.sql = new SqlReportSupport(database);		
	}
	
	@Override
	public ReportValueSet loadValues(ReportContext context)
	{
		ReportValueSet values = new ReportValueSet();
		put(values, "balanceStmt.assets.undepositedAndNonInterestCash",
			sql.accountBalance("Undep. & non-interest cash", context));
		put(values, "balanceStmt.assets.cashEarningInterest",
			sql.accountBalance("Cash Earning Interest", context));
		put(values, "balanceStmt.assets.receivables",
			sql.accountBalance("Receivables", context));
		put(values, "balanceStmt.assets.inventory",
			sql.accountBalance("Inventory", context));
		put(values, "balanceStmt.assets.regalia",
			sql.accountBalance("Regalia", context));
		put(values, "balanceStmt.assets.purchasedPropertyAndEquipment",
			sql.accountBalance("Purchased Property & Equipment", context));
		put(values, "balanceStmt.assets.lessAccumulatedDepreciation",
			sql.accountBalance("Less Accum. Depreciation", context));
		put(values, "balanceStmt.assets.prepaidExpenses",
			sql.accountBalance("Prepaid Expenses", context));
		put(values, "balanceStmt.assets.otherAssets",
			sql.accountBalance("Other Assets", context));
		
		put(values, "balanceStmt.liabilities.newsletterSubscriptionsDue",
			sql.accountBalance("Newsletter Subs. Due", context));
		put(values, "balanceStmt.liabilities.deferredRevenue",
			sql.accountBalance("Deferred Revenue", context));
		put(values, "balanceStmt.liabilities.payables",
			sql.accountBalance("Payables", context));
		put(values, "balanceStmt.liabilities.otherLiabilities",
			sql.accountBalance("Other Liabilities", context));
		put(values, "balanceStmt.netAssets",
			sql.accountBalance("Net Assets", context));
		
		BigDecimal totalAssets = sum(values,
			"balanceStmt.assets.undepositedAndNonInterestCash",
			"balanceStmt.assets.cashEarningInterest",
			"balanceStmt.assets.receivables",
			"balanceStmt.assets.inventory",
			"balanceStmt.assets.regalia",
			"balanceStmt.assets.purchasedPropertyAndEquipment",
			"balanceStmt.assets.lessAccumulatedDepreciation",
			"balanceStmt.assets.prepaidExpenses",
			"balanceStmt.assets.otherAssets");
		BigDecimal totalLiabilities = sum(values,
			"balanceStmt.liabilities.newsletterSubscriptionsDue",
			"balanceStmt.liabilities.deferredRevenue",
			"balanceStmt.liabilities.payables",
			"balanceStmt.liabilities.otherLiabilities");
		BigDecimal netAssets = value(values, "balanceStmt.netAssets");
		
		put(values, "balanceStmt.totalAssets", totalAssets);
		put(values, "balanceStmt.totalLiabilities", totalLiabilities);
		put(values, "balanceStmt.totalLiabilitiesAndNetAssets",
			totalLiabilities.add(netAssets));
		put(values, "balanceStmt.balanceCheck",
			totalAssets.subtract(totalLiabilities.add(netAssets)));
		return values;
		
	}
	
	private void put(ReportValueSet values, String key, BigDecimal value)
	{
		values.put(key, value == null ? BigDecimal.ZERO : value);
		
	}
	
	private BigDecimal sum(ReportValueSet values, String... keys)
	{
		BigDecimal total = BigDecimal.ZERO;
		for (String key : keys)
			total = total.add(value(values, key));
		return total;
		
	}
	
	private BigDecimal value(ReportValueSet values, String key)
	{
		Object v = values.get(key);
		return v instanceof BigDecimal bd ? bd : BigDecimal.ZERO;
		
	}
	
}
