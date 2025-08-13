package com.leonarduk.finance.stockfeed.interpolation;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.TimeSeriesInterpolator;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests that interpolation skips recognised UK bank holidays.
 */
public class HolidayInterpolationTest {
    private TimeSeries series;

    @Before
    public void setUp() {
        List<ExtendedHistoricalQuote> quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2022-12-23"), 100.0, 100.0, 100.0,
                        100.0, 1000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2022-12-28"), 110.0, 110.0, 110.0,
                        110.0, 1000.0, 0, ""));
        List<Bar> ticks = quotes.stream().map(ExtendedHistoricalQuote::new).collect(Collectors.toList());
        this.series = new BaseTimeSeries(ticks);
    }

    @Test
    public void testSkipsUKBankHolidaysFlatLine() {
        TimeSeriesInterpolator flat = new FlatLineInterpolator();
        TimeSeries actual = flat.interpolate(this.series);
        Assert.assertEquals(2, actual.getBarCount());
        Assert.assertEquals(LocalDate.parse("2022-12-23"), actual.getBar(0).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2022-12-28"), actual.getBar(1).getEndTime().toLocalDate());
        for (int i = 0; i < actual.getBarCount(); i++) {
            LocalDate date = actual.getBar(i).getEndTime().toLocalDate();
            Assert.assertFalse(date.equals(LocalDate.parse("2022-12-26")) || date.equals(LocalDate.parse("2022-12-27")));
        }
    }

    @Test
    public void testSkipsUKBankHolidaysLinear() {
        TimeSeriesInterpolator linear = new com.leonarduk.finance.stockfeed.datatransformation.interpolation.LinearInterpolator();
        TimeSeries actual = linear.interpolate(this.series);
        Assert.assertEquals(2, actual.getBarCount());
        Assert.assertEquals(LocalDate.parse("2022-12-23"), actual.getBar(0).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2022-12-28"), actual.getBar(1).getEndTime().toLocalDate());
        for (int i = 0; i < actual.getBarCount(); i++) {
            LocalDate date = actual.getBar(i).getEndTime().toLocalDate();
            Assert.assertFalse(date.equals(LocalDate.parse("2022-12-26")) || date.equals(LocalDate.parse("2022-12-27")));
        }
    }
}
