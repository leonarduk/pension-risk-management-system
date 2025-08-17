package com.leonarduk.finance.utils;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.exchange.ExchangeRateService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class TimeseriesUtilsTest {

    private List<Bar> getQuotes() {
        final List<Bar> series = Lists.newArrayList();
        series.add(new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2017-01-01"), BigDecimal.valueOf(12.3),
                BigDecimal.TEN, BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L,
                "TestCache"));
        return series;
    }

    @Test
    public void testcontainsDatePoints() throws Exception {
        final LocalDate toDate = LocalDate.parse("2017-01-03");
        final LocalDate fromDate = LocalDate.parse("2017-01-01");

        final List<Bar> cachedHistory = Lists.newArrayList();

        Assertions.assertFalse(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));

        cachedHistory
                .add(new ExtendedHistoricalQuote(Instrument.CASH, fromDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                        BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assertions.assertFalse(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));

        cachedHistory.add(new ExtendedHistoricalQuote(Instrument.CASH, toDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assertions.assertTrue(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));
    }

    @Test
    public final void testSeriesToCsv() {
        final StringBuilder actual = TimeseriesUtils.seriesToCsv(this.getQuotes());
        Assertions.assertEquals("date,open,high,low,close,volume,comment\n" + "2017-01-01,12.30,9.30,10.00,12.20,23.00,TestCache\n",
                actual.toString());
    }

    @Test
    public void testSeriesToCsvEscapesCommaInComment() {
        final List<Bar> series = Lists.newArrayList();
        series.add(new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2017-01-01"), BigDecimal.valueOf(12.3),
                BigDecimal.TEN, BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L,
                "Comment, with comma"));
        final StringBuilder actual = TimeseriesUtils.seriesToCsv(series);
        Assertions.assertEquals(
                "date,open,high,low,close,volume,comment\n" +
                        "2017-01-01,12.30,9.30,10.00,12.20,23.00,\"Comment, with comma\"\n",
                actual.toString());
    }

    @Test
    public void testGetMissingDataPoints() throws Exception {
        final LocalDate toDate = LocalDate.parse("2017-01-03");
        final LocalDate fromDate = LocalDate.parse("2017-01-01");

        final List<Bar> cachedHistory = Lists.newArrayList();

        Assertions.assertEquals(2, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());

        cachedHistory
                .add(new ExtendedHistoricalQuote(Instrument.CASH, fromDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                        BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assertions.assertEquals(1, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());

        cachedHistory.add(new ExtendedHistoricalQuote(Instrument.CASH, toDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assertions.assertEquals(0, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());
    }

    @Test
    public void testCleanUpSeriesGbpToGbxConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_GBP_GBX", "L", "EQUITY", "GBX");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("GBP");

        final int[] calls = {0};
        ExchangeRateService service = (from, to) -> {
            calls[0]++;
            return BigDecimal.ONE; // should not be used
        };
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(100.0, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("GBX", stock.getCurrency());
        Assertions.assertEquals(0, calls[0]);
    }

    @Test
    public void testCleanUpSeriesGbxToGbpConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_GBX_GBP", "L", "EQUITY", "GBP");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("GBX");

        final int[] calls = {0};
        ExchangeRateService service = (from, to) -> {
            calls[0]++;
            return BigDecimal.ONE; // should not be used
        };
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(0.01, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("GBP", stock.getCurrency());
        Assertions.assertEquals(0, calls[0]);
    }

    @Test
    public void testCleanUpSeriesUsdToGbpConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_USD_GBP", "L", "EQUITY", "GBP");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("USD");

        ExchangeRateService service = (from, to) -> BigDecimal.valueOf(0.8);
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(0.8, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("GBP", stock.getCurrency());
    }

    @Test
    public void testCleanUpSeriesUsdToGbxConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_USD_GBX", "L", "EQUITY", "GBX");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("USD");

        ExchangeRateService service = (from, to) -> BigDecimal.valueOf(0.8);
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(80.0, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("GBX", stock.getCurrency());
    }

    @Test
    public void testCleanUpSeriesGbpToUsdConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_GBP_USD", "L", "EQUITY", "USD");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("GBP");

        ExchangeRateService service = (from, to) -> BigDecimal.valueOf(1.25);
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(1.25, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("USD", stock.getCurrency());
    }

    @Test
    public void testCleanUpSeriesGbxToUsdConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_GBX_USD", "L", "EQUITY", "USD");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("GBX");

        ExchangeRateService service = (from, to) -> BigDecimal.valueOf(1.25);
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(0.0125, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("USD", stock.getCurrency());
    }

    @Test
    public void testFallbackPathUsesExchangeService() throws Exception {
        Instrument instrument = Instrument.fromString("TEST_FALLBACK", "L", "EQUITY", "USD");
        List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument, LocalDate.parse("2020-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 1L, ""));
        StockV1 stock = new StockV1(instrument, history);
        stock.setCurrency("CHF");

        final int[] calls = {0};
        ExchangeRateService service = (from, to) -> {
            calls[0]++;
            return BigDecimal.valueOf(1.3);
        };
        TimeseriesUtils.setExchangeRateService(service);

        TimeseriesUtils.cleanUpSeries(Optional.of(stock));
        Bar converted = stock.getHistory().get(0);
        Assertions.assertEquals(1.3, converted.getClosePrice().doubleValue(), 0.0001);
        Assertions.assertEquals("USD", stock.getCurrency());
        Assertions.assertEquals(1, calls[0]);
    }

}
