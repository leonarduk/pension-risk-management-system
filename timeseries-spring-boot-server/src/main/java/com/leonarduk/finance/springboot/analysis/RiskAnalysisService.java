package com.leonarduk.finance.springboot.analysis;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Service providing basic risk analysis metrics.
 */
@Service
public class RiskAnalysisService {

    /**
     * Calculate the maximum drawdown of a series of prices.
     * Max drawdown is defined as the largest peak-to-trough decline
     * as a fraction of the peak value.
     *
     * @param prices ordered list of prices
     * @return max drawdown as a decimal fraction (e.g. 0.2 for 20%)
     */
    public double calculateMaxDrawdown(List<Double> prices) {
        if (prices == null || prices.isEmpty()) {
            return 0.0;
        }
        double peak = prices.get(0);
        double maxDrawdown = 0.0;
        for (double price : prices) {
            if (price > peak) {
                peak = price;
            }
            double drawdown = (peak - price) / peak;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        return maxDrawdown;
    }
}

