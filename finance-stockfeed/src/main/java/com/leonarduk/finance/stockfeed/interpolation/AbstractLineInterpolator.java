package com.leonarduk.finance.stockfeed.interpolation;

import java.util.Iterator;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;

public abstract class AbstractLineInterpolator implements TimeSeriesInterpolator {

	protected Tick createSyntheticTick(final LocalDate currentDate, final Decimal newClosePrice,
			final Decimal newOpenPrice) {
		return new Tick(currentDate.toDateTimeAtCurrentTime(), newOpenPrice,
				Decimal.valueOf(Math.max(newOpenPrice.toDouble(), newClosePrice.toDouble())),
				Decimal.valueOf(Math.min(newOpenPrice.toDouble(), newClosePrice.toDouble())), newClosePrice,
				Decimal.NaN);
	}

	public abstract Tick createSyntheticTick(final Tick currentQuote, final LocalDate currentDate, Tick nextQuote);

	@Override
	public TimeSeries interpolate(final TimeSeries series) {
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
