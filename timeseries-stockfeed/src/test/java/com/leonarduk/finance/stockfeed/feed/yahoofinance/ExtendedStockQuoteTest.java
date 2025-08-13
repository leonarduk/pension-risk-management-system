package com.leonarduk.finance.stockfeed.feed.yahoofinance;

import com.leonarduk.finance.stockfeed.Instrument;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class ExtendedStockQuoteTest {

    @Test
    public void isPopulatedReturnsTrueWhenFieldsPresent() {
        ExtendedStockQuote quote = new ExtendedStockQuote(
                null, null, null, null, null, null, null, Instrument.UNKNOWN, null, null,
                null, null, BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(100), null, null,
                null, null, null, null);
        Assert.assertTrue(quote.isPopulated());
    }

    @Test
    public void isPopulatedReturnsFalseWhenFieldsMissing() {
        ExtendedStockQuote quote = new ExtendedStockQuote(
                null, null, null, null, null, null, null, Instrument.UNKNOWN, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null);
        Assert.assertFalse(quote.isPopulated());
    }
}
