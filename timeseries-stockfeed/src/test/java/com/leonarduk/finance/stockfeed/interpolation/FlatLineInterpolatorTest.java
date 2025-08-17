package com.leonarduk.finance.stockfeed.interpolation;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.TimeSeriesInterpolator;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DoubleNumFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FlatLineInterpolatorTest {
    private TimeSeriesInterpolator interpolator;
    private BarSeries series;

    @BeforeEach
    public void setUp() throws Exception {
        this.interpolator = new FlatLineInterpolator();
        List<ExtendedHistoricalQuote> quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-14"), 105.0, 115.0, 95.0,
                        110.0, 2000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-07"), 100.0, 112.0, 92.0,
                        102.0, 5000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-03"), 100.0, 110.0, 90.0,
                        105.0, 1000.0, 0, ""));

        final List<Bar> ticks = quotes.stream().map(ExtendedHistoricalQuote::new).collect(Collectors.toList());
        this.series = new BaseBarSeriesBuilder().withNumFactory(DoubleNumFactory.getInstance()).withBars(ticks).build();
    }

    @Test
    public void testInterpolateTimeSeries() {

        final BarSeries actual = this.interpolator.interpolate(this.series);
        Assertions.assertEquals(10, actual.getBarCount());
        Assertions.assertEquals(LocalDate.parse("2017-04-03"), actual.getBar(0).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assertions.assertEquals(LocalDate.parse("2017-04-04"), actual.getBar(1).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assertions.assertEquals(LocalDate.parse("2017-04-05"), actual.getBar(2).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assertions.assertEquals(LocalDate.parse("2017-04-07"), actual.getBar(4).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());
        Assertions.assertEquals(LocalDate.parse("2017-04-14"), actual.getBar(9).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate());

        Assertions.assertEquals(actual.getBar(0).getClosePrice(), actual.getBar(1).getClosePrice());
        Assertions.assertEquals(actual.getBar(4).getClosePrice(), actual.getBar(5).getClosePrice());

    }

    @Test
    public void testExtendToToDateDoesNotDuplicateFinalDate() throws Exception {
        List<ExtendedHistoricalQuote> quotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-07"), 100.0,
                        112.0, 92.0, 102.0, 5000.0, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2017-04-03"), 100.0,
                        110.0, 90.0, 105.0, 1000.0, 0, ""));

        List<Bar> base = quotes.stream().map(ExtendedHistoricalQuote::new)
                .collect(Collectors.toList());
        base.sort(TimeseriesUtils.getComparator());

        List<Bar> extended = new FlatLineInterpolator().extendToToDate(base,
                LocalDate.parse("2017-04-14"));

        LocalDate lastDate = extended.get(extended.size() - 1).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();

        Assertions.assertEquals(6, extended.size());
        Assertions.assertEquals(LocalDate.parse("2017-04-13"), lastDate);
        long occurrences = extended.stream()
                .filter(bar -> bar.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().isEqual(lastDate)).count();
        Assertions.assertEquals(1, occurrences);
    }

    @Test
    public void testInterpolationSkipsDuplicatedFinalEntry() {
        BarSeries actual = this.interpolator.interpolate(this.series);
        LocalDate finalDate = LocalDate.parse("2017-04-14");
        int count = 0;
        for (int i = 0; i < actual.getBarCount(); i++) {
            if (actual.getBar(i).getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().equals(finalDate)) {
                count++;
            }
        }
        Assertions.assertEquals(1, count);
    }

}
