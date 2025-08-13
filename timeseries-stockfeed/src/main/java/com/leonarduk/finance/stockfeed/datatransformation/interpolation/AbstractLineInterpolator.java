package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuoteTimeSeries;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

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
            // create a temporary tail containing the last quote and a future
            // placeholder so that interpolation can fill the gap
            List<Bar> tail = Lists.newArrayList(lastQuote,
                    this.calculateFutureValue(lastQuote, toLocalDate));

            // interpolate the tail range and remove any boundary entries which
            // simply duplicate the last known value
            List<Bar> extended = interpolateRange(tail, lastDateInSeries,
                    toLocalDate);
            if (!extended.isEmpty()) {
                extended.remove(extended.size() - 1); // drop placeholder
                // Drop the previous trading day only if it has been reinserted
                if (!extended.isEmpty()
                        && extended.get(0).getEndTime().toLocalDate().isEqual(lastDateInSeries)) {
                    extended.remove(0);
                }
            }

            series.addAll(extended);
        }

        return series;
    }

    @Override
    public List<Bar> interpolate(final List<Bar> series) throws IOException {
        ExtendedHistoricalQuoteTimeSeries timeseries = new ExtendedHistoricalQuoteTimeSeries(series);
        return ((ExtendedHistoricalQuoteTimeSeries) interpolate(timeseries)).getSeries();

    }

    @Override
    public TimeSeries interpolate(final TimeSeries timeseries) {
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
        List<Bar> newseries = Lists.newArrayList();
        if (series.isEmpty()) {
            return newseries;
        }

        final Iterator<LocalDate> dateIter = DateUtils.getLocalDateIterator(oldestDate, mostRecentDate);
        // assume series is already sorted chronologically
        int index = 0;
        Bar currentQuote = series.get(index);
        newseries.add(currentQuote);

        while (dateIter.hasNext() && index + 1 < series.size()) {
            LocalDate currentDate = dateIter.next();

            // skip the date already represented by the current quote
            if (currentDate.isEqual(currentQuote.getEndTime().toLocalDate())) {
                continue;
            }

            final Bar nextQuote = series.get(index + 1);
            final LocalDate nextQuoteDate = nextQuote.getEndTime().toLocalDate();

            if (currentDate.isBefore(nextQuoteDate)) {
                if (nextQuote.getEndTime().isAfter(currentQuote.getEndTime())) {
                    newseries.add(this.createSyntheticBar(currentQuote, currentDate, nextQuote));
                }
            } else if (currentDate.isEqual(nextQuoteDate)) {
                currentQuote = nextQuote;
                index++;
                newseries.add(currentQuote);
            }
        }

        return newseries;
    }

    public abstract Bar createSyntheticQuote(Bar currentQuote, LocalDate currentDate, Bar nextQuote) throws IOException;

    public abstract Bar createSyntheticBar(Bar currentQuote, LocalDate currentDate, Bar nextQuote);
}
