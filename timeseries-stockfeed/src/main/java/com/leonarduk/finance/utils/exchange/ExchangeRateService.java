package com.leonarduk.finance.utils.exchange;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Service for obtaining foreign exchange rates.
 */
public interface ExchangeRateService {

    /**
     * Retrieve the conversion rate from one currency to another.
     *
     * @param fromCurrency the currency we are converting from
     * @param toCurrency   the target currency
     * @return the conversion rate
     * @throws IOException if the rate could not be retrieved
     */
    BigDecimal getRate(String fromCurrency, String toCurrency) throws IOException;
}

