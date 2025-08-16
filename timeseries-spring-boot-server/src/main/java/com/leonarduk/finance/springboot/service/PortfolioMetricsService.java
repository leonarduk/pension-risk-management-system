package com.leonarduk.finance.springboot.service;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Service responsible for creating a summary of portfolio metrics.
 */
@Service
public class PortfolioMetricsService {

    private final Clock clock;

    public PortfolioMetricsService() {
        this(Clock.systemUTC());
    }

    public PortfolioMetricsService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Compile a simple summary of current portfolio metrics.
     *
     * @return summary text
     */
    public String createSummary() {
        return "Portfolio metrics summary generated at " + Instant.now(this.clock);
    }
}

