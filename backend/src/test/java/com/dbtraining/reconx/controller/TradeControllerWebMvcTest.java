package com.dbtraining.reconx.controller;

import com.dbtraining.reconx.dto.TradeMapper;
import com.dbtraining.reconx.dto.TradeRequest;
import com.dbtraining.reconx.dto.TradeResponse;
import com.dbtraining.reconx.repository.entity.Counterparty;
import com.dbtraining.reconx.repository.entity.Instrument;
import com.dbtraining.reconx.repository.entity.Trade;
import com.dbtraining.reconx.security.JwtAuthenticationFilter;
import com.dbtraining.reconx.security.JwtTokenProvider;
import com.dbtraining.reconx.security.SecurityConfig;
import com.dbtraining.reconx.service.TradeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TradeController.class)
@Import(SecurityConfig.class)
class TradeControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TradeService service;

    @MockBean
    private TradeMapper mapper;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void passJwtFilterThrough() throws Exception {
        // JwtAuthenticationFilter is a @MockBean, so by default its doFilter()
        // is a no-op and swallows every request before it reaches the
        // DispatcherServlet. Since SecurityConfig wires it into the real
        // filter chain via addFilterBefore(), we stub it here to just pass
        // the request/response through to the next filter in the chain.
        doAnswer(invocation -> {
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    private TradeRequest validRequest() {
        return new TradeRequest(
                "TRD-20260315-9999",
                1L,
                1L,
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now()
        );
    }

    @Test
    @WithMockUser(roles = "TRADER")
    void testCreateTrade_authenticated_returns201() throws Exception {

        Trade trade = new Trade();
        ReflectionTestUtils.setField(trade, "id", 42L);
        trade.setTradeRef("TRD-20260315-9999");

        Instrument instrument = new Instrument();
        ReflectionTestUtils.setField(instrument, "id", 1L);
        instrument.setSymbol("SAP.DE");

        Counterparty counterparty = new Counterparty();
        ReflectionTestUtils.setField(counterparty, "id", 1L);
        counterparty.setName("Apex Brokers Inc");

        trade.setInstrument(instrument);
        trade.setCounterparty(counterparty);
        trade.setAssetClass("EQUITY");
        trade.setSide("BUY");
        trade.setQuantity(new BigDecimal("100.0000"));
        trade.setPrice(new BigDecimal("245.50"));
        trade.setTradeDate(LocalDate.now());
        trade.setStatus("PENDING");

        Instant now = Instant.now();

        TradeResponse response = new TradeResponse(
                42L,
                "TRD-20260315-9999",
                1L,
                "SAP.DE",
                1L,
                "Apex Brokers Inc",
                "EQUITY",
                "BUY",
                new BigDecimal("100.0000"),
                new BigDecimal("245.50"),
                LocalDate.now(),
                "PENDING",
                now,
                now
        );

        when(service.create(any(TradeRequest.class), anyString()))
                .thenReturn(trade);

        when(mapper.toResponse(any(Trade.class)))
                .thenReturn(response);

        mockMvc.perform(post("/v1/trades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location",
                        containsString("/api/v1/trades/42")))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.tradeRef").value("TRD-20260315-9999"));
    }
}