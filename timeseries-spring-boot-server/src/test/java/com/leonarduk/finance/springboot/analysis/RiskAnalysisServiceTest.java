package com.leonarduk.finance.springboot.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class RiskAnalysisServiceTest {

    private final RiskAnalysisService service = new RiskAnalysisService();

    @Test
    void calculatesMaxDrawdown() {
        List<Double> prices = Arrays.asList(100.0, 120.0, 80.0, 130.0, 70.0);
        double maxDrawdown = service.calculateMaxDrawdown(prices);
        assertEquals(0.4615, maxDrawdown, 0.0001);
    }

    @Test
    void handlesEmptyPrices() {
        List<Double> prices = List.of();
        double maxDrawdown = service.calculateMaxDrawdown(prices);
        assertEquals(0.0, maxDrawdown, 0.0001);
    }
}

