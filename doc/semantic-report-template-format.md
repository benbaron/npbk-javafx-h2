# Semantic Report Template Format

This guide documents the compact JSON format used for workbook-modeled report panes.

## Purpose

The source workbook is a reference for layout, labels, and report structure. The application should not load the workbook at runtime, and it should not store a literal dump of every worksheet cell. The committed JSON files describe the meaning of each report form: sections, rows, labels, value keys, formats, and table columns.

The renderer combines a semantic JSON template with H2-backed report values.

## Runtime flow

1. `SemanticReportTemplateLoader` loads `<templateId>.report.json` from `src/main/resources/org/example/npbk/report/`.
2. `ReportValueProviderRegistry` selects the Java provider for the template ID.
3. The provider returns a `ReportValueSet` containing scalar values and optional tables.
4. `SemanticReportRenderer` renders the template and values as a JavaFX pane.

## Template files

Current template resources:

- `BalanceStmt.report.json`
- `IncomeStmt.report.json`
- `WorkbookSummary.report.json`
- `TransactionsList.report.json`
- `AllChecksTfrs.report.json`
- `FundTransfers.report.json`

## Top-level fields

Required fields:

- `schemaVersion`: currently `npbk-semantic-report-1`
- `templateId`: stable ID such as `BalanceStmt`
- `title`: display title
- `type`: `sectionReport` or `tableReport`

Optional fields:

- `sourceSheet`: workbook tab name for traceability
- `subtitle`: displayed below the title

## Section reports

Section reports are used for financial statement style forms. A section has a title and rows. A row may include:

- `type`: optional. Use `totalRow`, `spacer`, or omit for ordinary rows.
- `line`: workbook/report line label.
- `label`: row description.
- `valueKey`: key looked up in `ReportValueSet`.
- `format`: `text`, `date`, or `currency`.
- `sourceCell`: workbook cell reference for traceability only.
- `note`: optional explanatory text.

`sourceCell` and any future `sourceFormula` fields are documentation only. They are not executable logic.

## Table reports

Table reports are used for list-style workbook pages. A table report has:

- `tableKey`: the table name in `ReportValueSet`.
- `columns`: displayed columns.

Each column has:

- `label`: displayed heading.
- `field`: key in each row map.
- `format`: optional display format.

## Value calculation

Calculations belong in Java and H2, not in the JSON template.

Current providers include:

- `BalanceStatementValueProvider`
- `IncomeStatementValueProvider`
- `WorkbookSummaryValueProvider`
- `ListReportValueProvider`

Providers should eventually resolve accounts and report mappings by database IDs. The first pass may use canonical account names where the chart-of-accounts mapping layer is not complete.

## Styling

The semantic format uses application CSS classes rather than storing every Excel style. The renderer currently uses:

- `report-header-cell`
- `report-section-cell`
- `report-value-cell`
- `report-total-cell`
- `report-currency-cell`
- `report-note-cell`
- `wide-cell`

If a report needs special styling later, add semantic style names rather than storing raw workbook style IDs.

## Design rule

A semantic report template describes the report form structure and the named places where accounting values appear. It should not contain every Excel cell, formula, blank, border, and helper column.

## Future extensions

Likely future additions:

- explicit provider IDs
- semantic column widths
- named row or cell styles
- required-value warnings
- source formulas as documentation
- export adapters for text, XLSX, PDF, and JRXML
