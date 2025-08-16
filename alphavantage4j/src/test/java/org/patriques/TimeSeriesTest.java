package org.patriques;

import org.junit.Test;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.input.ApiParameter;
import org.patriques.output.AlphaVantageException;
import org.patriques.output.timeseries.*;
import org.patriques.output.timeseries.data.StockData;


import static org.junit.Assert.*;

public class TimeSeriesTest {

    private static final String ERROR_JSON = "{\"Error Message\":\"Test error\"}";

    private ApiConnector connectorWith(final String json) {
        return (ApiParameter... params) -> json;
    }

    @Test
    public void testIntraDayWithAndWithoutOutputSize() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Time Series (1min)\":{\"2024-01-01 00:00:00\":{" +
                "\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\",\"5. volume\":\"1000\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        IntraDay result = ts.intraDay("IBM", Interval.ONE_MIN, OutputSize.COMPACT);
        assertEquals("test", result.getMetaData().get("1. Information"));
        StockData data = result.getStockData().get(0);
        assertEquals(1.0, data.getOpen(), 0.0);
        // call overloaded method without output size
        result = ts.intraDay("IBM", Interval.ONE_MIN);
        assertNotNull(result.getStockData());
    }

    @Test(expected = AlphaVantageException.class)
    public void testIntraDayError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.intraDay("IBM", Interval.ONE_MIN);
    }

    @Test
    public void testDailyWithAndWithoutOutputSize() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Time Series (Daily)\":{\"2024-01-01\":{\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\",\"5. volume\":\"1000\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        Daily daily = ts.daily("IBM", OutputSize.COMPACT);
        assertEquals(1.0, daily.getStockData().get(0).getOpen(), 0.0);
        daily = ts.daily("IBM");
        assertEquals(1.0, daily.getStockData().get(0).getOpen(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testDailyError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.daily("IBM");
    }

    @Test
    public void testDailyAdjustedWithAndWithoutOutputSize() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Time Series (Daily)\":{\"2024-01-01\":{" +
                "\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\"," +
                "\"5. adjusted close\":\"1.0\",\"6. volume\":\"1000\",\"7. dividend amount\":\"0.0\",\"8. split coefficient\":\"1.0\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        DailyAdjusted da = ts.dailyAdjusted("IBM", OutputSize.COMPACT);
        assertEquals(1.0, da.getStockData().get(0).getAdjustedClose(), 0.0);
        da = ts.dailyAdjusted("IBM");
        assertEquals(1.0, da.getStockData().get(0).getAdjustedClose(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testDailyAdjustedError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.dailyAdjusted("IBM");
    }

    @Test
    public void testWeekly() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Weekly Time Series\":{\"2024-01-05\":{\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\",\"5. volume\":\"1000\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        Weekly weekly = ts.weekly("IBM");
        assertEquals(1.0, weekly.getStockData().get(0).getOpen(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testWeeklyError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.weekly("IBM");
    }

    @Test
    public void testWeeklyAdjusted() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Weekly Adjusted Time Series\":{\"2024-01-05\":{" +
                "\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\"," +
                "\"5. adjusted close\":\"1.0\",\"6. volume\":\"1000\",\"7. dividend amount\":\"0.0\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        WeeklyAdjusted wa = ts.weeklyAdjusted("IBM");
        assertEquals(1.0, wa.getStockData().get(0).getAdjustedClose(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testWeeklyAdjustedError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.weeklyAdjusted("IBM");
    }

    @Test
    public void testMonthly() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Monthly Time Series\":{\"2024-01-31\":{\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\",\"5. volume\":\"1000\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        Monthly monthly = ts.monthly("IBM");
        assertEquals(1.0, monthly.getStockData().get(0).getOpen(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testMonthlyError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.monthly("IBM");
    }

    @Test
    public void testMonthlyAdjusted() {
        String json = "{\"Meta Data\":{\"1. Information\":\"test\"}," +
                "\"Monthly Adjusted Time Series\":{\"2024-01-31\":{" +
                "\"1. open\":\"1.0\",\"2. high\":\"1.1\",\"3. low\":\"0.9\",\"4. close\":\"1.0\"," +
                "\"5. adjusted close\":\"1.0\",\"6. volume\":\"1000\",\"7. dividend amount\":\"0.0\"}}}";
        TimeSeries ts = new TimeSeries(connectorWith(json));
        MonthlyAdjusted ma = ts.monthlyAdjusted("IBM");
        assertEquals(1.0, ma.getStockData().get(0).getAdjustedClose(), 0.0);
    }

    @Test(expected = AlphaVantageException.class)
    public void testMonthlyAdjustedError() {
        TimeSeries ts = new TimeSeries(connectorWith(ERROR_JSON));
        ts.monthlyAdjusted("IBM");
    }
}

