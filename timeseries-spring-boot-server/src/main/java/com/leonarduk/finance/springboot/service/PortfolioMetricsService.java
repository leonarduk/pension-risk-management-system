package com.leonarduk.finance.springboot.service;

import java.time.Instant;
import org.springframework.stereotype.Service;

/**
 * Service responsible for creating a summary of portfolio metrics.
 */
@Service
public class PortfolioMetricsService {

    /**
     * Compile a simple summary of current portfolio metrics.
     *
     * @return summary text
     */
    public String createSummary() {
        return "Portfolio metrics summary generated at " + Instant.now();
    }
}

