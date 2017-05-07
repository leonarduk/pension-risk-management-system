package com.leonarduk.finance.stockfeed.interpolation;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.google.common.collect.Lists;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import yahoofinance.histquotes.HistoricalQuote;

public abstract class AbstractLineInterpolator implements TimeSeriesInterpolator {

	protected abstract HistoricalQuote calculateFutureValue(HistoricalQuote lastQuote, LocalDate today);

	protected abstract HistoricalQuote calculatePastValue(final HistoricalQuote firstQuote, final LocalDate fromDate);

	protected HistoricalQuote createSyntheticQuote(final HistoricalQuote currentQuote, final LocalDate currentDate,
			final BigDecimal newClosePrice, final BigDecimal newOpenPrice) {
		return new HistoricalQuote(currentQuote.getInstrument(), currentDate, newOpenPrice,
				newClosePrice.min(newOpenPrice), newClosePrice.max(newOpenPrice), newClosePrice, newClosePrice, 0L,
				"interpolated");
	}

	public abstract HistoricalQuote createSyntheticQuote(final HistoricalQuote currentQuote,
			final LocalDate currentDate, HistoricalQuote nextQuote);

	protected Tick createSyntheticTick(final LocalDate currentDate, final Decimal newClosePrice,
			final Decimal newOpenPrice) {
		return new Tick(currentDate.toDateTimeAtCurrentTime(), newOpenPrice,
				Decimal.valueOf(Math.max(newOpenPrice.toDouble(), newClosePrice.toDouble())),
				Decimal.valueOf(Math.min(newOpenPrice.toDouble(), newClosePrice.toDouble())), newClosePrice,
				Decimal.NaN);
	}

	public abstract Tick createSyntheticTick(final Tick currentQuote, final LocalDate currentDate, Tick nextQuote);

	public List<HistoricalQuote> extendToFromDate(final List<HistoricalQuote> history, final LocalDate fromDate) {
		if (history.isEmpty()) {
			return history;
		}
		final HistoricalQuote firstQuote = TimeseriesUtils.getOldestQuote(history);
		final LocalDate firstDateInSeries = firstQuote.getDate();
		if (firstDateInSeries.isAfter(fromDate)) {
			history.add(this.calculatePastValue(firstQuote, fromDate));
			TimeseriesUtils.sortQuoteList(history);
			return this.interpolate(history);
		}

		return history;
	}

	public List<HistoricalQuote> extendToToDate(final List<HistoricalQuote> history, final LocalDate today) {
		if (history.isEmpty()) {
			return history;
		}
		final HistoricalQuote lastQuote = TimeseriesUtils.getMostRecentQuote(history);
		final LocalDate lastDateInSeries = lastQuote.getDate();
		if (lastDateInSeries.isBefore(today)) {
			history.add(this.calculateFutureValue(lastQuote, today));
			TimeseriesUtils.sortQuoteList(history);
			return this.interpolate(history);
		}

		return history;
	}

	@Override
	public List<HistoricalQuote> interpolate(final List<HistoricalQuote> series) {
		if ((series == null) || (series.size() < 2)) {
			return series;
		}
		final List<HistoricalQuote> newSeries = Lists.newLinkedList();
		final HistoricalQuote oldestQuote = TimeseriesUtils.getOldestQuote(series);

		final Iterator<HistoricalQuote> seriesIter = series.iterator();
		HistoricalQuote currentQuote = seriesIter.next();

		final LocalDate endDate = oldestQuote.getDate();
		final Iterator<LocalDate> dateIter = DateUtils.getLocalDateNewToOldIterator(currentQuote.getDate(), endDate);
		LocalDate currentDate = dateIter.next();

		newSeries.add(TimeseriesUtils.getMostRecentQuote(series));

		// until the end
		while (currentDate.isAfter(endDate)) {
			final HistoricalQuote nextQuote = seriesIter.next();
			currentDate = dateIter.next();

			// Mon , Tue, Fri, Tues
			// until we match this date
			while (nextQuote.getDate().isBefore(currentDate)) {
				if (nextQuote.getDate().isBefore(currentQuote.getDate())) {
					newSeries.add(this.createSyntheticQuote(currentQuote, currentDate, nextQuote));
				}
				currentDate = dateIter.next();
			}

			// matched next one
			newSeries.add(nextQuote);
			currentQuote = nextQuote;
		}
		return newSeries;
	}

	@Override
	public TimeSeries interpolate(final TimeSeries series) {
		if (series.getEnd() < 0) {
			return series;
		}
		final TimeSeries newSeries = new TimeSeries(Period.days(1));
		final Tick oldestQuote = series.getFirstTick();

		final Iterator<Tick> seriesIter = TimeseriesUtils.getTimeSeriesIterator(series);
		Tick currentQuote = seriesIter.next();

		final Iterator<LocalDate> dateIter = DateUtils.getLocalDateIterator(currentQuote.getEndTime().toLocalDate(),
				oldestQuote.getEndTime().toLocalDate());
		LocalDate currentDate = dateIter.next();

		newSeries.addTick(series.getLastTick());

		// until the end
		while (currentDate.isBefore(oldestQuote.getEndTime().toLocalDate())) {
			final Tick nextQuote = seriesIter.next();
			currentDate = dateIter.next();

			// Mon , Tue, Fri, Tues
			// until we match this date
			while (nextQuote.getEndTime().toLocalDate().isAfter(currentDate)) {
				if (nextQuote.getEndTime().isAfter(currentQuote.getEndTime())) {
					newSeries.addTick(this.createSyntheticTick(currentQuote, currentDate, nextQuote));
				}
				currentDate = dateIter.next();
			}

			// matched next one
			newSeries.addTick(nextQuote);
			currentQuote = nextQuote;
		}
		return newSeries;
	}

}
