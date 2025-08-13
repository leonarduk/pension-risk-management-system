package com.leonarduk.finance.springboot;

import com.leonarduk.finance.springboot.analysis.RiskAnalysisService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint exposing risk analysis metrics.
 */
@RestController
@RequestMapping("/risk")
public class RiskAnalysisEndpoint {

    private final RiskAnalysisService riskAnalysisService;

    @Autowired
    public RiskAnalysisEndpoint(RiskAnalysisService riskAnalysisService) {
        this.riskAnalysisService = riskAnalysisService;
    }

    /**
     * Calculate the maximum drawdown for a list of prices.
     *
     * @param prices ordered list of prices in JSON array format
     * @return maximum drawdown as decimal (e.g. 0.2 for 20%)
     */
    @PostMapping("/maxdrawdown")
    public double maxDrawdown(@RequestBody List<Double> prices) {
        return this.riskAnalysisService.calculateMaxDrawdown(prices);
    }
}

