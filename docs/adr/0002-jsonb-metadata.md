# ADR-0002 — Use JSONB for instrument metadata

- Status: Accepted
- Date: 2026-07-28
- Deciders: ReconX team

## Context

Different asset classes (Equities, FX, Fixed Income, Derivatives, Commodities) require vastly different metadata attributes (e.g., sector/exchange for equities, tenor/underlying/expiry for derivatives, settlement info, ratings, etc.). Creating separate tables or columns for every potential attribute would lead to a sparse, bloated table schema, require frequent DDL schema updates as new attributes are introduced, and complicate querying.

## Decision

Use a PostgreSQL `JSONB` column named `metadata` on the `instruments` table to store schema-flexible, asset-class-specific attributes. Standardized columns (id, symbol, name, asset_class, currency, isin) will remain structured fields. All dynamic properties will go into the `metadata` JSONB document.

## Consequences

**Positive**
- Flexible schema that can adapt to new asset classes and metadata attributes without database migrations (DDL).
- Binary format (`JSONB`) allows fast decompression, updates, and indexing.
- Avoids sparse table layout with dozens of null columns.

**Negative**
- Schema validation moves to the application layer (must be enforced in Spring Boot / Hibernate).
- Harder to enforce referential integrity (FKs) or standard check constraints inside the JSONB structure.
- Slightly higher storage footprint per row compared to compact SQL types.
