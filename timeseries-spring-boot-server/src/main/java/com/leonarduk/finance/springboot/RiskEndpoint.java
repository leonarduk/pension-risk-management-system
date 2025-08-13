package com.leonarduk.finance.springboot;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * REST endpoint exposing risk related calculations.
 */
@RestController
@RequestMapping("/risk")
public class RiskEndpoint {

    /**
     * Calculate historical simulation Value at Risk (VaR) for a series of returns.
     *
     * @param body JSON payload containing a list of returns under the key "returns".
     * @param confidenceLevel Confidence level for the VaR (default 0.95).
     * @return Map containing the calculated VaR value under key "var".
     */
    @PostMapping("/historic-var")
    public Map<String, Double> historicVar(@RequestBody Map<String, List<Double>> body,
                                           @RequestParam(name = "confidenceLevel", defaultValue = "0.95") double confidenceLevel) {
        List<Double> returns = body.get("returns");
        if (returns == null || returns.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "returns must not be empty");
        }

        returns.sort(Comparator.naturalOrder());
        int index = (int) Math.floor((1 - confidenceLevel) * returns.size());
        index = Math.max(Math.min(index, returns.size() - 1), 0);
        double var = returns.get(index);
        return Collections.singletonMap("var", var);
    }
}
