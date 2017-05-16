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
package com.leonarduk.finance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.Period;

import com.leonarduk.finance.analysis.TraderOrderUtils;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;

/**
 * Walk-forward optimization example.
 * <p>
 *
 * @see http://en.wikipedia.org/wiki/Walk_forward_optimization
 * @see http://www.futuresmag.com/2010/04/01/can-your-system-do-the-walk
 */
public class WalkForward {

	/**
	 * @param series
	 *            the time series
	 * @return a map (key: strategy, value: name) of trading strategies
	 */
	public static List<AbstractStrategy> buildStrategiesMap(final TimeSeries series) {
		// {Moving Momentum=24916, RSI-2=-81064, Global Extrema=23748, CCI
		// Correction=-28035}
		final List<AbstractStrategy> strategies = new ArrayList<>();
		strategies.add(GlobalExtremaStrategy.buildStrategy(series));
		strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));
		return strategies;
	}

	private static void calculateSubseries(final List<AbstractStrategy> strategies,
	        final AnalysisCriterion profitCriterion, final TimeSeries slice,
	        final Map<String, AtomicInteger> scores) {
		boolean interesting = false;
		final StringBuilder buf = new StringBuilder(
		        "Sub-series: " + slice.getSeriesPeriodDescription() + "\n");
		for (final AbstractStrategy entry : strategies) {
			final Strategy strategy = entry.getStrategy();
			final String name = entry.getName();
			// For each strategy...
			final TradingRecord tradingRecord = slice.run(strategy);
			final double profit = profitCriterion.calculate(slice, tradingRecord);
			if (profit != 1.0) {
				interesting = true;
				if (profit > 1.0) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).incrementAndGet();
					System.out.println(TraderOrderUtils.getOrdersList(tradingRecord.getTrades(),
					        slice, strategy, name));
				}
				if (profit < 1.0) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).decrementAndGet();
				}
			}
			buf.append("\tProfit for " + name + ": " + profit + "\n");
		}

		// ArrayList<Strategy> strategies2 = new ArrayList<Strategy>();
		//
		// Strategy bestStrategy = profitCriterion.chooseBest(slice,
		// strategies2);
		// buf.append("\t\t--> Best strategy: " + strategies.get(bestStrategy) +
		// "\n");
		if (interesting) {
			// System.out.println(buf.toString());
		}
	}

	private static void computeForStrategies(final Map<String, AtomicInteger> totalscores,
	        final StockFeed feed, final String ticker) throws IOException {
		final Stock stock = feed.get(Instrument.fromString(ticker), 2).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);
		final List<TimeSeries> subseries = series.split(Period.days(1), Period.weeks(4));

		// Building the map of strategies
		final List<AbstractStrategy> strategies = WalkForward.buildStrategiesMap(series);

		// The analysis criterion
		final AnalysisCriterion profitCriterion = new TotalProfitCriterion();
		final Map<String, AtomicInteger> scores = new ConcurrentHashMap<>();

		for (final TimeSeries slice : subseries) {
			// For each sub-series...
			WalkForward.calculateSubseries(strategies, profitCriterion, slice, scores);
		}

		for (final Entry<String, AtomicInteger> timeSeries : scores.entrySet()) {
			totalscores.putIfAbsent(timeSeries.getKey(), new AtomicInteger());
			totalscores.get(timeSeries.getKey()).addAndGet(timeSeries.getValue().get());
		}
		System.out.println(ticker + scores);
	}

	public static void main(final String[] args) throws IOException {
		final Map<String, AtomicInteger> totalscores = new ConcurrentHashMap<>();
		final StockFeed feed = new IntelligentStockFeed();

		final String filePath = new File(
		        Demo.class.getClassLoader().getResource("Book1.csv").getFile()).getAbsolutePath();

		InvestmentsFileReader.getStocksFromCSVFile(filePath).parallelStream().forEach(stock -> {
			try {
				WalkForward.computeForStrategies(totalscores, feed, stock.getSymbol());
			}
			catch (final Exception e) {
				System.err.println("Failed to compute " + stock.getSymbol());
			}
		});

		System.out.println(totalscores);
	}
}
