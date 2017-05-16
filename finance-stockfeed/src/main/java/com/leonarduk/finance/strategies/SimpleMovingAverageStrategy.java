package com.leonarduk.finance.strategies;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class SimpleMovingAverageStrategy extends AbstractStrategy {

	public static SimpleMovingAverageStrategy buildStrategy(final TimeSeries series,
	        final int days) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		final SMAIndicator sma = new SMAIndicator(closePrice, days);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		final Strategy buySellSignals = new Strategy(new OverIndicatorRule(sma, closePrice),
		        new UnderIndicatorRule(sma, closePrice));
		return new SimpleMovingAverageStrategy("SMA (" + days + "days)", buySellSignals);

	}

	private SimpleMovingAverageStrategy(final String name, final Strategy strategy) {
		super(name, strategy);
	}

}
