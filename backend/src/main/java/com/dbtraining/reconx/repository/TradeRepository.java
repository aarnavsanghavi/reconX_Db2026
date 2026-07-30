package com.dbtraining.reconx.repository;

import com.dbtraining.reconx.repository.entity.Trade;
import org.springframework.data.jpa.repository.EntityGraph;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;


/**
 * ============================================================================
 * TICKET-ADV055 — Custom JPQL filter query
 * TICKET-ADV056 — Specification-based dynamic queries (JpaSpecificationExecutor)
 * TICKET-ADV057 — Pageable / Page<T> for paginated list endpoints
 *
 * WHAT:    Spring Data JPA repository for the Trade entity.
 * HOW:     Extends JpaRepository for standard CRUD; JpaSpecificationExecutor
 *          for Specification-based dynamic queries used by TradeService.list().
 *          findByFilters() is a JPQL query with (:param IS NULL OR ...) idiom
 *          for optional per-field filtering.
 * WHY:     findByTradeRef is used for duplicate-check on create and for Kafka
 *          consumer idempotency; findByFilters backs the Day-5 paged list
 *          endpoint; findAll(Specification, Pageable) backs ADV056 composable
 *          queries in TradeService.list().
 * OBSERVE: GET /api/v1/trades?status=PENDING&counterpartyId=1 should produce
 *          a WHERE clause with both predicates — enable show-sql=true to verify.
 * ============================================================================
 */
public interface TradeRepository
        extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {

    /** Derived query — Spring generates the SQL from the method name. */
    Optional<Trade> findByTradeRef(String tradeRef);

    /**
     * TICKET-ADV055 — Optional multi-filter JPQL query.
     * The (:param IS NULL OR field = :param) idiom lets callers pass null
     * to skip a filter without rewriting the query.
     *
     * Note: counterpartyId navigates the ManyToOne relation via JPQL
     * dot-notation (t.counterparty.id) — Hibernate will generate the JOIN.
     */
    @Query("""
        SELECT t FROM Trade t
        WHERE t.tradeDate BETWEEN :from AND :to
          AND (:status IS NULL OR t.status = :status)
          AND (:counterpartyId IS NULL OR t.counterparty.id = :counterpartyId)
        """)
    Page<Trade> findByFilters(@Param("from")           LocalDate from,
                              @Param("to")             LocalDate to,
                              @Param("status")         String status,
                              @Param("counterpartyId") Long counterpartyId,
                              Pageable pageable);

    /**
     * TICKET-ADV056/ADV057 — Override the JpaSpecificationExecutor findAll so that
     * the Specification-based list query eagerly fetches counterparty + instrument
     * in a single JOIN, preventing LazyInitializationException when the MapStruct
     * mapper accesses trade.getCounterparty().getId() after the transaction closes
     * in the controller.
     */
    @Override
    @EntityGraph(attributePaths = {"counterparty", "instrument"})
    Page<Trade> findAll(Specification<Trade> spec, Pageable pageable);

    /** Convenience count used by metrics/reporting (ADV083). */
    long countByStatus(String status);
}

