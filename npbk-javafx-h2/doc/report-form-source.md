# Archived report form source

The source workbook for the current workbook-modeled implementation plan is:

- `SCA Exchequer Report - 2026-03-D(1).xlsx`
- SHA-256: `d18a68f02c41e43cee06aa03fcf506a9ded5719fd2f296db7ea4464157f71071`
- Size: `11375361` bytes

The workbook should be stored under:

`npbk-javafx-h2/doc/SCA Exchequer Report - 2026-03-D(1).xlsx`

## Assistant artifact note

The GitHub connector used in this conversation can create text files directly, but it is not practical for committing this 11.4 MB binary workbook through the connector. The assistant-generated downloadable archive `npbk-doc-archive.zip` contains:

- `doc/SCA Exchequer Report - 2026-03-D(1).xlsx`
- `doc/implementation-plan.md`
- `doc/report-form-source.md`

To complete the repository archive manually, unzip that archive and commit the workbook into the same `doc/` directory.

## Role of the workbook

This workbook is the reference form for:

- workbook-modeled JavaFX report panels
- report row/column layout
- borders and visual cues
- navigation links
- formulas as evidence of data relationships
- validation and recalculation behavior

The Java/H2 application should preserve the workbook's familiar user-facing model while using normalized accounting records as the source of truth.
