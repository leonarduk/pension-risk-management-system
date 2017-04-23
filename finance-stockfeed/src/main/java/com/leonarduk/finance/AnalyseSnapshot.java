package com.leonarduk.finance;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.google.common.io.Resources;
import com.leonarduk.finance.chart.ChartDisplay;
import com.leonarduk.finance.chart.PieChartFactory;
import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Recommendation;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.stockfeed.DailyTimeseries;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.finance.stockfeed.file.IndicatorsToCsv;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import jersey.repackaged.com.google.common.collect.Lists;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AnalyseSnapshot {
	private final static Logger logger = Logger.getLogger(AnalyseSnapshot.class.getName());
	private static DecimalFormat format;
	private static int years = 20;

	public static void main(String[] args) throws InterruptedException, IOException {
		StringBuilder buf = createPortfolioReport();
		IndicatorsToCsv.writeFile("recommendations.html", buf);
	}

	public static StringBuilder createPortfolioReport() throws IOException {
		List<Position> positions = InvestmentsFileReader.getPositionsFromCSVFile(
				new File(Resources.getResource("data/portfolios.csv").getFile()).getAbsolutePath());
		List<Instrument> heldInstruments = positions.stream().filter(p -> p.getInstrument().equals(Instrument.UNKNOWN))
				.map(p -> p.getInstrument()).collect(Collectors.toList());
		// List<Position> emptyPositions =
		// getListedInstruments(heldInstruments);

		StringBuilder sbBody = new StringBuilder();
		StringBuilder sbHead = new StringBuilder();

		// IntelligentStockFeed.setRefresh(false);

		List<Valuation> valuations = analayzeAllEtfs(positions);
		createValuationsTable(valuations, sbBody, true);
		sbBody.append("<hr/>");
		PieChartFactory pieChartFactory = new PieChartFactory("Asset Allocation");
		valuations.stream().forEach(v -> {
			pieChartFactory.add(v.getPosition().getInstrument().assetType().name(), v.getValuation().toDouble());
		});
		TreeMap<String, Double> valueMap = new TreeMap<>(pieChartFactory.getValueMap());
		valueMap.put("Total", roundDecimal(Decimal.valueOf(pieChartFactory.getTotal())).toDouble());
		sbBody.append(ChartDisplay.getTable(valueMap, "Asset", "Value"));
		sbBody.append(ChartDisplay.saveImageAndReturnHtmlLink("assets", 400, 400, pieChartFactory.buildChart()));
		// createValuationsTable(analayzeAllEtfs(emptyPositions), sbBody,
		// false);

		StringBuilder buf = createHtmlText(sbHead, sbBody);
		return buf;
	}

	public static List<Position> getListedInstruments(List<Instrument> heldInstruments) {
		List<Instrument> emptyInstruments = Lists.newArrayList(Instrument.values());
		IntelligentStockFeed feed = new IntelligentStockFeed();
		emptyInstruments.removeAll(heldInstruments);
		return (List<Position>) emptyInstruments.stream().map(instrument -> {
			return new Position("", (Instrument) instrument, Decimal.ZERO,
					feed.get(EXCHANGE.London, instrument.getCode(), years), instrument.getIsin());
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static StringBuilder createHtmlText(StringBuilder sbHead, StringBuilder sbBody) {
		StringBuilder buf = new StringBuilder("<html><head>").append(sbHead).append("</head><body>");
		buf.append(sbBody).append("</body></html>\n");
		return buf;
	}

	protected static void createValuationsTable(List<Valuation> valuations, StringBuilder sb,
			boolean showPositionsHeld) {

		sb.append("<table><tr>");

		for (String name : new String[] { "Name", "ISIN", "Code", "Sector", "Type" }) {
			addHeader(name, sb);
		}

		if (showPositionsHeld) {
			for (String name : new String[] { "Quantity Owned", "Value Owned" }) {
				addHeader(name, sb);
			}
		}
		for (String name : new String[] { "Price", "AsOf", "Age of quote", "1D", "5D", "21D", "63D", "365d" }) {
			addHeader(name, sb);
		}

		String[] strategies = new String[] { "Global Extrema", "Moving Momentum", "SMA (12days)", "SMA (20days)",
				"SMA (50days)" };
		for (String name : strategies) {
			addHeader(name, sb);
		}
		sb.append("</tr>");
		for (Valuation optional : valuations) {
			logger.info(optional.toString());
			Instrument instrument = optional.getPosition().getInstrument();

			sb.append("<tr><td>");
			sb.append(instrument.fullName()).append("</td><td>"); //
			sb.append(instrument.getIsin()).append("</td><td>"); // ISIN
			sb.append(optional.getPosition().getSymbol()).append("</td><td>"); // Code
			sb.append(instrument.category()).append("</td><td>");
			sb.append(instrument.assetType()).append("</td><td>");

			if (showPositionsHeld) {
				sb.append(optional.getPosition().getAmount()).append("</td><td>");
				sb.append(optional.getValuation()).append("</td><td>");
			}
			sb.append(optional.getPrice()).append("</td><td>");
			LocalDate valuationDate = optional.getValuationDate();

			sb.append(valuationDate.toString()).append("</td>");
			addField(Decimal.valueOf(Days.daysBetween(LocalDate.now(), valuationDate).getDays()), sb);

			addField(optional.getReturn(Period.days(1)), sb);
			addField(optional.getReturn(Period.days(5)), sb);
			addField(optional.getReturn(Period.days(21)), sb);
			addField(optional.getReturn(Period.days(63)), sb);
			addField(optional.getReturn(Period.days(365)), sb);
			for (String name : strategies) {
				addField(optional.getRecommendation(name).getTradeRecommendation(), sb);
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
	}

	public static void addHeader(String name, StringBuilder sb) {
		sb.append("<th>").append(name).append("</th>");
	}

	public static void addField(RecommendedTrade recommendedTrade, StringBuilder sb) {
		String colour = "red";

		if (recommendedTrade != null && recommendedTrade.equals(RecommendedTrade.BUY)) {
			colour = "green";
		}
		if (recommendedTrade != null && recommendedTrade.equals(RecommendedTrade.HOLD)) {
			colour = "white";
		}
		sb.append("<td bgcolor='" + colour + "'>").append(recommendedTrade).append("</td>");
	}

	public static void addField(Decimal decimal, StringBuilder sb) {
		String colour = "red";

		if (decimal != null && decimal.isGreaterThan(Decimal.ZERO)) {
			colour = "green";
		} else if (decimal != null && decimal.equals(Decimal.ZERO)) {
			colour = "white";
		}
		sb.append("<td bgcolor='" + colour + "'>").append(decimal).append("</td>");
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
					valuation.addRecommendation(strategy,
							new Recommendation(RecommendedTrade.BUY, strategy, stock2.getInstrument()));
					boolean entered = tradingRecord.enter(endIndex, mostRecentTick.getAmount(), Decimal.TEN);
					if (entered) {
						Order entry = tradingRecord.getLastEntry();
						System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
								+ ", amount=" + entry.getAmount().toDouble() + ")");
					}
				} else if (strategy.getStrategy().shouldExit(endIndex)) {
					// Our strategy should exit
					valuation.addRecommendation(strategy,
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
			valuation.addReturn(Period.days(365), calculateReturn(series, 365));

			return valuation;
		} catch (Exception e) {
			return new Valuation(stock2, Decimal.NaN, LocalDate.now(), Decimal.ONE);
		}
	}

	public static Valuation createValuation(Position position, Tick lastTick) {
		Decimal price = lastTick.getClosePrice();
		if (position.getInstrument().currency().equals("GBX")) {
			price = price.dividedBy(Decimal.HUNDRED);
		}
		Decimal volume = position.getAmount();
		Valuation valuation = new Valuation(position, roundDecimal(price.multipliedBy(volume)),
				lastTick.getEndTime().toLocalDate(), roundDecimal(price));
		return valuation;
	}

	public static Decimal calculateReturn(TimeSeries series, int timePeriod) {
		Decimal initialValue = series.getLastTick().getClosePrice();
		int i = series.getEnd() - timePeriod;
		Decimal diff = i > -1 ? series.getTick(i).getClosePrice().minus(initialValue) : Decimal.ZERO;
		return roundDecimal(diff.dividedBy(initialValue).multipliedBy(Decimal.HUNDRED));
	}

	public static Decimal roundDecimal(Decimal decimal) {
		if (Decimal.NaN.equals(decimal))
			return decimal;
		format = new DecimalFormat("#.##");
		return Decimal.valueOf(format.format(decimal.toDouble()));
	}

}
