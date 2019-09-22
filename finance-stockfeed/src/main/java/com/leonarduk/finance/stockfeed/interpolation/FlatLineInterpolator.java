package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.ta4j.core.Bar;

import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.TimeseriesUtils;

public class FlatLineInterpolator extends AbstractLineInterpolator {

	@Override
	protected Bar calculateFutureValue(final Bar lastQuote, final LocalDate today) {
		return new ExtendedHistoricalQuote(lastQuote, today, "Copied from " + lastQuote.getEndTime().toLocalDate());
	}

	@Override
	protected Bar calculatePastValue(final Bar currentQuote, final LocalDate fromDate) throws IOException {
		return TimeseriesUtils.createSyntheticQuote(currentQuote, fromDate,
				BigDecimal.valueOf(currentQuote.getClosePrice().doubleValue()),
				BigDecimal.valueOf(currentQuote.getOpenPrice().doubleValue()),
				"Copied from " + currentQuote.getEndTime().toLocalDate());
	}

	@Override
	public Bar createSyntheticQuote(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote)
			throws IOException {
		return TimeseriesUtils.createSyntheticQuote(currentQuote, currentDate,
				BigDecimal.valueOf(currentQuote.getClosePrice().doubleValue()),
				BigDecimal.valueOf(currentQuote.getOpenPrice().doubleValue()),
				"Copied from " + currentQuote.getEndTime().toLocalDate());
	}

	@Override
	public Bar createSyntheticBar(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote) {
		return TimeseriesUtils.createSyntheticBar(currentDate, currentQuote.getClosePrice().doubleValue(),
				currentQuote.getOpenPrice().doubleValue(), "Copied from " + currentQuote.getEndTime().toLocalDate());

	}

}
