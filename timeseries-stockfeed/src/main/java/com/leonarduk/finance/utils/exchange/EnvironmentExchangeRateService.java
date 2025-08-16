package com.leonarduk.finance.utils.exchange;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Simple implementation of {@link ExchangeRateService} that retrieves rates
 * from system properties or environment variables. The lookup key uses the
 * format {@code FROM_TO_RATE}, e.g. {@code USD_GBP_RATE}.
 * If no value is found a rate of {@link BigDecimal#ONE} is returned.
 */
public class EnvironmentExchangeRateService implements ExchangeRateService {

    @Override
    public BigDecimal getRate(String fromCurrency, String toCurrency) throws IOException {
        String key = (fromCurrency + "_" + toCurrency + "_RATE").toUpperCase();
        String value = System.getProperty(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            return BigDecimal.ONE;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid exchange rate for " + key, e);
        }
    }
}

