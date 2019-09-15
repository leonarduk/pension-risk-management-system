package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.time.LocalDate;

import org.ta4j.core.Bar;

import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;

public class FlatLineInterpolator extends AbstractLineInterpolator {

	@Override
	protected ExtendedHistoricalQuote calculateFutureValue(final ExtendedHistoricalQuote lastQuote,
			final LocalDate today) {
		return new ExtendedHistoricalQuote(lastQuote, today, "FlatLineInterpolation");
	}

	@Override
	protected ExtendedHistoricalQuote calculatePastValue(final ExtendedHistoricalQuote currentQuote,
			final LocalDate fromDate) throws IOException {
		return this.createSyntheticQuote(currentQuote, fromDate, currentQuote.getClose(), currentQuote.getOpen(),
				"Copied from " + currentQuote.getDate());
	}

	@Override
	public ExtendedHistoricalQuote createSyntheticQuote(final ExtendedHistoricalQuote currentQuote,
			final LocalDate currentDate, final ExtendedHistoricalQuote nextQuote) throws IOException {
		return this.createSyntheticQuote(currentQuote, currentDate, currentQuote.getClose(), currentQuote.getOpen(),
				"Copied from " + currentQuote.getDate());
	}

	@Override
	public Bar createSyntheticBar(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote) {
		return this.createSyntheticBar(currentDate, currentQuote.getClosePrice().doubleValue(),
				currentQuote.getOpenPrice().doubleValue());
	}

}
