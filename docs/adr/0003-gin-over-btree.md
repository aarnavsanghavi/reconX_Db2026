# ADR-0003 — Use GIN index with jsonb_path_ops over B-tree for instrument metadata

- Status: Accepted
- Date: 2026-07-28
- Deciders: ReconX team

## Context

With `instruments.metadata` stored as JSONB, search queries (e.g., fetching all instruments where `metadata ->> 'sector' = 'Technology'`) will require a full table sequential scan (Seq Scan) because a standard B-tree index cannot index nested JSON keys and values. Since reconciliation and dashboard queries filter heavily by metadata fields, we need to optimize JSONB query performance.

## Decision

Create a Generalized Inverted Index (GIN) index using the `jsonb_path_ops` operator class on the `metadata` column:
`CREATE INDEX idx_instruments_metadata_gin ON instruments USING GIN (metadata jsonb_path_ops);`

We chose `jsonb_path_ops` over the default `jsonb_ops` because `jsonb_path_ops` creates much smaller indexes and provides significantly faster query performance for containment queries (using the `@>` operator), which is our primary query pattern.

## Consequences

**Positive**
- Performance: Queries filtering by JSON keys/values using containment `@>` will perform fast index scans instead of sequential scans.
- Space efficiency: `jsonb_path_ops` indexes are smaller than default `jsonb_ops` indexes.

**Negative**
- Write overhead: Every insert/update on the `instruments` table will incur a small GIN index maintenance cost.
- Limitation: `jsonb_path_ops` does not index existence operators (like `?` or `?|`), meaning queries checking if a key exists must fallback to sequential scans or use containment checks.
