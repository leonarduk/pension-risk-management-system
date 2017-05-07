package com.leonarduk.finance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Recommendation;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.file.IndicatorsToCsv;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import com.leonarduk.finance.utils.NumberUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

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

	private static int years = 20;

	public static List<Valuation> analayzeAllEtfs(final List<Position> stocks) throws IOException {
		return stocks.parallelStream().map(AnalyseSnapshot::analyseStock).collect(Collectors.toList());
	}

	public static Valuation analyseStock(final Position stock2) {
		TimeSeries series;
		try {
			Optional<Stock> stock = stock2.getStock();
			if (!stock.isPresent()) {
				stock = IntelligentStockFeed.getFlatCashSeries(stock2.getInstrument(), 1);
			}
			series = TimeseriesUtils.getTimeSeries(stock.get());
			if ((null == series) || (series.getTickCount() < 1)) {
				throw new IllegalArgumentException("No data");
			}

			final List<AbstractStrategy> strategies = new ArrayList<>();
			strategies.add(GlobalExtremaStrategy.buildStrategy(series));
			strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

			// IndicatorsToCsv.exportIndicatorsToCsv(series);
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
			logger.warning("Failed:" + e.getMessage());
			return new Valuation(stock2, Decimal.NaN, LocalDate.now(), Decimal.ONE);
		}
	}

	public static Decimal calculateReturn(final TimeSeries series, final int timePeriod) {
		if (timePeriod > series.getEnd()) {
			return Decimal.NaN;
		}
		final Decimal initialValue = series.getFirstTick().getClosePrice();
		final int i = timePeriod;
		final Decimal diff = i > -1 ? series.getTick(i).getClosePrice().minus(initialValue) : Decimal.ZERO;
		return NumberUtils.roundDecimal(diff.dividedBy(initialValue).multipliedBy(Decimal.HUNDRED));
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

		HtmlTools.addPieChartAndTable(assetTypeMap, sbBody, valuations, "Owned Assets", "Type", "Value");
		HtmlTools.addPieChartAndTable(underlyingTypeMap, sbBody, valuations, "Underlying Assets", "Type", "Value");

		createValuationsTable(analayzeAllEtfs(emptyPositions), sbBody, false);

		final StringBuilder buf = HtmlTools.createHtmlText(sbHead, sbBody);

		return buf;
	}

	public static Valuation createValuation(final Position position, final Tick lastTick) {
		Decimal price = lastTick.getClosePrice();
		if (position.getInstrument().currency().equals("GBX")) {
			price = price.dividedBy(Decimal.HUNDRED);
		}
		final Decimal volume = position.getAmount();
		final Valuation valuation = new Valuation(position, NumberUtils.roundDecimal(price.multipliedBy(volume)),
				lastTick.getEndTime().toLocalDate(), NumberUtils.roundDecimal(price));
		return valuation;
	}

	protected static void createValuationsTable(final List<Valuation> valuations, final StringBuilder sb,
			final boolean showPositionsHeld) {

		final List<List<DataField>> records = Lists.newLinkedList();

		for (final Valuation valuation : valuations) {
			final List<DataField> fields = Lists.newLinkedList();
			records.add(fields);
			logger.info(valuation.toString());
			final Instrument instrument = valuation.getPosition().getInstrument();

			fields.add(new DataField("Name", instrument.getName()));
			fields.add(new DataField("ISIN", instrument.getIsin(), true));
			fields.add(new DataField("Code", instrument.getCode()));
			fields.add(new DataField("Sector", instrument.getCategory()));
			fields.add(new DataField("Type", instrument.getAssetType().name()));

			fields.add(new DataField("Quantity Owned", valuation.getPosition().getAmount(), showPositionsHeld));
			fields.add(new DataField("Value Owned", valuation.getValuation(), showPositionsHeld));

			final LocalDate valuationDate = valuation.getValuationDate();

			fields.add(new DataField("Price", valuation.getPrice()));
			fields.add(new DataField("AsOf", valuationDate));

			for (final int day : new Integer[] { 1, 5, 21, 63, 365 }) {
				fields.add(new DataField(day + "D", valuation.getReturn(Period.days(day))));
			}

			for (final String name : new String[] { "SMA (12days)", "SMA (20days)", "SMA (50days)", "Global Extrema",
					"Moving Momentum", }) {
				fields.add(new DataField(name, valuation.getRecommendation(name).getTradeRecommendation()));
			}
		}

		HtmlTools.printTable(sb, records);
	}

	public static List<Position> getListedInstruments(final List<Instrument> heldInstruments) throws IOException {
		final List<Instrument> emptyInstruments = Lists.newArrayList(Instrument.values());
		final IntelligentStockFeed feed = new IntelligentStockFeed();
		emptyInstruments.removeAll(heldInstruments);
		return emptyInstruments.stream().map(instrument -> {
			return new Position("", instrument, Decimal.ZERO, feed.get(instrument, years), instrument.getIsin());
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static void main(final String[] args) throws InterruptedException, IOException, URISyntaxException {
		final StringBuilder buf = createPortfolioReport(true);
		IndicatorsToCsv.writeFile("recommendations.html", buf);
	}

}
