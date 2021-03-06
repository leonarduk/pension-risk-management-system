///**
// * The MIT License (MIT)
// *
// * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in
// * all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//package com.leonarduk.finance.analysis;
//
//import java.io.IOException;
//
//import org.ta4j.core.TimeSeries;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.analysis.criteria.AverageProfitCriterion;
//import org.ta4j.core.analysis.criteria.AverageProfitableTradesCriterion;
//import org.ta4j.core.analysis.criteria.BuyAndHoldCriterion;
//import org.ta4j.core.analysis.criteria.LinearTransactionCostCriterion;
//import org.ta4j.core.analysis.criteria.MaximumDrawdownCriterion;
//import org.ta4j.core.analysis.criteria.NumberOfTradesCriterion;
//import org.ta4j.core.analysis.criteria.RewardRiskRatioCriterion;
//import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
//import org.ta4j.core.analysis.criteria.VersusBuyAndHoldCriterion;
//
//import com.leonarduk.finance.stockfeed.Instrument;
//import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
//import com.leonarduk.finance.stockfeed.Stock;
//import com.leonarduk.finance.stockfeed.StockFeed;
//import com.leonarduk.finance.strategies.AbstractStrategy;
//import com.leonarduk.finance.strategies.MovingMomentumStrategy;
//import com.leonarduk.finance.utils.TimeseriesUtils;
//
//import eu.verdelhan.ta4j.analysis.criteria.NumberOfTicksCriterion;
//
///**
// * This class diplays analysis criterion values after running a trading strategy
// * over a time series.
// */
//public class StrategyAnalysis {
//
//	public static void main(final String[] args) throws IOException {
//
//		// Getting the time series
//		final StockFeed feed = new IntelligentStockFeed();
//		final String ticker = "XMJG";
//		final Stock stock = feed.get(Instrument.fromString(ticker), 20).get();
//		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);
//
//		// Building the trading strategy
//		final AbstractStrategy strategy = MovingMomentumStrategy
//		        .buildStrategy(series, 12, 26, 9);
//		// Running the strategy
//		final TradingRecord tradingRecord = series.run(strategy.getStrategy());
//
//		/**
//		 * Analysis criteria
//		 */
//
//		// Total profit
//		final TotalProfitCriterion totalProfit = new TotalProfitCriterion();
//		System.out.println("Total profit: "
//		        + totalProfit.calculate(series, tradingRecord));
//		// Number of ticks
//		System.out.println("Number of ticks: " + new NumberOfTicksCriterion()
//		        .calculate(series, tradingRecord));
//		// Average profit (per tick)
//		System.out.println(
//		        "Average profit (per tick): " + new AverageProfitCriterion()
//		                .calculate(series, tradingRecord));
//		// Number of trades
//		System.out.println("Number of trades: " + new NumberOfTradesCriterion()
//		        .calculate(series, tradingRecord));
//		// Profitable trades ratio
//		System.out.println("Profitable trades ratio: "
//		        + new AverageProfitableTradesCriterion().calculate(series,
//		                tradingRecord));
//		// Maximum drawdown
//		System.out.println("Maximum drawdown: " + new MaximumDrawdownCriterion()
//		        .calculate(series, tradingRecord));
//		// Reward-risk ratio
//		System.out
//		        .println("Reward-risk ratio: " + new RewardRiskRatioCriterion()
//		                .calculate(series, tradingRecord));
//		// Total transaction cost
//		System.out.println("Total transaction cost (from $1000): "
//		        + new LinearTransactionCostCriterion(1000, 0.005)
//		                .calculate(series, tradingRecord));
//		// Buy-and-hold
//		System.out.println("Buy-and-hold: "
//		        + new BuyAndHoldCriterion().calculate(series, tradingRecord));
//		// Total profit vs buy-and-hold
//		System.out.println(
//		        "Custom strategy profit vs buy-and-hold strategy profit: "
//		                + new VersusBuyAndHoldCriterion(totalProfit)
//		                        .calculate(series, tradingRecord));
//	}
//}
