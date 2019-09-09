/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.leonarduk.finance.strategies;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

/**
 * Moving momentum strategy.
 *
 * http://stockcharts.com/help/doku.php?id=chart_school:trading_strategies:moving_momentum
 */
public class MovingMomentumStrategy extends AbstractStrategy {

	public static AbstractStrategy buildStrategy(final TimeSeries series,
	        final int shortEmaPeriod, final int longEmaPeriod,
	        final int emaMacdPeriod) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

		// The bias is bullish when the shorter-moving average moves above the
		// longer moving average.
		// The bias is bearish when the shorter-moving average moves below the
		// longer moving average.
		final EMAIndicator shortEma = new EMAIndicator(closePrice,
		        shortEmaPeriod);
		final EMAIndicator longEma = new EMAIndicator(closePrice,
		        longEmaPeriod);

		final StochasticOscillatorKIndicator stochasticOscillK = new StochasticOscillatorKIndicator(
		        series, 14);

		final MACDIndicator macd = new MACDIndicator(closePrice, shortEmaPeriod,
		        longEmaPeriod);
		final EMAIndicator emaMacd = new EMAIndicator(macd, emaMacdPeriod);

		// Entry rule
		final Rule entryRule = new OverIndicatorRule(shortEma, longEma) // Trend
		        .and(new CrossedDownIndicatorRule(stochasticOscillK,
		                Double.valueOf(20))) // Signal
		                                      // 1
		        .and(new OverIndicatorRule(macd, emaMacd)); // Signal 2

		// Exit rule
		final Rule exitRule = new UnderIndicatorRule(shortEma, longEma) // Trend
		        .and(new CrossedUpIndicatorRule(stochasticOscillK,
		                Double.valueOf(80))) // Signal
		                                      // 1
		        .and(new UnderIndicatorRule(macd, emaMacd)); // Signal 2

		return new MovingMomentumStrategy("Moving Momentum",
		        new BaseStrategy(entryRule, exitRule));
	}

	private MovingMomentumStrategy(final String name, final Strategy strategy) {
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
