package com.leonarduk.finance.stockfeed.feed;

import com.google.common.collect.Lists;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import java.io.Serial;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Function;

public class ExtendedHistoricalQuoteTimeSeries implements TimeSeries {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -4258117616509944879L;
    private final List<Bar> series;

    public ExtendedHistoricalQuoteTimeSeries() {
        this(Lists.newArrayList());
    }

    public ExtendedHistoricalQuoteTimeSeries(List<Bar> series2) {
        this.series = series2;
    }

    public List<Bar> getSeries() {
        return this.series;
    }

    @Override
    public String getName() {
        if (this.series.isEmpty())
            return "Empty";
        return ((ExtendedHistoricalQuote) this.series.get(0)).getSymbol();
    }

    @Override
    public Bar getBar(int i) {
        return this.series.get(i);
    }

    @Override
    public int getBarCount() {
        return this.series.size();
    }

    @Override
    public List<Bar> getBarData() {
        return this.series;
    }

    @Override
    public int getBeginIndex() {
        return 0;
    }

    @Override
    public int getEndIndex() {
        return this.series.size() - 1;
    }

    @Override
    public void setMaximumBarCount(int maximumBarCount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaximumBarCount() {
        return this.getBarCount();
    }

    @Override
    public int getRemovedBarsCount() {
        return 0;
    }

    @Override
    public void addBar(Bar bar, boolean replace) {
        if (replace)
            throw new UnsupportedOperationException();
        this.series.add(bar);
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBar(ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice, Num closePrice, Num volume,
                       Num amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                       Num closePrice, Num volume) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addBar(Duration timePeriod, ZonedDateTime endTime, Num openPrice, Num highPrice, Num lowPrice,
                       Num closePrice, Num volume, Num amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPrice(Num price) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TimeSeries getSubSeries(int startIndex, int endIndex) {
        return new ExtendedHistoricalQuoteTimeSeries(series.subList(startIndex, endIndex));
    }

    @Override
    public Num numOf(Number number) {
        return DoubleNum.valueOf(number);
    }

    @Override
    public Function<Number, Num> function() {
        throw new UnsupportedOperationException();
    }

}
