package com.leonarduk.finance.stockfeed.interpolation;

import org.joda.time.LocalDate;

import com.leonarduk.finance.utils.DateUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Tick;

public class LinearInterpolator extends AbstractLineInterpolator {

	@Override
	public Tick createSyntheticTick(final Tick currentQuote, final LocalDate currentDate, final Tick nextQuote) {

		final double timeInteval = DateUtils.getDiffInWorkDays(nextQuote.getEndTime().toLocalDate(),
				currentQuote.getEndTime().toLocalDate());
		final int dayCount = DateUtils.getDiffInWorkDays(currentQuote.getEndTime().toLocalDate(), currentDate);
		final double multiplier = dayCount / timeInteval;

		final Decimal changeClosePrice = nextQuote.getClosePrice().minus(currentQuote.getClosePrice());
		final Decimal changeOpenPrice = nextQuote.getOpenPrice().minus(currentQuote.getOpenPrice());

		final Decimal newClosePrice = currentQuote.getClosePrice()
				.plus(changeClosePrice.multipliedBy(Decimal.valueOf(multiplier)));
		final Decimal newOpenPrice = currentQuote.getOpenPrice()
				.plus(changeOpenPrice.multipliedBy(Decimal.valueOf(multiplier)));

		return this.createSyntheticTick(currentDate, newClosePrice, newOpenPrice);
	}

}
