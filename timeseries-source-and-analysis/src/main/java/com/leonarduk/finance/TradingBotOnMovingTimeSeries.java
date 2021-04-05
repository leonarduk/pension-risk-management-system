//package com.leonarduk.finance;
//
//import java.io.IOException;
//
//import org.ta4j.core.BaseStrategy;
//import org.ta4j.core.BaseTradingRecord;
//import org.ta4j.core.Order;
//import org.ta4j.core.Strategy;
//import org.ta4j.core.TimeSeries;
//import org.ta4j.core.TradingRecord;
//import org.ta4j.core.indicators.SMAIndicator;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.trading.rules.OverIndicatorRule;
//import org.ta4j.core.trading.rules.UnderIndicatorRule;
//
//import com.leonarduk.finance.stockfeed.Instrument;
//import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
//import com.leonarduk.finance.stockfeed.Stock;
//import com.leonarduk.finance.utils.TimeseriesUtils;
//
///**
// * This class is an example of a dummy trading bot using ta4j.
// */
//public class TradingBotOnMovingTimeSeries {
//
//	/** Close price of the last tick */
//	private static double LAST_TICK_CLOSE_PRICE;
//
//	/**
//	 * @param series
//	 *            a time series
//	 * @return a dummy strategy
//	 */
//	private static Strategy buildStrategy(final TimeSeries series) {
//		if (series == null) {
//			throw new IllegalArgumentException("Series cannot be null");
//		}
//
//		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//		final SMAIndicator sma = new SMAIndicator(closePrice, 12);
//
//		// Signals
//		// Buy when SMA goes over close price
//		// Sell when close price goes over SMA
//		final Strategy buySellSignals = new BaseStrategy(
//		        new OverIndicatorRule(sma, closePrice),
//		        new UnderIndicatorRule(sma, closePrice));
//		return buySellSignals;
//	}
//
//	/**
//	 * Generates a random tick.
//	 *
//	 * @return a random tick
//	 */
//	private static Tick generateRandomTick() {
//		final Double maxRange = Double.valueOf("0.03"); // 3.0%
//		final Double openPrice = TradingBotOnMovingTimeSeries.LAST_TICK_CLOSE_PRICE;
//		final Double minPrice = openPrice.minus(openPrice.multipliedBy(
//		        maxRange.multipliedBy(Double.valueOf(Math.random()))));
//		final Double maxPrice = openPrice.plus(openPrice.multipliedBy(
//		        maxRange.multipliedBy(Double.valueOf(Math.random()))));
//		final Double closePrice = TradingBotOnMovingTimeSeries
//		        .randDecimal(minPrice, maxPrice);
//		TradingBotOnMovingTimeSeries.LAST_TICK_CLOSE_PRICE = closePrice;
//		return new Tick(DateTime.now(), openPrice, maxPrice, minPrice,
//		        closePrice, Double.ONE);
//	}
//
//	/**
//	 * Builds a moving time series (i.e. keeping only the maxTickCount last
//	 * ticks)
//	 *
//	 * @param maxTickCount
//	 *            the number of ticks to keep in the time series (at maximum)
//	 * @return a moving time series
//	 * @throws IOException
//	 */
//	private static TimeSeries initMovingTimeSeries(final int maxTickCount)
//	        throws IOException {
//		final String ticker = "PHGP";
//		final Stock stock = new IntelligentStockFeed()
//		        .get(Instrument.fromString(ticker), 1).get();
//		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);
//		System.out.print("Initial tick count: " + series.getTickCount());
//		// Limitating the number of ticks to maxTickCount
//		series.setMaximumTickCount(maxTickCount);
//		TradingBotOnMovingTimeSeries.LAST_TICK_CLOSE_PRICE = series
//		        .getTick(series.getEndIndex()).getClosePrice();
//		System.out.println(" (limited to " + maxTickCount + "), close price = "
//		        + TradingBotOnMovingTimeSeries.LAST_TICK_CLOSE_PRICE);
//		return series;
//	}
//
//	public static void main(final String[] args)
//	        throws InterruptedException, IOException {
//
//		System.out.println(
//		        "********************** Initialization **********************");
//		// Getting the time series
//		final TimeSeries series = TradingBotOnMovingTimeSeries
//		        .initMovingTimeSeries(20);
//
//		// Building the trading strategy
//		final Strategy strategy = TradingBotOnMovingTimeSeries
//		        .buildStrategy(series);
//
//		// Initializing the trading history
//		final TradingRecord tradingRecord = new BaseTradingRecord();
//		System.out.println(
//		        "************************************************************");
//
//		/**
//		 * We run the strategy for the 50 next ticks.
//		 */
//		for (int i = 0; i < 50; i++) {
//
//			// New tick
//			Thread.sleep(30); // I know...
//			final Tick newTick = TradingBotOnMovingTimeSeries
//			        .generateRandomTick();
//			System.out.println(
//			        "------------------------------------------------------\n"
//			                + "Tick " + i + " added, close price = "
//			                + newTick.getClosePrice().toDouble());
//			series.addTick(newTick);
//
//			final int endIndex = series.getEndIndex();
//			final SnapshotAnalyser snapshotAnalyser = new SnapshotAnalyser();
//			if (strategy.shouldEnter(endIndex)) {
//				// Our strategy should enter
//				System.out.println("Strategy should ENTER on " + endIndex);
//				final boolean entered = tradingRecord.enter(endIndex,
//				        newTick.getClosePrice(), Double.TEN);
//				if (entered) {
//					final Order entry = tradingRecord.getLastEntry();
//					snapshotAnalyser.showTradeAction(entry, "Enter");
//				}
//			}
//			else if (strategy.shouldExit(endIndex)) {
//				// Our strategy should exit
//				System.out.println("Strategy should EXIT on " + endIndex);
//				final boolean exited = tradingRecord.exit(endIndex,
//				        newTick.getClosePrice(), Double.valueOf(10.0));
//				if (exited) {
//					final Order exit = tradingRecord.getLastExit();
//					snapshotAnalyser.showTradeAction(exit, "Exit");
//				}
//			}
//		}
//	}
//
//	/**
//	 * Generates a random Double number between min and max.
//	 *
//	 * @param min
//	 *            the minimum bound
//	 * @param max
//	 *            the maximum bound
//	 * @return a random Double number between min and max
//	 */
//	private static Double randDecimal(final Double min, final Double max) {
//		Double randomDecimal = null;
//		if ((min != null) && (max != null) && Double.compare(min, max) < 0) {
//			randomDecimal = max.minus(min)
//			        .multipliedBy(Double.valueOf(Math.random())).plus(min);
//		}
//		return randomDecimal;
//	}
//}
