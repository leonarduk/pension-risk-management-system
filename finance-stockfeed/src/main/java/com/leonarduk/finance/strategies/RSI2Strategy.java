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

import java.io.IOException;

import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import com.leonarduk.finance.analysis.TraderOrderUtils;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.TimeseriesUtils;


/**
 * 2-Period RSI Strategy
 *
 * http://stockcharts.com/school/doku.php?id=chart_school:trading_strategies:rsi2
 */
public class RSI2Strategy {

	public static Strategy buildStrategy(final TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		final SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		final SMAIndicator longSma = new SMAIndicator(closePrice, 200);

		// We use a 2-period RSI indicator to identify buying
		// or selling opportunities within the bigger trend.
		final RSIIndicator rsi = new RSIIndicator(closePrice, 2);

		// Entry rule
		// The long-term trend is up when a security is above its 200-period
		// SMA.
		final Rule entryRule = new OverIndicatorRule(shortSma, longSma) // Trend
		        .and(new CrossedDownIndicatorRule(rsi, Double.valueOf(5))) // Signal
		                                                                    // 1
		        .and(new OverIndicatorRule(shortSma, closePrice)); // Signal 2

		// Exit rule
		// The long-term trend is down when a security is below its 200-period
		// SMA.
		final Rule exitRule = new UnderIndicatorRule(shortSma, longSma) // Trend
		        .and(new CrossedUpIndicatorRule(rsi, Double.valueOf(95))) // Signal
		                                                                   // 1
		        .and(new UnderIndicatorRule(shortSma, closePrice)); // Signal 2

		// TODO: Finalize the strategy

		return new BaseStrategy(entryRule, exitRule);
	}

	public static void main(final String[] args) throws IOException {

		final StockFeed feed = new IntelligentStockFeed();
		final String ticker = "PHGP";
		final StockV1 stock = feed.get(Instrument.fromString(ticker), 20).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);

		// Building the trading strategy
		final Strategy strategy = RSI2Strategy.buildStrategy(series);

		// Running the strategy
		TimeSeriesManager manager = new TimeSeriesManager(series);
		final TradingRecord tradingRecord = manager.run(strategy);
		System.out.println("Number of trades for the strategy: "
		        + tradingRecord.getTradeCount());
		System.out.println(
		        TraderOrderUtils.getOrdersList(tradingRecord.getTrades(),
		                series, strategy, RSI2Strategy.class.getName()));

		// Analysis
		System.out.println("Total profit for the strategy: "
		        + new TotalProfitCriterion().calculate(series, tradingRecord));
	}

}
