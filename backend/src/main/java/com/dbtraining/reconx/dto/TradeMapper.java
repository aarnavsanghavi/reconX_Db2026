package com.dbtraining.reconx.dto;

import com.dbtraining.reconx.repository.entity.Trade;
import org.mapstruct.*;

/**
 * ============================================================================
 * TICKET-ADV054 — MapStruct mapper: Trade entity <-> DTO
 *
 * WHAT:    Generates the entity↔DTO conversion at compile time.
 * HOW:     componentModel="spring" → MapStruct emits a @Component bean named
 *          tradeMapper that you can @Autowire.
 *          unmappedTargetPolicy=ERROR → the build fails if a new field is added
 *          to TradeResponse and the mapping is not updated.
 * WHY:     Hand-written mappers drift. MapStruct fails the build if a new
 *          field is added to one side and forgotten on the other.
 * OBSERVE: After `./mvnw compile`, inspect
 *          target/generated-sources/annotations/.../TradeMapperImpl.java —
 *          it should contain explicit getter/setter calls and a @Component
 *          annotation so Spring picks it up automatically.
 * ============================================================================
 */
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TradeMapper {

    /**
     * Maps a Trade JPA entity to the TradeResponse DTO.
     * Flat-maps the nested Counterparty and Instrument relations
     * so no lazy-load explosion occurs after the transaction closes.
     */
    @Mapping(source = "instrument.id",     target = "instrumentId")
    @Mapping(source = "instrument.symbol", target = "instrumentSymbol")
    @Mapping(source = "counterparty.id",   target = "counterpartyId")
    @Mapping(source = "counterparty.name", target = "counterpartyName")
    TradeResponse toResponse(Trade trade);

    /**
     * Maps a TradeRequest DTO to a Trade entity shell.
     * Fields wired by the service layer are deliberately ignored here:
     *  - id          — assigned by the database (IDENTITY)
     *  - counterparty — resolved from counterpartyId by the service
     *  - instrument   — resolved from instrumentId by the service
     *  - status       — defaulted to "PENDING" by the service
     *  - createdAt    — populated by Spring Data auditing
     *  - modifiedAt   — populated by Spring Data auditing
     *  - deletedAt    — set only by softDelete()
     */
    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "instrument",   ignore = true)
    @Mapping(target = "status",       ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    @Mapping(target = "modifiedAt",   ignore = true)
    @Mapping(target = "deletedAt",    ignore = true)
    Trade toEntity(TradeRequest req);
}
