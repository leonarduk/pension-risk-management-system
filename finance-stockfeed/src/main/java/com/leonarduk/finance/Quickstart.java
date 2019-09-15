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
package com.leonarduk.finance;

import static com.leonarduk.finance.stockfeed.file.IndicatorsToCsv.exportIndicatorsToCsv;

import java.io.IOException;

import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.StopGainRule;
import org.ta4j.core.trading.rules.StopLossRule;

import com.leonarduk.finance.analysis.TraderOrderUtils;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.TimeseriesUtils;

/**
 * Quickstart for ta4j.
 * <p>
 * Global example.
 */
public class Quickstart {

	public static void main(final String[] args) throws IOException {

		// Getting a time series (from any provider: CSV, web service, etc.)
		final StockFeed feed = new IntelligentStockFeed();
		final String ticker = "ISJP";
		final StockV1 stock = feed.get(Instrument.fromString(ticker), 2).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);

		// Getting the close price of the ticks
		final Num firstClosePrice = series.getBar(0).getClosePrice();
		System.out.println("First close price: " + firstClosePrice.doubleValue());
		// Or within an indicator:
		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Here is the same close price:
		System.out.println(firstClosePrice.isEqual(closePrice.getValue(0))); // equal
		                                                                     // to
		                                                                     // firstClosePrice

		// Getting the simple moving average (SMA) of the close price over the
		// last 5 ticks
		final SMAIndicator shortSma = new SMAIndicator(closePrice, 5);
		// Here is the 5-ticks-SMA value at the 42nd index
		System.out.println("5-ticks-SMA value at the 42nd index: "
		        + shortSma.getValue(42).doubleValue());

		// Getting a longer SMA (e.g. over the 30 last ticks)
		final SMAIndicator longSma = new SMAIndicator(closePrice, 30);

		// Ok, now let's building our trading rules!

		// Buying rules
		// We want to buy:
		// - if the 5-ticks SMA crosses over 30-ticks SMA
		// - or if the price goes below a defined price (e.g $800.00)
		final Rule buyingRule = new CrossedUpIndicatorRule(shortSma, longSma);
		// .or(new CrossedDownIndicatorRule(closePrice,
		// Double.valueOf("800")));

		// Selling rules
		// We want to sell:
		// - if the 5-ticks SMA crosses under 30-ticks SMA
		// - or if if the price looses more than 3%
		// - or if the price earns more than 2%
		final Rule sellingRule = new CrossedDownIndicatorRule(shortSma, longSma)
		        .or(new StopLossRule(closePrice, Double.valueOf("3")))
		        .or(new StopGainRule(closePrice, Double.valueOf("2")));

		// Running our juicy trading strategy...
		final Strategy strategy =  new BaseStrategy(buyingRule, sellingRule);
		final String strategyName = "30-tick-SMA";
		TimeSeriesManager manager = new TimeSeriesManager(series);

		final TradingRecord tradingRecord = manager.run(strategy);
		System.out.println("Number of trades for our strategy: "
		        + tradingRecord.getTradeCount());

		// Analysis

		// Getting the cash flow of the resulting trades
		// final CashFlow cashFlow = new CashFlow(series, tradingRecord);

		// Getting the profitable trades ratio
		final AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
		System.out.println("Profitable trades ratio: "
		        + profitTradesRatio.calculate(series, tradingRecord));
		// Getting the reward-risk ratio
		final AnalysisCriterion rewardRiskRatio = new RewardRiskRatioCriterion();
		System.out.println("Reward-risk ratio: "
		        + rewardRiskRatio.calculate(series, tradingRecord));

		// Total profit of our strategy
		// vs total profit of a buy-and-hold strategy
		final AnalysisCriterion vsBuyAndHold = new VersusBuyAndHoldCriterion(
		        new TotalProfitCriterion());
		System.out.println("Our profit vs buy-and-hold profit: "
		        + vsBuyAndHold.calculate(series, tradingRecord));
//		CandlestickChart.displayCandlestickChart(stock);
//		BollingerBars.displayBollingerBars(stock);
		exportIndicatorsToCsv(series);

		System.out.println(TraderOrderUtils.getOrdersList(
		        tradingRecord.getTrades(), series, strategy, strategyName));

		// Your turn!
	}
}
