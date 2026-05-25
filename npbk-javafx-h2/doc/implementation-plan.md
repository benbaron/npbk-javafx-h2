# NonprofitBookkeeping Workbook-Modeled Implementation Plan

## Source workbook

- File: `SCA Exchequer Report - 2026-03-D(1).xlsx`
- SHA-256: `d18a68f02c41e43cee06aa03fcf506a9ded5719fd2f296db7ea4464157f71071`
- Purpose: reference workbook for JavaFX/H2 pane layout, report layout, validation behavior, navigation links, and spreadsheet-style data entry.

The workbook is the user-facing reference model. It is not the long-term application architecture and should not be treated as a cell-storage schema. The Java application should preserve familiar workbook workflows while storing durable accounting records in H2.

## Core architecture

The application uses an accounting-native H2 schema as the root source of truth:

- `accounts` / chart of accounts
- `funds`
- `budget_categories`
- `bank_accounts`
- `people`
- `transactions`
- `transaction_lines`
- `documents`
- `supplies`
- `inventory_items`
- `bank_statement_lines`
- `supplemental_lines`
- `period_close_records`

Workbook-like pages are JavaFX views over these records. They are not stored as generic cells.

## User-interface direction

Use the `sca-jakarta-h2`-style shell:

- top menu/toolbar
- left navigation tree
- center panel host
- right inspector pane
- scrollable center workspace

The left navigation acts as the workbook's Navigation Links area. Panels should open through stable `AppPanelId` values, not raw strings.

## Spreadsheet-style editing direction

Restore the Excel-like editing behavior inside the new `PanelHost` shell:

- editable JavaFX `TableView` grids
- combo-box cells for controlled values
- text/date/money cells
- Enter/Tab commit-and-move behavior
- validation messages near the active row
- recalculation after edit commit
- read-only calculated columns where appropriate

The old tabbed prototype views should be adapted into `AppPanel` wrappers and then moved onto the normalized schema.

## Reporting panel direction

Reporting panes are read-only and visually modeled on the workbook sheets. They should approximate:

- workbook row/column positioning
- sheet headings and section headers
- borders
- fill colors
- subtotal/total rows
- currency alignment
- explanatory notes and warnings

Report panels should later export to text, `.xlsx`, and PDF.

Initial reporting panes to model from the workbook:

- `BalanceStmt`
- `IncomeStmt`
- `WorkbookSummary`
- `TransactionsList`
- `AllChecksTfrs`
- `FundTransfers`

## Workbook pages and Java roles

### `BalanceStmt`

Read-only Statement of Financial Position pane.

Backed by:

- `balance_stmt_view`
- `accounts`
- `transaction_lines`
- `transactions`
- supplemental asset/liability schedules where relevant

Should approximate the Excel report layout and render official report lines.

### `IncomeStmt`

Read-only Statement of Activities pane.

Backed by:

- `income_stmt_view`
- `accounts`
- `transaction_lines`
- `transactions`
- funds and budget categories where relevant

Should approximate the Excel report layout and render official report lines.

### `WorkbookSummary`

Separate from any generic Summary page. It is its own workbook-modeled support/reporting pane.

### `WorkbookTables`

Mostly internal/admin/reference support data. Use it to inform:

- chart of accounts
- dropdown lists
- report line mappings
- workbook validation/reference lists

It may become an admin/reference pane, but should not be treated as a normal user-entry sheet.

### `TransactionsList`

Read-only/searchable report/list view over the normalized transaction tables.

Backed by:

- `transactions_list_view`
- `ledger_search`

### `AllChecksTfrs`

Read-only/searchable view of checks and transfers.

Backed by:

- `all_checks_tfrs_view`
- bank-account-linked transactions
- reference/check numbers
- reconciliation/banking data as implemented

### `FundTransfers`

Read-only/searchable report over fund-transfer activity.

Backed by:

- `fund_transfers_view`
- transaction lines grouped by transaction and fund

The present implementation may need stronger transfer classification rules later.

### `Supplies`

Stored user-maintained data-entry pane. Supplies are separate from durable inventory assets.

## Database/search strategy

Start normalized, then add read models/views for the screens.

Core search/report views:

- `ledger_search`
- `transactions_list_view`
- `all_checks_tfrs_view`
- `fund_transfers_view`
- `balance_stmt_view`
- `income_stmt_view`

Index columns used in joins, filters, sorting, and grouping. High-value indexes include transaction date, reference number, person/vendor, bank account/date, line transaction, account, fund, budget category, bank statement status/date, and supplemental line kind/due date.

Avoid using display strings as primary links. Use IDs internally, but preserve exact workbook/SCA names for display and reporting.

## Immediate implementation sequence

1. Keep the new `MainWindow` / `PanelHost` shell.
2. Wire the spreadsheet-style `LedgerView` into `PanelHost` as `Transaction Editor`.
3. Make `TransactionRepository` persist the familiar ledger row as real accounting records:
   - transaction header
   - two or more balanced transaction lines
   - lookup IDs for accounts, funds, budget categories, bank accounts, and people
4. Implement fixed workbook-form report templates for `BalanceStmt` and `IncomeStmt`.
5. Implement workbook-form/list templates for:
   - `TransactionsList`
   - `AllChecksTfrs`
   - `FundTransfers`
   - `WorkbookSummary`
6. Convert `Supplies`, `Inventory`, `Outstanding`, `Budget`, and supplemental detail panes from the old tabbed UI to `AppPanel` wrappers.
7. Expand the chart-of-accounts admin pane so account names and mappings can be modified while preserving stable database IDs.
8. Add banking/reconciliation workflow panes.
9. Add period-close workflow panes.
10. Add export-to-text, export-to-xlsx, and export-to-PDF for report panels.

## Important constraints

- Preserve exact SCA/chart-of-accounts display names in the database and UI.
- Do not make row numbers the durable data relationship.
- Use database IDs for links.
- Keep formulas/recalculation in services, not JavaFX table-cell event handlers.
- Treat the workbook as UI/report reference and behavior evidence, not as a cell-by-cell storage model.
- Reporting panes are read-only.
- Data-entry panes may look like workbook sheets but must be less brittle than Excel.
