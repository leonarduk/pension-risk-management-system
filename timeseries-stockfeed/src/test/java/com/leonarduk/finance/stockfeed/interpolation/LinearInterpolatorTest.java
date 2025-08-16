package com.leonarduk.finance.stockfeed.interpolation;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.LinearInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.TimeSeriesInterpolator;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.DoubleNumFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.leonarduk.finance.utils.TimeseriesUtils;

public class LinearInterpolatorTest {
    private TimeSeriesInterpolator interpolator;
    private BarSeries series;

    @Before
    public void setUp() throws Exception {
        this.interpolator = new LinearInterpolator();
        List<ExtendedHistoricalQuote> quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-03"), 100.0, 110.0, 90.0,
                        105.0, 1000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-07"), 100.0, 112.0, 92.0,
                        102.0, 5000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-14"), 105.0, 115.0, 95.0,
                        110.0, 2000.0, 0, ""));

        final List<Bar> ticks = quotes.stream().map(ExtendedHistoricalQuote::new).collect(Collectors.toList());
        this.series = new BaseBarSeriesBuilder().withNumFactory(DoubleNumFactory.getInstance()).withBars(ticks).build();
    }

    @Test
    @Ignore
    public void testInterpolateTimeseries() {
        final BarSeries actual = this.interpolator.interpolate(this.series);
        Assert.assertEquals(10, actual.getBarCount());
        Assert.assertEquals(LocalDate.parse("2017-04-03"), actual.getBar(0).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-04"), actual.getBar(1).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-05"), actual.getBar(2).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-07"), actual.getBar(4).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(LocalDate.parse("2017-04-14"), actual.getBar(9).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());

        Assert.assertEquals(DoubleNum.valueOf(104.25), actual.getBar(1).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(103.5), actual.getBar(2).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(102.75), actual.getBar(3).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(102), actual.getBar(4).getClosePrice());
        Assert.assertEquals(DoubleNum.valueOf(106.8).doubleValue(), actual.getBar(5).getClosePrice().doubleValue(),
                0.001);

    }

    @Test
    public void testInterpolationSkipsDuplicatedFinalEntry() throws Exception {
        List<ExtendedHistoricalQuote> quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-07"), 100.0,
                        112.0, 92.0, 102.0, 5000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-03"), 100.0,
                        110.0, 90.0, 105.0, 1000.0, 0, ""));

        List<Bar> base = quotes.stream().map(ExtendedHistoricalQuote::new)
                .collect(Collectors.toList());
        List<Bar> extended = new FlatLineInterpolator().extendToToDate(base,
                LocalDate.parse("2017-04-14"));

        BarSeries ts = new LinearInterpolator().interpolate(new BaseBarSeriesBuilder().withNumFactory(DoubleNumFactory.getInstance()).withBars(extended).build());
        Assert.assertEquals(LocalDate.parse("2017-04-13"),
                ts.getBar(ts.getBarCount() - 1).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
    }

    @Test
    public void testInterpolateSkipsDuplicatedFinalEntry() {
        BarSeries actual = this.interpolator.interpolate(this.series);
        LocalDate finalDate = LocalDate.parse("2017-04-14");
        int count = 0;
        for (int i = 0; i < actual.getBarCount(); i++) {
            if (actual.getBar(i).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().equals(finalDate)) {
                count++;
            }
        }
        Assert.assertEquals(1, count);
    }

    @Test
    public void testExtendToToDateExtrapolatesUsingSlope() throws Exception {
        List<Bar> base = new ArrayList<>();
        base.add(new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2024-01-02"), 100.0, 100.0, 100.0,
                100.0, 0.0, 0, ""));
        base.add(new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2024-01-03"), 110.0, 110.0, 110.0,
                110.0, 0.0, 0, ""));
        base.sort(TimeseriesUtils.getComparator());

        List<Bar> extended = new LinearInterpolator().extendToToDate(base, LocalDate.parse("2024-01-05"));
        extended.sort(TimeseriesUtils.getComparator());
        Bar last = extended.get(extended.size() - 1);
        Assert.assertEquals(LocalDate.parse("2024-01-04"), last.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(120.0, last.getClosePrice().doubleValue(), 0.0001);
    }

    @Test
    public void testExtendToFromDateExtrapolatesUsingSlope() throws Exception {
        List<Bar> base = new ArrayList<>();
        base.add(new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2024-01-03"), 110.0, 110.0, 110.0,
                110.0, 0.0, 0, ""));
        base.add(new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2024-01-04"), 120.0, 120.0, 120.0,
                120.0, 0.0, 0, ""));
        base.sort(TimeseriesUtils.getComparator());

        List<Bar> extended = new LinearInterpolator().extendToFromDate(base, LocalDate.parse("2024-01-02"));
        extended.sort(TimeseriesUtils.getComparator());
        Bar first = extended.get(0);
        Assert.assertEquals(LocalDate.parse("2024-01-02"), first.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assert.assertEquals(100.0, first.getClosePrice().doubleValue(), 0.0001);
    }

}
