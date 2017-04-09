/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.leonarduk.stockmarketview.strategies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.joda.time.Period;

import com.leonarduk.stockmarketview.Demo;
import com.leonarduk.stockmarketview.chart.TraderOrderUtils;
import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.stockfeed.SymbolFileReader;
import com.leonarduk.stockmarketview.stockfeed.yahoo.YahooFeed;

import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import yahoofinance.Stock;

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
	public static Map<Strategy, String> buildStrategiesMap(TimeSeries series) {
		HashMap<Strategy, String> strategies = new HashMap<Strategy, String>();
		strategies.put(CCICorrectionStrategy.buildStrategy(series), "CCI Correction");
		strategies.put(GlobalExtremaStrategy.buildStrategy(series), "Global Extrema");
		strategies.put(MovingMomentumStrategy.buildStrategy(series), "Moving Momentum");
		strategies.put(RSI2Strategy.buildStrategy(series), "RSI-2");
		return strategies;
	}

	public static void main(String[] args) throws IOException {
		Map<String, AtomicInteger> totalscores = new ConcurrentHashMap<>();
		StockFeed feed = new YahooFeed();

		String filePath = new File(Demo.class.getClassLoader().getResource("Book1.csv").getFile()).getAbsolutePath();

		SymbolFileReader.getStocksFromCSVFile(filePath).parallelStream().forEach(stock -> {
			try {
				computeForStrategies(totalscores, feed, stock.getSymbol());
			} catch (Exception e) {
				System.err.println("Failed to compute " + stock.getSymbol());
			}
		});

		System.out.println(totalscores);
	}

	private static void computeForStrategies(Map<String, AtomicInteger> totalscores, StockFeed feed, String ticker)
			throws IOException {
		Stock stock = feed.get(EXCHANGE.London, ticker).get();
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);
		List<TimeSeries> subseries = series.split(Period.days(1), Period.weeks(4));

		// Building the map of strategies
		Map<Strategy, String> strategies = buildStrategiesMap(series);

		// The analysis criterion
		AnalysisCriterion profitCriterion = new TotalProfitCriterion();
		Map<String, AtomicInteger> scores = new ConcurrentHashMap<>();

		for (TimeSeries slice : subseries) {
			// For each sub-series...
			calculateSubseries(strategies, profitCriterion, slice, scores);
		}
		totalscores.putAll(scores);
		System.out.println(ticker + scores);
	}

	private static void calculateSubseries(Map<Strategy, String> strategies, AnalysisCriterion profitCriterion,
			TimeSeries slice, Map<String, AtomicInteger> scores) {
		boolean interesting = false;
		StringBuilder buf = new StringBuilder("Sub-series: " + slice.getSeriesPeriodDescription() + "\n");
		for (Map.Entry<Strategy, String> entry : strategies.entrySet()) {
			Strategy strategy = entry.getKey();
			String name = entry.getValue();
			// For each strategy...
			TradingRecord tradingRecord = slice.run(strategy);
			double profit = profitCriterion.calculate(slice, tradingRecord);
			if (profit != 1.0) {
				interesting = true;
				if (profit > 1.0) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).incrementAndGet();
					System.out
							.println(TraderOrderUtils.getOrdersList(tradingRecord.getTrades(), slice, strategy, name));
				}
				if (profit < 1.0) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).decrementAndGet();
				}
			}
			buf.append("\tProfit for " + name + ": " + profit + "\n");
		}

		Strategy bestStrategy = profitCriterion.chooseBest(slice, new ArrayList<Strategy>(strategies.keySet()));
		buf.append("\t\t--> Best strategy: " + strategies.get(bestStrategy) + "\n");
		if (interesting) {
			// System.out.println(buf.toString());
		}
	}
}
