package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.repository.AuditLogRepository;
import com.dbtraining.reconx.repository.entity.AuditLogEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TICKET-ADV071 — GET /api/v1/audit/trades/{tradeRef}
 * TICKET-ADV138 — GET /api/v1/audit/trades/{tradeRef}/events
 */
@RestController
@RequestMapping("/v1/audit")
@Tag(name = "audit")
@SecurityRequirement(name = "bearerAuth")
public class AuditController {

    private final AuditLogRepository auditRepo;

    public AuditController(AuditLogRepository auditRepo) { this.auditRepo = auditRepo; }

    // -------------------------------------------------------------------------
    // TICKET-ADV071 — GET /api/v1/audit/trades/{tradeRef}
    // -------------------------------------------------------------------------
    @GetMapping("/trades/{tradeRef}")
    @Operation(summary = "Get audit history for a trade (by tradeRef)")
    public List<AuditLogEntry> history(@PathVariable String tradeRef) {
        // Return all audit log entries for the given tradeRef, oldest-first.
        return auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
    }

    // -------------------------------------------------------------------------
    // TICKET-ADV138 — GET /api/v1/audit/trades/{tradeRef}/events (Kafka events)
    // -------------------------------------------------------------------------
    @GetMapping("/trades/{tradeRef}/events")
    @Operation(summary = "Stream of all Kafka-sourced events for a trade")
    public List<AuditLogEntry> events(@PathVariable String tradeRef) {
        // Reuses the same audit log table; Kafka consumer populates it in Day 6.
        return auditRepo.findByTradeRefOrderByEventTimestampAsc(tradeRef);
    }
}
