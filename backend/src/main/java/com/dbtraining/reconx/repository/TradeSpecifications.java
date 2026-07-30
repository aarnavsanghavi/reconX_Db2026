package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * ============================================================================
 * TICKET-ADV056 — TradeSpecifications
 *
 * WHAT:    Static factories that return Specification<Trade> instances which
 *          callers compose with .and() / .or() in the service layer for
 *          dynamic queries.
 * HOW:     Each method returns a lambda `(root, query, cb) -> Predicate`.
 *          A null filter argument means "no constraint", encoded via
 *          cb.conjunction() (equivalent to SQL `1=1`, a no-op predicate).
 *          The service composes them:
 *            Specification.where(tradeDateBetween(from, to))
 *                         .and(hasStatus(status))
 *                         .and(hasCounterparty(counterpartyId))
 * WHY:     Avoids exploding the repository with findByXAndYAndZ... methods
 *          for every possible combination of filters. Each spec is an
 *          independently testable unit, and new filters are added without
 *          touching the repository interface.
 * OBSERVE: GET /api/v1/trades?status=NEW&from=2026-01-01 should produce SQL
 *          with exactly the two predicates — enable `spring.jpa.show-sql:true`
 *          to verify. When only from/to are passed, no status or counterparty
 *          clauses appear in the WHERE.
 * ============================================================================
 */
public final class TradeSpecifications {

    private TradeSpecifications() {}

    /**
     * Matches trades whose status equals the supplied value.
     * Returns a no-op conjunction when status is null (all statuses pass).
     */
    public static Specification<Trade> hasStatus(String status) {
        return (root, q, cb) -> status == null
                ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    /**
     * Matches trades whose tradeDate falls within [from, to].
     * Handles partial ranges:
     *  - both null   → no-op (all dates pass)
     *  - from only   → tradeDate >= from
     *  - to only     → tradeDate <= to
     *  - both given  → tradeDate BETWEEN from AND to
     */
    public static Specification<Trade> tradeDateBetween(LocalDate from, LocalDate to) {
        return (root, q, cb) -> {
            if (from == null && to == null) return cb.conjunction();
            if (from == null)               return cb.lessThanOrEqualTo(root.get("tradeDate"), to);
            if (to == null)                 return cb.greaterThanOrEqualTo(root.get("tradeDate"), from);
            return cb.between(root.get("tradeDate"), from, to);
        };
    }

    /**
     * Matches trades linked to the given counterparty id.
     * Navigates the ManyToOne relation via root.get("counterparty").get("id"),
     * which Hibernate translates into an INNER JOIN on counterparty_id.
     * Returns a no-op conjunction when counterpartyId is null.
     */
    public static Specification<Trade> hasCounterparty(Long counterpartyId) {
        return (root, q, cb) -> counterpartyId == null
                ? cb.conjunction()
                : cb.equal(root.get("counterparty").get("id"), counterpartyId);
    }
}
