package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuoteTimeSeries;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractLineInterpolator implements TimeSeriesInterpolator {

    protected abstract Bar calculateFutureValue(Bar lastQuote, LocalDate toLocalDate);

    protected abstract Bar calculatePastValue(final Bar firstQuote, final LocalDate fromDate) throws IOException;

    public List<Bar> extendToFromDate(final List<Bar> series, final LocalDate fromDate) throws IOException {
        if (series.isEmpty()) {
            return series;
        }
        final Bar firstQuote = TimeseriesUtils.getOldestQuote(series);
        final LocalDate firstDateInSeries = firstQuote.getEndTime().toLocalDate();
        if (firstDateInSeries.isAfter(fromDate)) {
            series.add(this.calculatePastValue(firstQuote, fromDate));
            interpolateRange(series, fromDate, firstDateInSeries);
        }

        return series;
    }

    public List<Bar> extendToToDate(final List<Bar> series, final LocalDate toLocalDate) throws IOException {
        if (series.isEmpty()) {
            return series;
        }
        final Bar lastQuote = TimeseriesUtils.getMostRecentQuote(series);
        final LocalDate lastDateInSeries = lastQuote.getEndTime().toLocalDate();
        if (lastDateInSeries.isBefore(toLocalDate)) {
            series.add(this.calculateFutureValue(lastQuote, toLocalDate));
            interpolateRange(series, lastDateInSeries, toLocalDate);

        }

        return series;
    }

    @Override
    public List<Bar> interpolate(final List<Bar> series) throws IOException {
        ExtendedHistoricalQuoteTimeSeries timeseries = new ExtendedHistoricalQuoteTimeSeries(series);
        return ((ExtendedHistoricalQuoteTimeSeries) interpolate(timeseries)).getSeries();

    }

    @Override
    public BarSeries interpolate(final BarSeries timeseries) {
        if (timeseries.getEndIndex() < 0) {
            return timeseries;
        }

        List<Bar> series = timeseries.getBarData();
        TimeseriesUtils.sortQuoteList(series);

        final LocalDate oldestDate = TimeseriesUtils.getOldestQuote(series).getEndTime().toLocalDate();
        final LocalDate mostRecentDate = TimeseriesUtils.getMostRecentQuote(series).getEndTime().toLocalDate();

        return new ExtendedHistoricalQuoteTimeSeries(interpolateRange(series, oldestDate, mostRecentDate));
    }

    public List<Bar> interpolateRange(List<Bar> series, final LocalDate oldestDate, final LocalDate mostRecentDate) {
        LocalDate currentDate = oldestDate;

        final Iterator<LocalDate> dateIter = DateUtils.getLocalDateIterator(oldestDate, mostRecentDate);
        final Iterator<Bar> seriesIter = series.iterator();
        Bar currentQuote = seriesIter.next();

        List<Bar> newseries = Lists.newArrayList();
        // until the end
        while (currentDate.isBefore(mostRecentDate) && seriesIter.hasNext()) {
            final Bar nextQuote = seriesIter.next();
            currentDate = dateIter.next();

            // Mon , Tue, Fri, Tues
            // until we match this date
            while (nextQuote.getEndTime().toLocalDate().isAfter(currentDate)) {
                if (nextQuote.getEndTime().isAfter(currentQuote.getEndTime())) {
                    newseries.add(this.createSyntheticBar(currentQuote, currentDate, nextQuote));
                }
                currentDate = dateIter.next();
            }

            // matched next one
            currentQuote = nextQuote;
            newseries.add(currentQuote);
        }
        return newseries;
    }

    public abstract Bar createSyntheticQuote(Bar currentQuote, LocalDate currentDate, Bar nextQuote) throws IOException;

    public abstract Bar createSyntheticBar(Bar currentQuote, LocalDate currentDate, Bar nextQuote);
}
