package com.leonarduk.stockmarketview.strategies;

import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

public class SimpleMovingAverageStrategy extends AbstractStrategy {

	private SimpleMovingAverageStrategy(String name, Strategy strategy) {
		super(name, strategy);
	}

	public static SimpleMovingAverageStrategy buildStrategy(TimeSeries series, int days) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		SMAIndicator sma = new SMAIndicator(closePrice, days);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		Strategy buySellSignals = new Strategy(new OverIndicatorRule(sma, closePrice),
				new UnderIndicatorRule(sma, closePrice));
		return new SimpleMovingAverageStrategy("SMA (" + days + "days)", buySellSignals);

	}

}
