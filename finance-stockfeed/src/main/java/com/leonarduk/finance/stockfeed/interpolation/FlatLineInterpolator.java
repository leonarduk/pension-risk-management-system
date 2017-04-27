package com.leonarduk.finance.stockfeed.interpolation;

import org.joda.time.LocalDate;

import eu.verdelhan.ta4j.Tick;

public class FlatLineInterpolator extends AbstractLineInterpolator {

	@Override
	public Tick createSyntheticTick(final Tick currentQuote, final LocalDate currentDate, final Tick nextQuote) {
		return this.createSyntheticTick(currentDate, currentQuote.getClosePrice(), currentQuote.getOpenPrice());
	}

}
