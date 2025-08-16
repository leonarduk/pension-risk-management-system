package com.leonarduk.finance.utils;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.exchange.ExchangeRateService;
import org.junit.Assert;
import org.junit.Test;
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

        Assert.assertFalse(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));

        cachedHistory
                .add(new ExtendedHistoricalQuote(Instrument.CASH, fromDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                        BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assert.assertFalse(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));

        cachedHistory.add(new ExtendedHistoricalQuote(Instrument.CASH, toDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assert.assertTrue(TimeseriesUtils.containsDatePoints(cachedHistory, fromDate, toDate));
    }

    @Test
    public final void testSeriesToCsv() {
        final StringBuilder actual = TimeseriesUtils.seriesToCsv(this.getQuotes());
        Assert.assertEquals("date,open,high,low,close,volume,comment\n" + "2017-01-01,12.30,9.30,10.00,12.20,23.00,TestCache\n",
                actual.toString());
    }

    @Test
    public void testSeriesToCsvEscapesCommaInComment() {
        final List<Bar> series = Lists.newArrayList();
        series.add(new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2017-01-01"), BigDecimal.valueOf(12.3),
                BigDecimal.TEN, BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L,
                "Comment, with comma"));
        final StringBuilder actual = TimeseriesUtils.seriesToCsv(series);
        Assert.assertEquals(
                "date,open,high,low,close,volume,comment\n" +
                        "2017-01-01,12.30,9.30,10.00,12.20,23.00,\"Comment, with comma\"\n",
                actual.toString());
    }

    @Test
    public void testGetMissingDataPoints() throws Exception {
        final LocalDate toDate = LocalDate.parse("2017-01-03");
        final LocalDate fromDate = LocalDate.parse("2017-01-01");

        final List<Bar> cachedHistory = Lists.newArrayList();

        Assert.assertEquals(2, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());

        cachedHistory
                .add(new ExtendedHistoricalQuote(Instrument.CASH, fromDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                        BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assert.assertEquals(1, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());

        cachedHistory.add(new ExtendedHistoricalQuote(Instrument.CASH, toDate, BigDecimal.valueOf(12.3), BigDecimal.TEN,
                BigDecimal.valueOf(9.3), BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));

        Assert.assertEquals(0, TimeseriesUtils.getMissingDataPoints(cachedHistory, fromDate, toDate).size());
    }

    @Test
    public void testCleanUpSeriesUsdToGbpConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST1", "L", "EQUITY", "GBP");
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
        Assert.assertEquals(0.8, converted.getClosePrice().doubleValue(), 0.0001);
        Assert.assertEquals("GBP", stock.getCurrency());
    }

    @Test
    public void testCleanUpSeriesUsdToGbxConversion() throws Exception {
        Instrument instrument = Instrument.fromString("TEST2", "L", "EQUITY", "GBX");
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
        Assert.assertEquals(80.0, converted.getClosePrice().doubleValue(), 0.0001);
        Assert.assertEquals("GBX", stock.getCurrency());
    }

}
