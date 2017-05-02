package com.leonarduk.finance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.leonarduk.finance.chart.ChartDisplay;
import com.leonarduk.finance.chart.PieChartFactory;
import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Recommendation;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.file.IndicatorsToCsv;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.Stock;

/**
 * This class is an example of a dummy trading bot using ta4j.
 * <p>
 */
public class AnalyseSnapshot {
	public static class DataField {
		private final String name;
		private final Object value;
		private boolean display;

		public DataField(final String name, final Object value) {
			this(name, value, true);
		}

		public DataField(final String name, final Object value, final boolean display) {
			this.name = name;
			this.value = value;
			this.display = display;
		}

		public String getName() {
			return this.name;
		}

		public Object getValue() {
			return this.value;
		}

		public void hide() {
			this.display = false;
		}

		public boolean isDisplay() {
			return this.display;
		}

	}

	private final static Logger logger = Logger.getLogger(AnalyseSnapshot.class.getName());
	private static DecimalFormat format;

	private static int years = 20;

	public static void addField(final Object value, final StringBuilder sb) {
		sb.append("<td bgcolor='" + getColour(value) + "'>").append(value).append("</td>");
	}

	public static void addHeader(final String name, final StringBuilder sb) {
		sb.append("<th>").append(name).append("</th>");
	}

	public static void addPieChartAndTable(final Map<String, Double> assetTypeMap, final StringBuilder sbBody,
			final List<Valuation> valuations, final String title, final String key, final String value)
			throws IOException {
		final PieChartFactory pieChartFactory = new PieChartFactory(title);
		pieChartFactory.addAll(assetTypeMap);
		assetTypeMap.put("Total", roundDecimal(Decimal.valueOf(pieChartFactory.getTotal())).toDouble());
		sbBody.append(ChartDisplay.getTable(assetTypeMap, key, value));
		final String filename = title.replace(" ", "_");
		sbBody.append(ChartDisplay.saveImageAsSvgAndReturnHtmlLink(filename, 400, 400, pieChartFactory.buildChart()));
	}

	public static List<Valuation> analayzeAllEtfs(final List<Position> stocks) throws IOException {
		return stocks.parallelStream().map(AnalyseSnapshot::analyseStock).collect(Collectors.toList());
	}

