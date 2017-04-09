package com.leonarduk.stockmarketview;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.SymbolFileReader;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.stockfeed.google.GoogleFeed;
import com.leonarduk.stockmarketview.strategies.AbstractStrategy;
import com.leonarduk.stockmarketview.strategies.GlobalExtremaStrategy;
import com.leonarduk.stockmarketview.strategies.MovingMomentumStrategy;
import com.leonarduk.stockmarketview.strategies.SimpleMovingAverageStrategy;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import yahoofinance.Stock;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AnalyseSnapshot {

	public static void main(String[] args) throws InterruptedException, IOException {
		StockFeed feed = new GoogleFeed();
		// String ticker = "PHGP";

		List<String> recommendations = new ArrayList<>();
		String filePath = new File(Demo.class.getClassLoader().getResource("Book1.csv").getFile()).getAbsolutePath();

		SymbolFileReader.getStocksFromCSVFile(filePath).parallelStream().forEach(stock -> {
			try {
				recommendations.addAll(analyseStock(feed, stock.getSymbol()));
			} catch (Exception e) {
				System.err.println("Failed to compute " + stock.getSymbol());
			}
		});
		System.out.println(recommendations);
	}

	private static List<String> analyseStock(StockFeed feed, String ticker) throws IOException {
		List<String> recommendations = new ArrayList<>();
		Stock stock = feed.get(EXCHANGE.London, ticker, 1).get();
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);

		List<AbstractStrategy> strategies = new ArrayList<>();
		strategies.add(GlobalExtremaStrategy.buildStrategy(series));
		strategies.add(MovingMomentumStrategy.buildStrategy(series));
//		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
//		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
//		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

		// Initializing the trading history
		TradingRecord tradingRecord = new TradingRecord();

		for (AbstractStrategy strategy : strategies) {

			int endIndex = series.getEnd();
			if (strategy.getStrategy().shouldEnter(endIndex)) {
				// Our strategy should enter
				recommendations.add(strategy.getName() + " should buy " + ticker);
				boolean entered = tradingRecord.enter(endIndex, series.getLastTick().getAmount(), Decimal.TEN);
				if (entered) {
					Order entry = tradingRecord.getLastEntry();
					System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
							+ ", amount=" + entry.getAmount().toDouble() + ")");
				}
			} else if (strategy.getStrategy().shouldExit(endIndex)) {
				// Our strategy should exit
				recommendations.add(strategy.getName() + " should sell " + ticker);
				boolean exited = tradingRecord.exit(endIndex, series.getLastTick().getClosePrice(), Decimal.TEN);
				if (exited) {
					Order exit = tradingRecord.getLastExit();
					System.out.println("Exited on " + exit.getIndex() + " (price=" + exit.getPrice().toDouble()
							+ ", amount=" + exit.getAmount().toDouble() + ")");
				}
			}
		}
		return recommendations;
	}
}
