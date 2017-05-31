/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.leonarduk.finance.strategies;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.helpers.HighestValueIndicator;
import eu.verdelhan.ta4j.indicators.helpers.LowestValueIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MaxPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MinPriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.MultiplierIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

/**
 * Strategies which compares current price to global extrema over a week.
 */
public class GlobalExtremaStrategy extends AbstractStrategy {

	// We assume that there were at least one trade every 5 minutes during the
	// whole week
	private static final int NB_TICKS_PER_WEEK = 12 * 24 * 7;

	/**
	 * @param series
	 *            a time series
	 * @return a global extrema strategy
	 */
	public static GlobalExtremaStrategy buildStrategy(final TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrices = new ClosePriceIndicator(series);

		// Getting the max price over the past week
		final MaxPriceIndicator maxPrices = new MaxPriceIndicator(series);
		final HighestValueIndicator weekMaxPrice = new HighestValueIndicator(
		        maxPrices, GlobalExtremaStrategy.NB_TICKS_PER_WEEK);
		// Getting the min price over the past week
		final MinPriceIndicator minPrices = new MinPriceIndicator(series);
		final LowestValueIndicator weekMinPrice = new LowestValueIndicator(
		        minPrices, GlobalExtremaStrategy.NB_TICKS_PER_WEEK);

		// Going long if the close price goes below the min price
		final MultiplierIndicator downWeek = new MultiplierIndicator(
		        weekMinPrice, Decimal.valueOf("1.004"));
		final Rule buyingRule = new UnderIndicatorRule(closePrices, downWeek);

		// Going short if the close price goes above the max price
		final MultiplierIndicator upWeek = new MultiplierIndicator(weekMaxPrice,
		        Decimal.valueOf("0.996"));
		final Rule sellingRule = new OverIndicatorRule(closePrices, upWeek);

		return new GlobalExtremaStrategy(new Strategy(buyingRule, sellingRule));
	}

	public GlobalExtremaStrategy(final Strategy strategy) {
		super("Global Extrema", strategy);
	}
}
