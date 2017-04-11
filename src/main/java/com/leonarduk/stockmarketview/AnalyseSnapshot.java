package com.leonarduk.stockmarketview;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.leonarduk.stockmarketview.AnalyseSnapshot.Recommendation.Trade;
import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.IndicatorsToCsv;
import com.leonarduk.stockmarketview.stockfeed.Instrument;
import com.leonarduk.stockmarketview.stockfeed.IntelligentStockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.strategies.AbstractStrategy;
import com.leonarduk.stockmarketview.strategies.GlobalExtremaStrategy;
import com.leonarduk.stockmarketview.strategies.MovingMomentumStrategy;
import com.leonarduk.stockmarketview.strategies.SimpleMovingAverageStrategy;

import edu.emory.mathcs.backport.java.util.Arrays;
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
		StockFeed feed = new IntelligentStockFeed();
		analayzeAllEtfs(feed);
	}

	@SuppressWarnings("unchecked")
	public static void analayzeAllEtfs(StockFeed feed) throws IOException {
		analayzeAllEtfs(feed, Arrays.asList(Instrument.values()));

	}

	public static void analayzeAllEtfs(StockFeed feed, List<Instrument> stocks) throws IOException {
		Map<Instrument, Set<Recommendation>> recommendations = new ConcurrentHashMap<>();
		stocks.parallelStream().forEach(stock -> {
			try {
				recommendations.putAll(analyseStock(stock, feed));
			} catch (Exception e) {
				System.err.println("Failed to compute " + stock);
			}
		});

		StringBuilder buf = new StringBuilder("Symbol,Sector,Strategy,Recommendation\n");
		for (Set<Recommendation> symbol : recommendations.values()) {
			for (Recommendation recommendation : symbol) {
				buf.append(recommendation.instrument.code()).append(",").append(recommendation.instrument.category())
						.append(",").append(recommendation.strategy.getName()).append(",")
						.append(recommendation.tradeRecommendation.name()).append("\n");
			}

		}
		IndicatorsToCsv.writeFile("target/recommendations.csv", buf);
	}

	public static Map<Instrument, Set<Recommendation>> analyseStock(Instrument stock2, StockFeed feed)
			throws IOException {
		Map<Instrument, Set<Recommendation>> recommendations = new ConcurrentHashMap<>();

		Stock stock = feed.get(EXCHANGE.London, stock2.code(), 11).get();
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);

		List<AbstractStrategy> strategies = new ArrayList<>();
		strategies.add(GlobalExtremaStrategy.buildStrategy(series));
		strategies.add(MovingMomentumStrategy.buildStrategy(series));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

		IndicatorsToCsv.exportToCsv(series);
		// Initializing the trading history
		TradingRecord tradingRecord = new TradingRecord();

		for (AbstractStrategy strategy : strategies) {

			int endIndex = series.getEnd();
			if (strategy.getStrategy().shouldEnter(endIndex)) {
				// Our strategy should enter
				Set<Recommendation> tickerRecommendations = recommendations.getOrDefault(stock2, new HashSet<>());
				tickerRecommendations.add(new Recommendation(Trade.BUY, strategy, stock2));
				recommendations.put(stock2, tickerRecommendations);
				boolean entered = tradingRecord.enter(endIndex, series.getLastTick().getAmount(), Decimal.TEN);
				if (entered) {
					Order entry = tradingRecord.getLastEntry();
					System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
							+ ", amount=" + entry.getAmount().toDouble() + ")");
				}
			} else if (strategy.getStrategy().shouldExit(endIndex)) {
				// Our strategy should exit
				Set<Recommendation> tickerRecommendations = recommendations.getOrDefault(stock2, new HashSet<>());
				tickerRecommendations.add(new Recommendation(Trade.SELL, strategy, stock2));
				recommendations.put(stock2, tickerRecommendations);
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

	static class Recommendation {
		enum Trade {
			BUY, SELL, HOLD
		}

		private Trade tradeRecommendation;
		private AbstractStrategy strategy;
		private Instrument instrument;

		public Recommendation(Trade tradeRecommendation, AbstractStrategy strategy, Instrument stock2) {
			this.tradeRecommendation = tradeRecommendation;
			this.strategy = strategy;
			this.instrument = stock2;
		}

		public Trade getTradeRecommendation() {
			return tradeRecommendation;
		}

		public AbstractStrategy getStrategy() {
			return strategy;
		}

		public Instrument getSymbol() {
			return instrument;
		}

		@Override
		public String toString() {
			return "Recommendation [tradeRecommendation=" + tradeRecommendation + ", strategy=" + strategy + ", symbol="
					+ instrument + "]";
		}

	}

}
