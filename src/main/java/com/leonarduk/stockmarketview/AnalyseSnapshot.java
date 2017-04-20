package com.leonarduk.stockmarketview;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.leonarduk.stockmarketview.portfolio.Position;
import com.leonarduk.stockmarketview.portfolio.Recommendation;
import com.leonarduk.stockmarketview.portfolio.RecommendedTrade;
import com.leonarduk.stockmarketview.portfolio.Valuation;
import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.Instrument;
import com.leonarduk.stockmarketview.stockfeed.IntelligentStockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.file.IndicatorsToCsv;
import com.leonarduk.stockmarketview.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.stockmarketview.strategies.AbstractStrategy;
import com.leonarduk.stockmarketview.strategies.GlobalExtremaStrategy;
import com.leonarduk.stockmarketview.strategies.MovingMomentumStrategy;
import com.leonarduk.stockmarketview.strategies.SimpleMovingAverageStrategy;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AnalyseSnapshot {
	private final static Logger logger = Logger.getLogger(AnalyseSnapshot.class.getName());
	private static DecimalFormat format;

	public static void main(String[] args) throws InterruptedException, IOException {
		StockFeed feed = new IntelligentStockFeed();
		String resource = "C:/Users/Stephen/FinanceWorkspace/stockmarketview/src/main/resources/portfolios.csv";

		List<Position> positions = InvestmentsFileReader.getPositionsFromCSVFile(resource);
		List<Instrument> heldInstruments = positions.stream().filter(p -> p.getInstrument().equals(Instrument.UNKNOWN))
				.map(p -> p.getInstrument()).collect(Collectors.toList());

		// List<Position> emptyPositions = (List<Position>)
		// Arrays.asList(Instrument.values()).stream().map(instrument -> {
		// return new Position("", (Instrument) instrument, BigDecimal.ZERO);
		// }).collect(Collectors.toList());
		//
		// Stock stock = feed.get(EXCHANGE.London, stock2.code(), 11).get();

		List<Valuation> valuations = analayzeAllEtfs(positions);
		StringBuilder sb = new StringBuilder();
		for (Valuation optional : valuations) {
			logger.info(optional.toString());
			sb.append(optional.toString()).append("\n");
		}
		IndicatorsToCsv.writeFile("recommendations.csv", sb);
	}

	public static List<Valuation> analayzeAllEtfs(List<Position> stocks) throws IOException {
		return stocks.parallelStream().map(AnalyseSnapshot::analyseStock).collect(Collectors.toList());
	}

	public static Valuation analyseStock(Position stock2) {
		TimeSeries series;
		try {
			series = DailyTimeseries.getTimeSeries(stock2.getStock().get());

			List<AbstractStrategy> strategies = new ArrayList<>();
			strategies.add(GlobalExtremaStrategy.buildStrategy(series));
			strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

			IndicatorsToCsv.exportIndicatorsToCsv(series);
			TradingRecord tradingRecord = new TradingRecord();

			Tick mostRecentTick = series.getFirstTick();
			Valuation valuation = createValuation(stock2, mostRecentTick);
			for (AbstractStrategy strategy : strategies) {

				int endIndex = series.getEnd();
				if (strategy.getStrategy().shouldEnter(endIndex)) {
					// Our strategy should enter
					valuation.addRecommendation(
							new Recommendation(RecommendedTrade.BUY, strategy, stock2.getInstrument()));
					boolean entered = tradingRecord.enter(endIndex, mostRecentTick.getAmount(), Decimal.TEN);
					if (entered) {
						Order entry = tradingRecord.getLastEntry();
						System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
								+ ", amount=" + entry.getAmount().toDouble() + ")");
					}
				} else if (strategy.getStrategy().shouldExit(endIndex)) {
					// Our strategy should exit
					valuation.addRecommendation(
							new Recommendation(RecommendedTrade.SELL, strategy, stock2.getInstrument()));
					boolean exited = tradingRecord.exit(endIndex, mostRecentTick.getClosePrice(), Decimal.TEN);
					if (exited) {
						Order exit = tradingRecord.getLastExit();
						System.out.println("Exited on " + exit.getIndex() + " (price=" + exit.getPrice().toDouble()
								+ ", amount=" + exit.getAmount().toDouble() + ")");
					}
				}
			}

			valuation.addReturn(Period.days(1), calculateReturn(series, 1));
			valuation.addReturn(Period.days(5), calculateReturn(series, 5));
			valuation.addReturn(Period.days(21), calculateReturn(series, 21));
			valuation.addReturn(Period.days(63), calculateReturn(series, 63));

			return valuation;
		} catch (Exception e) {
			return new Valuation(stock2, Decimal.NaN, LocalDate.now());
		}
	}

	protected static Valuation createValuation(Position position, Tick lastTick) {
		Decimal price = lastTick.getClosePrice();
		Decimal volume = position.getAmount();
		Valuation valuation = new Valuation(position, price.multipliedBy(volume),
				lastTick.getEndTime().toLocalDate());
		return valuation;
	}

	protected static Decimal calculateReturn(TimeSeries series, int timePeriod) {
		Decimal initialValue = series.getLastTick().getClosePrice();
		Decimal diff = series.getTick(series.getEnd() - timePeriod).getClosePrice().minus(initialValue);
		return roundDecimal(diff.dividedBy(initialValue).multipliedBy(Decimal.HUNDRED));
	}

	public static Decimal roundDecimal(Decimal decimal) {
		format = new DecimalFormat("#.##");
		return Decimal.valueOf(format.format(decimal.toDouble()));
	}

}
