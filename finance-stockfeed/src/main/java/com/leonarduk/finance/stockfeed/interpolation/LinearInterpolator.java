package com.leonarduk.finance.stockfeed.interpolation;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import com.leonarduk.finance.utils.DateUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;
import yahoofinance.histquotes.HistoricalQuote;

public class LinearInterpolator extends AbstractLineInterpolator {

	@Override
	protected HistoricalQuote calculateFutureValue(
	        final HistoricalQuote lastQuote, final LocalDate today) {
		// TODO maybe use a gradient from a few points before
		throw new UnsupportedOperationException();
	}

	@Override
	protected HistoricalQuote calculatePastValue(
	        final HistoricalQuote firstQuote, final LocalDate fromDate) {
		// TODO maybe use a gradient from a few points before
		throw new UnsupportedOperationException();
	}

	@Override
	public HistoricalQuote createSyntheticQuote(
	        final HistoricalQuote currentQuote, final LocalDate currentDate,
	        final HistoricalQuote nextQuote) {
		final double timeInteval = DateUtils
		        .getDiffInWorkDays(nextQuote.getDate(), currentQuote.getDate());
		final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getDate(),
		        currentDate);
		final double multiplier = dayCount / timeInteval;

		final BigDecimal changeClosePrice = nextQuote.getClose()
		        .subtract(currentQuote.getClose());
		final BigDecimal changeOpenPrice = nextQuote.getOpen()
		        .subtract(currentQuote.getOpen());

		final BigDecimal newClosePrice = currentQuote.getClose()
		        .add(changeClosePrice.multiply(BigDecimal.valueOf(multiplier)));
		final BigDecimal newOpenPrice = currentQuote.getOpen()
		        .add(changeOpenPrice.multiply(BigDecimal.valueOf(multiplier)));

		return this.createSyntheticQuote(currentQuote, currentDate,
		        newClosePrice, newOpenPrice,
		        "Interpolated from " + currentQuote.getDate() + "("
		                + currentQuote.getClose() + ") to "
		                + nextQuote.getDate() + " (" + nextQuote.getClose()
		                + ")");
	}

	@Override
	public Tick createSyntheticTick(final Tick currentQuote,
	        final LocalDate currentDate, final Tick nextQuote) {

		final double timeInteval = DateUtils.getDiffInWorkDays(
		        nextQuote.getEndTime().toLocalDate(),
		        currentQuote.getEndTime().toLocalDate());
		final int dayCount = DateUtils.getDiffInWorkDays(
		        currentQuote.getEndTime().toLocalDate(), currentDate);
		final double multiplier = dayCount / timeInteval;

		final Decimal changeClosePrice = nextQuote.getClosePrice()
		        .minus(currentQuote.getClosePrice());
		final Decimal changeOpenPrice = nextQuote.getOpenPrice()
		        .minus(currentQuote.getOpenPrice());

		final Decimal newClosePrice = currentQuote.getClosePrice().plus(
		        changeClosePrice.multipliedBy(Decimal.valueOf(multiplier)));
		final Decimal newOpenPrice = currentQuote.getOpenPrice().plus(
		        changeOpenPrice.multipliedBy(Decimal.valueOf(multiplier)));

		return this.createSyntheticTick(currentDate, newClosePrice,
		        newOpenPrice);
	}

}
