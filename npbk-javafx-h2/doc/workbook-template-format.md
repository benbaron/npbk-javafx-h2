# Embedded Workbook Template Format

This document describes the JSON template format used to render workbook-modeled report panes inside the JavaFX/H2 application.

## Design purpose

The SCA exchequer workbook is a source artifact for user-facing report layout. The Java application must not depend on the workbook file at runtime. Instead, selected workbook sheets are extracted into JSON templates, committed as application resources, and rendered by JavaFX.

The source workbook is currently:

- `SCA Exchequer Report - 2026-03-D(1).xlsx`
- SHA-256: `d18a68f02c41e43cee06aa03fcf506a9ded5719fd2f296db7ea4464157f71071`

## Runtime rule

At runtime the application loads embedded JSON template resources from:

```text
src/main/resources/org/example/npbk/ui/template/
```

The preferred committed resource form is:

```text
<SheetName>.template.json.gz.b64
```

The template language is still JSON. The `.gz.b64` wrapper is only packaging so the resources remain compact and safe to include in the JAR.

The application should not load the original `.xlsx` workbook at runtime.

## Target sheets

The first report/list templates are:

```text
BalanceStmt
IncomeStmt
WorkbookSummary
TransactionsList
AllChecksTfrs
FundTransfers
```

`BalanceStmt` and `IncomeStmt` should represent the official workbook report forms. `TransactionsList`, `AllChecksTfrs`, and `FundTransfers` are workbook-modeled list/report pages. `WorkbookSummary` is a separate support/report page, not the same as the older Summary tab.

## JSON object structure

Each uncompressed template is a JSON object:

```json
{
  "schemaVersion": "npbk-workbook-template-1",
  "sourceWorkbook": "SCA Exchequer Report - 2026-03-D(1).xlsx",
  "sourceWorkbookSha256": "d18a68f02c41e43cee06aa03fcf506a9ded5719fd2f296db7ea4464157f71071",
  "sheetName": "BalanceStmt",
  "sourceRange": "A1:K42",
  "rows": [],
  "columns": [],
  "mergedRegions": [],
  "styles": {},
  "cells": []
}
```

## `rows`

The `rows` array preserves workbook row indices and approximate pixel heights.

```json
{
  "index": 1,
  "heightPx": 20
}
```

The row index is the original 1-based Excel row number.

## `columns`

The `columns` array preserves workbook column indices, letters, and approximate pixel widths.

```json
{
  "index": 2,
  "letter": "B",
  "widthPx": 150
}
```

The column index is the original 1-based Excel column number.

## `mergedRegions`

Merged regions are recorded using original Excel row/column coordinates.

```json
{
  "firstRow": 4,
  "firstCol": 2,
  "lastRow": 4,
  "lastCol": 10
}
```

The renderer should apply the span only when it encounters the upper-left cell of the merged region.

## `styles`

The `styles` object maps workbook style IDs to a normalized style description.

```json
{
  "42": {
    "font": {
      "bold": true,
      "italic": false,
      "size": 11.0,
      "color": "#000000"
    },
    "fill": {
      "fgColor": "#D9EAF7"
    },
    "border": {
      "top": "thin",
      "bottom": "thin"
    },
    "alignment": {
      "horizontal": "center",
      "vertical": "center",
      "wrapText": true
    },
    "numberFormatId": 164,
    "formatCode": "#,##0.00;[Red]\\(#,##0.00\\)"
  }
}
```

The renderer does not need to reproduce every Excel style property immediately. It should prioritize:

- font weight
- font style
- font size
- fills/background colors
- borders
- horizontal alignment
- wrapping

## `cells`

The `cells` array contains cells from the extracted workbook range that have visible text or formulas.

```json
{
  "ref": "B4",
  "row": 4,
  "col": 2,
  "styleId": 37,
  "text": "COMPARATIVE BALANCE STATEMENT AS OF MARCH 31, 2026",
  "formula": null,
  "rawValue": null
}
```

`row` and `col` are the original Excel coordinates. `text` is the display string extracted from the workbook. Formula cells may have:

```json
{
  "formula": "SUM(H15:H23)",
  "rawValue": "0",
  "text": "-"
}
```

The extraction process should prefer display text where possible. It should avoid showing raw Excel serial dates such as `46112` in the JavaFX report.

## Values from H2

The first version renders workbook-extracted display text. Later versions should support dynamic value binding through an additional optional key:

```json
{
  "ref": "H24",
  "row": 24,
  "col": 8,
  "styleId": 84,
  "text": "-",
  "valueKey": "balanceStmt.totalAssets",
  "format": "currency"
}
```

The renderer should resolve `valueKey` from a report-value map, then fall back to `text` if no dynamic value is available.

Recommended value key naming:

```text
balanceStmt.assets.undepositedAndNonInterestCash
balanceStmt.assets.cashEarningInterest
balanceStmt.totalAssets
incomeStmt.totalGrossIncome
incomeStmt.totalExpenses
transactionsList.totalBankNow
allChecksTfrs.outstandingTotal
fundTransfers.totalTransfersOut
```

## Extraction workflow

The extraction workflow is local/regeneration-time only:

```text
archived workbook .xlsx
  -> extractor script/tool
  -> *.template.json
  -> gzip + base64
  -> *.template.json.gz.b64 committed under src/main/resources
```

The application consumes only the committed JSON resources.

## Renderer workflow

Runtime rendering flow:

```text
JsonWorkbookTemplateLoader.load("BalanceStmt")
  -> inflate/decode JSON resource
  -> parse JsonObject
  -> WorkbookTemplateRenderer.render(template)
  -> JavaFX GridPane inside the scrollable center workspace
```

## Implementation notes

- The renderer should not mutate the template data.
- Coordinates remain Excel-style 1-based row/column values.
- The JavaFX renderer translates the first row/column in the template into GridPane index 0.
- Horizontal and vertical scroll bars are expected at the center workspace level.
- The report panes are read-only.
- Exporters can reuse the same JSON template model later for text, `.xlsx`, PDF, or JRXML generation.

## Why not runtime XLSX

Runtime XLSX loading is intentionally avoided because it would make the Java program brittle and dependent on an external workbook file. The workbook is a reference source, not the application engine.

## Why JSON

JSON is easy to generate, inspect, version, diff, and load from Java. It separates form layout data from Java renderer logic while keeping the report form embedded inside the program.
