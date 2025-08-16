package com.leonarduk.finance.springboot.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class PortfolioMetricsServiceTest {

    @Test
    void createSummaryIncludesTimestamp() {
        PortfolioMetricsService service = new PortfolioMetricsService();
        String summary = service.createSummary();
        assertTrue(summary.startsWith("Portfolio metrics summary generated at "));
        String timestamp = summary.substring("Portfolio metrics summary generated at ".length());
        assertDoesNotThrow(() -> Instant.parse(timestamp));
    }

    @Test
    void createSummaryUsesFixedClock() {
        Instant fixedInstant = Instant.parse("2020-01-01T00:00:00Z");
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        PortfolioMetricsService service = new PortfolioMetricsService(clock);
        String summary = service.createSummary();
        assertEquals("Portfolio metrics summary generated at " + fixedInstant, summary);
    }

    @Test
    void createSummaryAtEpoch() {
        Instant fixedInstant = Instant.EPOCH;
        Clock clock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        PortfolioMetricsService service = new PortfolioMetricsService(clock);
        String summary = service.createSummary();
        assertEquals("Portfolio metrics summary generated at " + fixedInstant, summary);
    }
}
