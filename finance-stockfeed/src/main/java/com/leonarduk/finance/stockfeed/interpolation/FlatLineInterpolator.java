package com.leonarduk.finance.stockfeed.interpolation;

import org.joda.time.LocalDate;

import eu.verdelhan.ta4j.Tick;
import yahoofinance.histquotes.HistoricalQuote;

public class FlatLineInterpolator extends AbstractLineInterpolator {

	@Override
	protected HistoricalQuote calculateFutureValue(final HistoricalQuote lastQuote, final LocalDate today) {
		return new HistoricalQuote(lastQuote, today, "FlatLineInterpolation");
	}

	@Override
	protected HistoricalQuote calculatePastValue(final HistoricalQuote currentQuote, final LocalDate fromDate) {
		return this.createSyntheticQuote(currentQuote, fromDate, currentQuote.getClose(), currentQuote.getOpen(),
				"Copied from " + currentQuote.getDate());
	}

	@Override
	public HistoricalQuote createSyntheticQuote(final HistoricalQuote currentQuote, final LocalDate currentDate,
			final HistoricalQuote nextQuote) {
		return this.createSyntheticQuote(currentQuote, currentDate, currentQuote.getClose(), currentQuote.getOpen(),
				"Copied from " + currentQuote.getDate());
	}

	@Override
	public Tick createSyntheticTick(final Tick currentQuote, final LocalDate currentDate, final Tick nextQuote) {
		return this.createSyntheticTick(currentDate, currentQuote.getClosePrice(), currentQuote.getOpenPrice());
	}

}
