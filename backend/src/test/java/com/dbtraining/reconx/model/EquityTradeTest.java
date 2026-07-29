package com.dbtraining.reconx.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.dbtraining.reconx.model.TradeType.AssetClass;
class EquityTradeTest {

    @Test
    void builder_buildsWhenAllRequiredPresent() {
        EquityTrade trade = EquityTrade.builder()
                .tradeRef(TradeRef.of("SAP-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("10"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.now())
                .counterpartyId(1L)
                .build();

        assertThat(trade.tradeRef().value()).isEqualTo("SAP-20260603-0001");
        assertThat(trade.notional().amount()).isEqualByComparingTo("1000");
        assertThat(trade.assetClass()).isEqualTo(AssetClass.EQUITY);
    }

    @Test
    void builder_missingPrice_throws() {
        assertThatThrownBy(() -> EquityTrade.builder()
                .tradeRef(TradeRef.of("SAP-20260603-0001"))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("10"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.now())
                .counterpartyId(1L)
                .build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("price");
    }

    @Test
    void equality_byTradeRef() {
        EquityTrade t1 = sampleEquity("SAP-20260603-0001");
        EquityTrade t2 = sampleEquity("SAP-20260603-0001");
        EquityTrade t3 = sampleEquity("SAP-20260603-0002");

        assertThat(t1).isEqualTo(t2);
        assertThat(t1.hashCode()).isEqualTo(t2.hashCode());
        assertThat(t1).isNotEqualTo(t3);
    }

    private EquityTrade sampleEquity(String ref) {
        return EquityTrade.builder()
                .tradeRef(TradeRef.of(ref))
                .instrumentSymbol("SAP.DE")
                .quantity(new BigDecimal("100"))
                .price(new BigDecimal("100"))
                .currency("EUR")
                .side(Side.BUY)
                .tradeDate(LocalDate.of(2026, 6, 3))
                .counterpartyId(1L)
                .build();
    }
}
