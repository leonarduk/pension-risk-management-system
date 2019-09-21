package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;

import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuoteTimeSeries;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

public abstract class AbstractLineInterpolator implements TimeSeriesInterpolator {

	protected abstract Bar calculateFutureValue(Bar lastQuote, LocalDate toLocalDate);

	protected abstract Bar calculatePastValue(final Bar firstQuote, final LocalDate fromDate) throws IOException;

	public List<Bar> extendToFromDate(final List<Bar> history, final LocalDate fromDate) throws IOException {
		if (history.isEmpty()) {
			return history;
		}
		final Bar firstQuote = TimeseriesUtils.getOldestQuote(history);
		final LocalDate firstDateInSeries = firstQuote.getEndTime().toLocalDate();
		if (firstDateInSeries.isAfter(fromDate)) {
			history.add(this.calculatePastValue(firstQuote, fromDate));
		}

		return history;
	}

	public List<Bar> extendToToDate(final List<Bar> list, final LocalDate toLocalDate) throws IOException {
		if (list.isEmpty()) {
			return list;
		}
		final Bar lastQuote = TimeseriesUtils.getMostRecentQuote(list);
		final LocalDate lastDateInSeries = lastQuote.getEndTime().toLocalDate();
		if (lastDateInSeries.isBefore(toLocalDate)) {
			list.add(this.calculateFutureValue(lastQuote, toLocalDate));
		}

		return list;
	}

	@Override
	public List<Bar> interpolate(final List<Bar> series) throws IOException {
		ExtendedHistoricalQuoteTimeSeries timeseries = new ExtendedHistoricalQuoteTimeSeries(series);
		return ((ExtendedHistoricalQuoteTimeSeries) interpolate(timeseries, new ExtendedHistoricalQuoteTimeSeries()))
				.getSeries();

	}

	@Override
	public TimeSeries interpolate(final TimeSeries series) {
		return interpolate(series, new ExtendedHistoricalQuoteTimeSeries());
	}

	public TimeSeries interpolate(final TimeSeries series, TimeSeries newSeries) {

		if (series.getEndIndex() < 0) {
			return series;
		}
		final Bar oldestQuote = series.getFirstBar();

		final Iterator<Bar> seriesIter = TimeseriesUtils.getTimeSeriesIterator(series);
		Bar currentQuote = seriesIter.next();

		final Iterator<LocalDate> dateIter = DateUtils.getLocalDateIterator(currentQuote.getEndTime().toLocalDate(),
				oldestQuote.getEndTime().toLocalDate());
		LocalDate currentDate = dateIter.next();

		newSeries.addBar(series.getLastBar());

		// until the end
		while (currentDate.isBefore(oldestQuote.getEndTime().toLocalDate())) {
			final Bar nextQuote = seriesIter.next();
			currentDate = dateIter.next();

			// Mon , Tue, Fri, Tues
			// until we match this date
			while (nextQuote.getEndTime().toLocalDate().isAfter(currentDate)) {
				if (nextQuote.getEndTime().isAfter(currentQuote.getEndTime())) {
					newSeries.addBar(this.createSyntheticBar(currentQuote, currentDate, nextQuote));
				}
				currentDate = dateIter.next();
			}

			// matched next one
			currentQuote = nextQuote;
			newSeries.addBar(currentQuote);
		}
		return newSeries;
	}

	public abstract Bar createSyntheticQuote(Bar currentQuote, LocalDate currentDate, Bar nextQuote) throws IOException;

	public abstract Bar createSyntheticBar(Bar currentQuote, LocalDate currentDate, Bar nextQuote);
}
