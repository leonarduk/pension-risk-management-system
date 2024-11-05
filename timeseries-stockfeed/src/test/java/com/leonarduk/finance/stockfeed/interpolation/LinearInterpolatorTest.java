package com.leonarduk.finance.stockfeed.interpolation;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.LinearInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.TimeSeriesInterpolator;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LinearInterpolatorTest {
    private TimeSeriesInterpolator interpolator;
    private TimeSeries series;
    private List<ExtendedHistoricalQuote> quotes;

    @Before
    public void setUp() throws Exception {
        this.interpolator = new LinearInterpolator();
        quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-03"), 100.0, 110.0, 90.0,
                        105.0, 1000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-07"), 100.0, 112.0, 92.0,
                        102.0, 5000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-14"), 105.0, 115.0, 95.0,
                        110.0, 2000.0, 0, ""));

        final List<Bar> ticks = quotes.stream().map(q -> new ExtendedHistoricalQuote(q)).collect(Collectors.toList());
        this.series = new BaseTimeSeries(ticks);
    }

    @Test
    @Ignore
    public void testInterpolateTimeseries() {
        final TimeSeries actual = this.interpolator.interpolate(this.series);
        Assert.assertEquals(10, actual.getBarCount());
        Assert.assertEquals(LocalDate.parse("2017-04-03"), actual.getBar(0).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-04"), actual.getBar(1).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-05"), actual.getBar(2).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-07"), actual.getBar(4).getEndTime().toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-14"), actual.getBar(9).getEndTime().toLocalDate());

        Assert.assertEquals(DoubleNum.valueOf(104.25), actual.getBar(1).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(103.5), actual.getBar(2).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(102.75), actual.getBar(3).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(102), actual.getBar(4).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(106.8).doubleValue(), actual.getBar(5).getClosePrice().doubleValue(),
                0.001);

    }

}
