# NonprofitBookkeeping Engineering Documentation

This directory contains the version-controlled engineering guidance for the JavaFX/H2 bookkeeping application.

The workbook remains an important user-interface and reporting reference, but the Java application and H2 database are the authoritative implementation. These documents describe how to preserve familiar workbook workflows while building a normalized, auditable bookkeeping system.

## Documents

- [Architecture](architecture.md) — package responsibilities, dependency direction, application layers, and implementation boundaries.
- [Accounting Model](accounting-model.md) — double-entry rules, transaction lifecycle, fund accounting, budgets, period close, and audit expectations.
- [Database Schema](database-schema.md) — current tables and views, relationships, constraints, indexing, and planned schema additions.
- [Database Migrations](database-migrations.md) — required nondestructive schema-versioning and migration workflow.
- [UI Guidelines](ui-guidelines.md) — JavaFX workspace, responsive layout, spreadsheet-style editors, styling, accessibility, and geometry rules.
- [Testing Strategy](testing-strategy.md) — unit, H2 repository, service, migration, report, UI-policy, and regression testing.
- [Banking and Reconciliation](banking-and-reconciliation.md) — statement import, matching, reconciliation lifecycle, controls, and future implementation.
- [Development Workflow](development-workflow.md) — branch, pull-request, review, CI, and post-merge practices.
- [Implementation Plan](implementation-plan.md) — current state, priorities, phased delivery, and workbook-derived targets.
- [Semantic Report Template Format](semantic-report-template-format.md) — current JSON report-template structure and rendering contract.
- [Report Form Source Manifest](report-form-source.md) — identity and hash of the workbook used as a report-layout reference.

## Authority and precedence

When guidance conflicts, use this order:

1. Correct accounting treatment and preservation of user data.
2. Current code and schema on `main`, after verifying whether the implementation is intentional or transitional.
3. These engineering documents.
4. The workbook as user-interface, terminology, validation, and report-layout evidence.
5. Older prototype classes and obsolete branches.

A conflict should be documented and resolved deliberately. Do not create a second architecture merely to avoid modifying an obsolete implementation.

## Updating these documents

Documentation changes should be included in the same pull request as the architectural change they describe. A major implementation change is incomplete when the relevant design, schema, testing, or workflow document remains inaccurate.
