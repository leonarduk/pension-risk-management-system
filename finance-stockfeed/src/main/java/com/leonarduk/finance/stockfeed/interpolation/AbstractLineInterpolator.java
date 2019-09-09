package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseTimeSeries;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.num.PrecisionNum;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.yahoo.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

public abstract class AbstractLineInterpolator implements TimeSeriesInterpolator {

	protected abstract ExtendedHistoricalQuote calculateFutureValue(ExtendedHistoricalQuote lastQuote, LocalDate today);

	protected abstract ExtendedHistoricalQuote calculatePastValue(final ExtendedHistoricalQuote firstQuote,
			final LocalDate fromDate) throws IOException;

	protected ExtendedHistoricalQuote createSyntheticQuote(final ExtendedHistoricalQuote currentQuote,
			final LocalDate currentDate, final BigDecimal newClosePriceRaw, final BigDecimal newOpenPriceRaw,
			final String comment) throws IOException {
		final BigDecimal newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
		final BigDecimal newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);
		return new ExtendedHistoricalQuote(currentQuote.getInstrument(), currentDate, newOpenPrice,
				newClosePrice.min(newOpenPrice), newClosePrice.max(newOpenPrice), newClosePrice, newClosePrice, 0L,
				comment);
	}

	public abstract ExtendedHistoricalQuote createSyntheticQuote(final ExtendedHistoricalQuote currentQuote,
			final LocalDate currentDate, ExtendedHistoricalQuote nextQuote) throws IOException;

	protected Bar createSyntheticBar(final LocalDate currentDate, final Double newClosePriceRaw,
			final Double newOpenPriceRaw) {

		final Double newClosePrice = NumberUtils.roundDecimal(newClosePriceRaw);
		final Double newOpenPrice = NumberUtils.roundDecimal(newOpenPriceRaw);

		ZonedDateTime endTime = currentDate.atStartOfDay(ZoneId.of("Europe/London"));
		Function<Number, Num> numFunction = new Function<Number, Num>() {

			@Override
			public Num apply(Number t) {
				return PrecisionNum.valueOf(t.doubleValue());
			}
		};
		return new BaseBar(endTime, newOpenPrice,
				Double.valueOf(Math.max(newOpenPrice.doubleValue(), newClosePrice.doubleValue())),
				Double.valueOf(Math.min(newOpenPrice.doubleValue(), newClosePrice.doubleValue())), newClosePrice,
				Double.NaN, numFunction);
	}

	public abstract Bar createSyntheticBar(final Bar currentQuote, final LocalDate currentDate, Bar nextQuote);

	public List<ExtendedHistoricalQuote> extendToFromDate(final List<ExtendedHistoricalQuote> history,
			final LocalDate fromDate) throws IOException {
		if (history.isEmpty()) {
			return history;
		}
		final ExtendedHistoricalQuote firstQuote = TimeseriesUtils.getOldestQuote(history);
		final LocalDate firstDateInSeries = firstQuote.getLocaldate();
		if (firstDateInSeries.isAfter(fromDate)) {
			history.add(this.calculatePastValue(firstQuote, fromDate));
			TimeseriesUtils.sortQuoteList(history);
			return this.interpolate(history);
		}

		return history;
	}

	public List<ExtendedHistoricalQuote> extendToToDate(final List<ExtendedHistoricalQuote> history,
			final LocalDate today) throws IOException {
		if (history.isEmpty()) {
			return history;
		}
		final ExtendedHistoricalQuote lastQuote = TimeseriesUtils.getMostRecentQuote(history);
		final LocalDate lastDateInSeries = lastQuote.getLocaldate();
		if (lastDateInSeries.isBefore(today)) {
			history.add(this.calculateFutureValue(lastQuote, today));
			TimeseriesUtils.sortQuoteList(history);
			return this.interpolate(history);
		}

		return history;
	}

	@Override
	public List<ExtendedHistoricalQuote> interpolate(final List<ExtendedHistoricalQuote> series) throws IOException {
		if ((series == null) || (series.size() < 2)) {
			return series;
		}
		final List<ExtendedHistoricalQuote> newSeries = Lists.newLinkedList();
		final ExtendedHistoricalQuote oldestQuote = TimeseriesUtils.getOldestQuote(series);

		final Iterator<ExtendedHistoricalQuote> seriesIter = series.iterator();
		ExtendedHistoricalQuote currentQuote = seriesIter.next();

		final LocalDate endDate = oldestQuote.getLocaldate();
		final Iterator<LocalDate> dateIter = DateUtils.getLocalDateIterator(currentQuote.getLocaldate(), endDate);
		LocalDate currentDate = dateIter.next();

		newSeries.add(TimeseriesUtils.getMostRecentQuote(series));

		// until the end
		while (currentDate.isAfter(endDate)) {
			final ExtendedHistoricalQuote nextQuote = seriesIter.next();
			currentDate = dateIter.next();

			// Mon , Tue, Fri, Tues
			// until we match this date
			while (nextQuote.getDate().before(currentDate)) {
				if (nextQuote.getDate().before(currentQuote.getDate())) {
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
		if (series.getEndIndex() < 0) {
			return series;
		}
		final TimeSeries newSeries = new BaseTimeSeries();
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
		}
		return newSeries;
	}

}
