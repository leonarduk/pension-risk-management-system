package com.leonarduk.finance.springboot;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * REST endpoint exposing risk related calculations.
 */
@RestController
@RequestMapping("/risk")
public class RiskEndpoint {

    @Autowired
    private MessageSource messageSource;

    /**
     * Calculate historical simulation Value at Risk (VaR) for a series of returns.
     *
     * @param body JSON payload containing a list of returns under the key "returns".
     * @param confidenceLevel Confidence level for the VaR (default 0.95).
     * @param acceptLanguage Optional Accept-Language header to specify locale.
     * @param lang Optional query parameter to specify locale.
     * @return Map containing the calculated VaR value under key "var".
     */
    @PostMapping("/historic-var")
    public Map<String, Double> historicVar(@RequestBody Map<String, List<Double>> body,
                                           @RequestParam(name = "confidenceLevel", defaultValue = "0.95") double confidenceLevel,
                                           @RequestHeader(name = "Accept-Language", required = false) String acceptLanguage,
                                           @RequestParam(name = "lang", required = false) String lang) {

        Locale locale = Locale.getDefault();
        if (StringUtils.isNotBlank(lang)) {
            locale = Locale.forLanguageTag(lang);
        } else if (StringUtils.isNotBlank(acceptLanguage)) {
            locale = Locale.forLanguageTag(acceptLanguage);
        }
        LocaleContextHolder.setLocale(locale);

        List<Double> returns = body.get("returns");
        if (returns == null || returns.isEmpty()) {
            String message = messageSource.getMessage("returns.empty", null, locale);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        returns.sort(Comparator.naturalOrder());
        int index = (int) Math.floor((1 - confidenceLevel) * returns.size());
        index = Math.max(Math.min(index, returns.size() - 1), 0);
        double var = returns.get(index);
        return Collections.singletonMap("var", var);
    }
}
