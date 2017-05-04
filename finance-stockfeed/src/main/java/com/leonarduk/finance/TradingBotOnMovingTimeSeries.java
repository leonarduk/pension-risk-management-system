package com.leonarduk.finance;

import java.io.IOException;

import org.joda.time.DateTime;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.trading.rules.OverIndicatorRule;
import eu.verdelhan.ta4j.trading.rules.UnderIndicatorRule;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class TradingBotOnMovingTimeSeries {

	/** Close price of the last tick */
	private static Decimal LAST_TICK_CLOSE_PRICE;

	/**
	 * @param series
	 *            a time series
	 * @return a dummy strategy
	 */
	private static Strategy buildStrategy(final TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		final SMAIndicator sma = new SMAIndicator(closePrice, 12);

		// Signals
		// Buy when SMA goes over close price
		// Sell when close price goes over SMA
		final Strategy buySellSignals = new Strategy(new OverIndicatorRule(sma, closePrice),
				new UnderIndicatorRule(sma, closePrice));
		return buySellSignals;
	}

	/**
	 * Generates a random tick.
	 *
	 * @return a random tick
	 */
	private static Tick generateRandomTick() {
		final Decimal maxRange = Decimal.valueOf("0.03"); // 3.0%
		final Decimal openPrice = LAST_TICK_CLOSE_PRICE;
		final Decimal minPrice = openPrice
				.minus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
		final Decimal maxPrice = openPrice
				.plus(openPrice.multipliedBy(maxRange.multipliedBy(Decimal.valueOf(Math.random()))));
		final Decimal closePrice = randDecimal(minPrice, maxPrice);
		LAST_TICK_CLOSE_PRICE = closePrice;
		return new Tick(DateTime.now(), openPrice, maxPrice, minPrice, closePrice, Decimal.ONE);
	}

	/**
	 * Builds a moving time series (i.e. keeping only the maxTickCount last
	 * ticks)
	 *
	 * @param maxTickCount
	 *            the number of ticks to keep in the time series (at maximum)
	 * @return a moving time series
	 * @throws IOException
	 */
	private static TimeSeries initMovingTimeSeries(final int maxTickCount) throws IOException {
		final String ticker = "PHGP";
		final Stock stock = new IntelligentStockFeed().get(Instrument.fromString(ticker), 1).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock);
		System.out.print("Initial tick count: " + series.getTickCount());
		// Limitating the number of ticks to maxTickCount
		series.setMaximumTickCount(maxTickCount);
		LAST_TICK_CLOSE_PRICE = series.getTick(series.getEnd()).getClosePrice();
		System.out.println(" (limited to " + maxTickCount + "), close price = " + LAST_TICK_CLOSE_PRICE);
		return series;
	}

	public static void main(final String[] args) throws InterruptedException, IOException {

		System.out.println("********************** Initialization **********************");
		// Getting the time series
		final TimeSeries series = initMovingTimeSeries(20);

		// Building the trading strategy
		final Strategy strategy = buildStrategy(series);

		// Initializing the trading history
		final TradingRecord tradingRecord = new TradingRecord();
		System.out.println("************************************************************");

		/**
		 * We run the strategy for the 50 next ticks.
		 */
		for (int i = 0; i < 50; i++) {

			// New tick
			Thread.sleep(30); // I know...
			final Tick newTick = generateRandomTick();
			System.out.println("------------------------------------------------------\n" + "Tick " + i
					+ " added, close price = " + newTick.getClosePrice().toDouble());
			series.addTick(newTick);

			final int endIndex = series.getEnd();
			if (strategy.shouldEnter(endIndex)) {
				// Our strategy should enter
				System.out.println("Strategy should ENTER on " + endIndex);
				final boolean entered = tradingRecord.enter(endIndex, newTick.getClosePrice(), Decimal.TEN);
				if (entered) {
					final Order entry = tradingRecord.getLastEntry();
					System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
							+ ", amount=" + entry.getAmount().toDouble() + ")");
				}
			} else if (strategy.shouldExit(endIndex)) {
				// Our strategy should exit
				System.out.println("Strategy should EXIT on " + endIndex);
				final boolean exited = tradingRecord.exit(endIndex, newTick.getClosePrice(), Decimal.TEN);
				if (exited) {
					final Order exit = tradingRecord.getLastExit();
					System.out.println("Exited on " + exit.getIndex() + " (price=" + exit.getPrice().toDouble()
							+ ", amount=" + exit.getAmount().toDouble() + ")");
				}
			}
		}
	}

	/**
	 * Generates a random decimal number between min and max.
	 *
	 * @param min
	 *            the minimum bound
	 * @param max
	 *            the maximum bound
	 * @return a random decimal number between min and max
	 */
	private static Decimal randDecimal(final Decimal min, final Decimal max) {
		Decimal randomDecimal = null;
		if ((min != null) && (max != null) && min.isLessThan(max)) {
			randomDecimal = max.minus(min).multipliedBy(Decimal.valueOf(Math.random())).plus(min);
		}
		return randomDecimal;
	}
}
