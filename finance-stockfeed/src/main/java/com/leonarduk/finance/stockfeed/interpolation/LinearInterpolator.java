package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.ta4j.core.Bar;

import com.leonarduk.finance.stockfeed.yahoo.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.DateUtils;

public class LinearInterpolator extends AbstractLineInterpolator {

	@Override
	protected ExtendedHistoricalQuote calculateFutureValue(final ExtendedHistoricalQuote lastQuote,
			final LocalDate today) {
		// TODO maybe use a gradient from a few points before
		throw new UnsupportedOperationException();
	}

	@Override
	protected ExtendedHistoricalQuote calculatePastValue(final ExtendedHistoricalQuote firstQuote,
			final LocalDate fromDate) {
		// TODO maybe use a gradient from a few points before
		throw new UnsupportedOperationException();
	}

	@Override
	public ExtendedHistoricalQuote createSyntheticQuote(final ExtendedHistoricalQuote currentQuote,
			final LocalDate currentDate, final ExtendedHistoricalQuote nextQuote) throws IOException {
		final double timeInteval = DateUtils.getDiffInWorkDays(nextQuote.getLocaldate(), currentQuote.getLocaldate());
		final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getLocaldate(), currentDate);
		final double multiplier = dayCount / timeInteval;

		final BigDecimal changeClosePrice = nextQuote.getClose().subtract(currentQuote.getClose());
		final BigDecimal changeOpenPrice = nextQuote.getOpen().subtract(currentQuote.getOpen());

		final BigDecimal newClosePrice = currentQuote.getClose()
				.add(changeClosePrice.multiply(BigDecimal.valueOf(multiplier)));
		final BigDecimal newOpenPrice = currentQuote.getOpen()
				.add(changeOpenPrice.multiply(BigDecimal.valueOf(multiplier)));

		return this.createSyntheticQuote(currentQuote, currentDate, newClosePrice, newOpenPrice,
				"Interpolated from " + currentQuote.getDate() + "(" + currentQuote.getClose() + ") to "
						+ nextQuote.getDate() + " (" + nextQuote.getClose() + ")");
	}

	@Override
	public Bar createSyntheticBar(final Bar currentQuote, final LocalDate currentDate, final Bar nextQuote) {

		final double timeInteval = DateUtils.getDiffInWorkDays(nextQuote.getEndTime().toLocalDate(),
				currentQuote.getEndTime().toLocalDate());
		final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().toLocalDate(), currentDate);
		final double multiplier = dayCount / timeInteval;

		final Double changeClosePrice = nextQuote.getClosePrice().doubleValue()
				- currentQuote.getClosePrice().doubleValue();
		final Double changeOpenPrice = nextQuote.getOpenPrice().doubleValue()
				- currentQuote.getOpenPrice().doubleValue();

		final Double newClosePrice = currentQuote.getClosePrice().doubleValue()
				+ (changeClosePrice * Double.valueOf(multiplier));
		final Double newOpenPrice = currentQuote.getOpenPrice().doubleValue()
				+ (changeOpenPrice * Double.valueOf(multiplier));

		return this.createSyntheticBar(currentDate, newClosePrice, newOpenPrice);
	}

}
