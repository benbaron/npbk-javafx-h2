# NonprofitBookkeeping JavaFX/H2 Spreadsheet-Style Prototype

This is an Eclipse-importable Maven prototype for a database-backed accounting program that uses the spreadsheet as the familiar data-entry model rather than as the runtime calculation engine.

## What this pass adds

This version expands the original Ledger-only prototype into a broader first-pass application with functional tabs for:

- Summary
- Ledger
- Outstanding
- Budget
- Asset Details
- Liability Details
- Assets & Inventory
- Supplies
- Banking
- Period Close

Each non-report tab now has first-pass JavaFX controls, view models, services, repositories, validation hooks, and H2 persistence tables.

## Current architecture

```text
JavaFX spreadsheet-like UI
  -> View models with JavaFX properties
  -> Service layer for recalculation and validation
  -> Repository layer
  -> H2 file database under ~/.npbk-prototype/
```

The project intentionally does **not** store generic spreadsheet cells. It stores accounting/business records and renders them in spreadsheet-like grids.

## Eclipse import

1. Open Eclipse.
2. Choose `File > Import... > Maven > Existing Maven Projects`.
3. Select this project folder.
4. Let Eclipse update Maven dependencies.
5. Debug/run the plain launcher class:

```text
org.example.npbk.app.EclipseLauncher
```

Do not run `NonprofitBookkeepingApp` directly from Eclipse unless your JavaFX module path is configured manually.

## Maven run

```bash
mvn javafx:run
```

## Database

By default, the H2 database is created here:

```text
~/.npbk-prototype/npbk.mv.db
```

For a clean run, stop the app and delete the `~/.npbk-prototype` directory.

## Implemented first-pass behavior

### Ledger

- Spreadsheet-style editable grid
- Combo boxes for bank account, timing flags, budget category, and fund
- Date/text/money editing cells
- Enter/Tab navigation
- Recalculation of bank and budget effects
- Validation messages
- H2 persistence

### Summary

- Organization/branch/kingdom/report-period fields
- Derived totals from ledger and outstanding items
- Validation panel
- H2 persistence

### Outstanding

- Outstanding check/deposit grid
- Direction and status dropdowns
- Cleared-date-driven recalculation
- Validation and persistence

### Budget

- Fund/category/type/planned amount grid
- Actuals recalculated from ledger budget effects
- Variance calculation
- Validation and persistence

### Asset Details / Liability Details

- Shared supplemental-line engine
- Asset-side kinds: receivable, prepaid expense, other asset
- Liability-side kinds: deferred revenue, payable, other liability
- Linked transaction id field as a first-pass replacement for spreadsheet row links
- Remaining amount calculation placeholder
- Validation and persistence

### Assets & Inventory

- Inventory grid with item number, acquisition date, description, quantity/value, item type, use, guardian, confirmation date, notes
- Validation and persistence

### Supplies

- Supply grid with quantity, unit cost, calculated total value, use/location/guardian, notes
- Validation and persistence

### Banking

- First-pass statement line grid
- Statement/posted dates, amount, status, matched transaction id
- This is the seed for later import/matching/reconciliation workflows

### Period Close

- First-pass close checklist
- Validates period dates, ledger errors, open outstanding items, supplemental schedule errors, and checklist confirmations
- Saves close records to H2

## Known limitations

This is still a first pass. It is meant to establish the application structure, not final accounting completeness.

- It has a spreadsheet-like UI, but it is not a full Excel clone.
- Transaction split/debit-credit modeling is still simplified in the Ledger tab.
- Banking import/matching is represented by editable statement rows, not an importer yet.
- Period close does not yet lock transactions or generate closing entries.
- Supplemental detail remaining amount is currently a placeholder equal to amount.
- The project is compile-tested with Maven. Import it in Eclipse and let Maven resolve JavaFX/H2 dependencies.

## Suggested next development steps

1. Replace the simplified single-amount Ledger row with a true transaction + transaction line model.
2. Replace linked transaction id text entry with a picker showing date/name/amount, while storing durable ids.
3. Add statement CSV import and matching rules in Banking.
4. Add period lock rules that prevent editing closed-period transactions.
5. Add report DTOs for Income Statement, Balance Statement, Budget vs Actual, and Fund Activity.
6. Add unit tests for each recalculation/validation service.


## Running from Eclipse

Preferred debug entry point:

```text
org.example.npbk.app.EclipseLauncher
```

`EclipseLauncher` is a plain Java `main` class. It does **not** extend `javafx.application.Application`, which avoids the common Eclipse error:

```text
Error: JavaFX runtime components are missing, and are required to run this application
```

A compatibility launcher is also present at:

```text
org.example.npbk.app.Launcher
```

Do not run `org.example.npbk.app.NonprofitBookkeepingApp` directly from Eclipse unless you have explicitly configured the JavaFX module path yourself.

