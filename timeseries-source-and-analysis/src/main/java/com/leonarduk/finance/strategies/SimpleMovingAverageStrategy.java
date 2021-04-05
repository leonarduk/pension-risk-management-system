package com.leonarduk.finance.strategies;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class SimpleMovingAverageStrategy extends AbstractStrategy {

	public static SimpleMovingAverageStrategy buildStrategy(
	        final TimeSeries series, final int days) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		final SMAIndicator sma = new SMAIndicator(closePrice, days);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		final Strategy buySellSignals = new BaseStrategy(
		        new OverIndicatorRule(sma, closePrice),
		        new UnderIndicatorRule(sma, closePrice));
		return new SimpleMovingAverageStrategy("SMA (" + days + "days)",
		        buySellSignals);

	}

	private SimpleMovingAverageStrategy(final String name,
	        final Strategy strategy) {
		super(name, strategy);
	}

	@Override
	public Rule getEntryRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Rule getExitRule() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Strategy and(Strategy strategy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Strategy or(Strategy strategy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Strategy and(String name, Strategy strategy, int unstablePeriod) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Strategy or(String name, Strategy strategy, int unstablePeriod) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Strategy opposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUnstablePeriod(int unstablePeriod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getUnstablePeriod() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUnstableAt(int index) {
		// TODO Auto-generated method stub
		return false;
	}

}