	public static Valuation analyseStock(final Position stock2) {
		TimeSeries series;
		try {
			Optional<Stock> stock = stock2.getStock();
			if (!stock.isPresent()) {
				stock = IntelligentStockFeed.getFlatCashSeries(stock2.getInstrument(), stock2.getSymbol());
			}
			series = TimeseriesUtils.getTimeSeries(stock.get());

			final List<AbstractStrategy> strategies = new ArrayList<>();
			strategies.add(GlobalExtremaStrategy.buildStrategy(series));
			strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

			IndicatorsToCsv.exportIndicatorsToCsv(series);
			final TradingRecord tradingRecord = new TradingRecord();

			final Tick mostRecentTick = series.getLastTick();
			final Valuation valuation = createValuation(stock2, mostRecentTick);
			for (final AbstractStrategy strategy : strategies) {

				final int endIndex = series.getEnd();
				if (strategy.getStrategy().shouldEnter(endIndex)) {
					// Our strategy should enter
					valuation.addRecommendation(strategy,
							new Recommendation(RecommendedTrade.BUY, strategy, stock2.getInstrument()));
					final boolean entered = tradingRecord.enter(endIndex, mostRecentTick.getAmount(), Decimal.TEN);
					if (entered) {
						final Order entry = tradingRecord.getLastEntry();
						System.out.println("Entered on " + entry.getIndex() + " (price=" + entry.getPrice().toDouble()
								+ ", amount=" + entry.getAmount().toDouble() + ")");
					}
				} else if (strategy.getStrategy().shouldExit(endIndex)) {
					// Our strategy should exit
					valuation.addRecommendation(strategy,
							new Recommendation(RecommendedTrade.SELL, strategy, stock2.getInstrument()));
					final boolean exited = tradingRecord.exit(endIndex, mostRecentTick.getClosePrice(), Decimal.TEN);
					if (exited) {
						final Order exit = tradingRecord.getLastExit();
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
		} catch (final Exception e) {
			return new Valuation(stock2, Decimal.NaN, LocalDate.now(), Decimal.ONE);
		}
	}

	public static Decimal calculateReturn(final TimeSeries series, final int timePeriod) {
		final Decimal initialValue = series.getFirstTick().getClosePrice();
		final int i = timePeriod;
		final Decimal diff = i > -1 ? series.getTick(i).getClosePrice().minus(initialValue) : Decimal.ZERO;
		return roundDecimal(diff.dividedBy(initialValue).multipliedBy(Decimal.HUNDRED));
	}

	public static StringBuilder createHtmlText(final StringBuilder sbHead, final StringBuilder sbBody) {
		final StringBuilder buf = new StringBuilder("<html><head>").append(sbHead).append("</head><body>");
		buf.append(sbBody).append("</body></html>\n");
		return buf;
	}

	public static StringBuilder createPortfolioReport(final boolean extendedReport)
			throws IOException, URISyntaxException {

		final List<Position> positions = InvestmentsFileReader.getPositionsFromCSVFile("resources/data/portfolios.csv");
		final List<Instrument> heldInstruments = positions.stream()
				.filter(p -> p.getInstrument().equals(Instrument.UNKNOWN)).map(p -> p.getInstrument())
				.collect(Collectors.toList());

		List<Position> emptyPositions = Lists.newArrayList();

		if (extendedReport) {
			emptyPositions = getListedInstruments(heldInstruments);
		}
		final StringBuilder sbBody = new StringBuilder();
		final StringBuilder sbHead = new StringBuilder();

		// IntelligentStockFeed.setRefresh(false);

		final List<Valuation> valuations = analayzeAllEtfs(positions);

		createValuationsTable(valuations, sbBody, true);
		sbBody.append("<hr/>");

		final Map<String, Double> assetTypeMap = valuations.parallelStream()
				.collect(Collectors.groupingByConcurrent(v -> v.getPosition().getInstrument().assetType().name(),
						Collectors.summingDouble((v -> v.getValuation().toDouble()))));
		final Map<String, Double> underlyingTypeMap = valuations.parallelStream()
				.collect(Collectors.groupingByConcurrent(v -> v.getPosition().getInstrument().underlyingType().name(),
						Collectors.summingDouble((v -> v.getValuation().toDouble()))));

		addPieChartAndTable(assetTypeMap, sbBody, valuations, "Owned Assets", "Type", "Value");
		addPieChartAndTable(underlyingTypeMap, sbBody, valuations, "Underlying Assets", "Type", "Value");

		createValuationsTable(analayzeAllEtfs(emptyPositions), sbBody, true);

		final StringBuilder buf = createHtmlText(sbHead, sbBody);

		return buf;
	}

	public static Valuation createValuation(final Position position, final Tick lastTick) {
		Decimal price = lastTick.getClosePrice();
		if (position.getInstrument().currency().equals("GBX")) {
			price = price.dividedBy(Decimal.HUNDRED);
		}
		final Decimal volume = position.getAmount();
		final Valuation valuation = new Valuation(position, roundDecimal(price.multipliedBy(volume)),
				lastTick.getEndTime().toLocalDate(), roundDecimal(price));
		return valuation;
	}

	protected static void createValuationsTable(final List<Valuation> valuations, final StringBuilder sb,
			final boolean showPositionsHeld) {

		final List<List<DataField>> records = Lists.newLinkedList();

		for (final Valuation optional : valuations) {
			final List<DataField> fields = Lists.newLinkedList();
			records.add(fields);
			logger.info(optional.toString());
			final Instrument instrument = optional.getPosition().getInstrument();

			fields.add(new DataField("Name", instrument.getName()));
			fields.add(new DataField("ISIN", instrument.getIsin(), false));
			fields.add(new DataField("Code", instrument.getCode()));
			fields.add(new DataField("Sector", instrument.getCategory()));
			fields.add(new DataField("Type", instrument.getAssetType().name()));

			fields.add(new DataField("Quantity Owned", optional.getPosition().getAmount(), showPositionsHeld));
			fields.add(new DataField("Value Owned", optional.getValuation(), showPositionsHeld));

			final LocalDate valuationDate = optional.getValuationDate();

			fields.add(new DataField("Price", optional.getPrice()));
			fields.add(new DataField("AsOf", valuationDate));

			for (final int day : new Integer[] { 1, 5, 21, 63, 365 }) {
				fields.add(new DataField(day + "D", optional.getReturn(Period.days(day))));
			}

			for (final String name : new String[] { "Global Extrema", "Moving Momentum", "SMA (12days)", "SMA (20days)",
					"SMA (50days)" }) {
				fields.add(new DataField("Global Extrema", optional.getRecommendation(name).getTradeRecommendation()));
			}
		}

		if (records.size() > 0) {
			sb.append("<table><tr>");
			records.get(0).stream().forEach(f -> {
				if (f.isDisplay()) {
					addHeader(f.getName(), sb);
				}
			});
			sb.append("</tr>");

			for (final List<DataField> list : records) {
				sb.append("<tr>");
				list.stream().forEach(f -> {
					if (f.isDisplay()) {
						addField(f.getValue(), sb);
					}
				});
				sb.append("</tr>");

			}
			sb.append("</table>");
		}
	}

	public static String getColour(final Object value) {
		String colour = "white";
		if ((value != null) && (value.equals(RecommendedTrade.BUY) || ((value instanceof LocalDate)
				&& (Decimal.valueOf(Days.daysBetween(LocalDate.now(), ((LocalDate) value)).getDays()))
						.equals(Decimal.ZERO))

				|| ((value instanceof Decimal) && ((Decimal) value).isGreaterThan(Decimal.ZERO)))) {
			colour = "green";
		}

		if (((value != null) && value.equals(RecommendedTrade.SELL))
				|| ((value instanceof LocalDate)
						&& (Decimal.valueOf(Days.daysBetween(LocalDate.now(), ((LocalDate) value)).getDays()))
								.isGreaterThan(Decimal.ONE))
				|| ((value instanceof Decimal) && ((Decimal) value).isLessThan(Decimal.ZERO))) {
			colour = "red";
		}
		return colour;
	}

	public static List<Position> getListedInstruments(final List<Instrument> heldInstruments) {
		final List<Instrument> emptyInstruments = Lists.newArrayList(Instrument.values());
		final IntelligentStockFeed feed = new IntelligentStockFeed();
		emptyInstruments.removeAll(heldInstruments);
		return emptyInstruments.stream().map(instrument -> {
			return new Position("", instrument, Decimal.ZERO,
					feed.get(instrument.getExchange(), instrument.getCode(), years), instrument.getIsin());
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static void main(final String[] args) throws InterruptedException, IOException, URISyntaxException {
		final StringBuilder buf = createPortfolioReport(true);
		IndicatorsToCsv.writeFile("recommendations.html", buf);
	}

	public static Decimal roundDecimal(final Decimal decimal) {
		if (Decimal.NaN.equals(decimal)) {
			return decimal;
		}
		format = new DecimalFormat("#.##");
		return Decimal.valueOf(format.format(decimal.toDouble()));
	}

}
