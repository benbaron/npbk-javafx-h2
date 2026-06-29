# Database Migration Policy

## Purpose

The H2 database is the authoritative source of accounting data. Application upgrades must preserve existing user records and must never resolve schema incompatibility by deleting or recreating a user database.

## Required migration behavior

1. Every schema change is represented by an ordered, versioned migration.
2. Migrations are applied inside an explicit database transaction whenever H2 supports the required DDL transaction semantics.
3. A failed migration must stop startup, roll back changes where possible, and leave the original database available for diagnosis and recovery.
4. Existing tables and columns are altered or copied forward nondestructively.
5. Destructive operations require a separately reviewed data-retention plan and are not part of normal application startup.
6. The application must not silently drop tables because a required column is missing.
7. File-mode H2 is used by the application. In-memory H2 is used for migration and repository tests.

## Compatibility handling

When an older prototype schema is detected, startup must do one of the following:

- apply a supported forward migration;
- report a precise incompatibility error naming the detected schema and the required migration;
- offer an explicit, user-controlled export-and-upgrade workflow.

Startup must not delete tables, database files, transactions, transaction lines, reconciliations, period-close records, attachments, or audit history.

## Migration ledger

The database must contain a migration ledger recording at least:

- migration version;
- migration name;
- checksum;
- applied timestamp;
- application version;
- success or failure status.

A migration whose checksum differs from an already-applied version must be rejected rather than reapplied silently.

## Testing requirements

Each material schema change requires tests that prove:

- a clean in-memory database migrates to the current schema;
- a database at the immediately preceding version migrates without data loss;
- migration failure preserves preexisting records;
- repeated startup is idempotent;
- constraints, foreign keys, indexes, and delete behavior match the documented schema;
- no migration path drops a populated accounting table merely because its shape is old.

## Current remediation requirement

The present `Database.initialize()` implementation contains prototype compatibility logic that may drop incompatible tables. That behavior conflicts with this policy and must be replaced by versioned, forward-only migrations before the application is considered production-safe.
