package com.leonarduk.finance.springboot.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PortfolioSummarySchedulerTest {

    @Mock
    private PortfolioMetricsService metricsService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PortfolioSummaryScheduler scheduler;

    @Test
    void sendPortfolioSummaryDelegatesToServices() {
        when(metricsService.createSummary()).thenReturn("summary");

        scheduler.sendPortfolioSummary();

        verify(metricsService).createSummary();
        verify(emailService).send("Portfolio Metrics Summary", "summary");
    }
}
