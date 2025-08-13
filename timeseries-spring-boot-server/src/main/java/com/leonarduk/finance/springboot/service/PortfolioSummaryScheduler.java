package com.leonarduk.finance.springboot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task that builds a portfolio summary and emails it at configured times.
 */
@Component
public class PortfolioSummaryScheduler {

    private final PortfolioMetricsService metricsService;
    private final EmailService emailService;

    public PortfolioSummaryScheduler(PortfolioMetricsService metricsService,
            EmailService emailService) {
        this.metricsService = metricsService;
        this.emailService = emailService;
    }

    /**
     * Triggered based on cron expression defined in application properties.
     */
    @Scheduled(cron = "${portfolio.summary.cron:0 0 8,18 * * *}")
    public void sendPortfolioSummary() {
        String summary = metricsService.createSummary();
        emailService.send("Portfolio Metrics Summary", summary);
    }
}

