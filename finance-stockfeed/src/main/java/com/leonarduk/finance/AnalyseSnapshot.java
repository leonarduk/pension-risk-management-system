package com.leonarduk.finance;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Recommendation;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.FileUtils;
import com.leonarduk.finance.utils.HtmlTools;
import com.leonarduk.finance.utils.NumberUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import com.leonarduk.finance.utils.ValueFormatter;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.collect.Maps;

/**
 * This class is an example of a dummy trading bot using ta4j.
 */
public class AnalyseSnapshot {

	private final static Logger	logger	= Logger.getLogger(AnalyseSnapshot.class.getName());

	private static int			years	= 20;

	public static List<Valuation> analayzeAllEtfs(final List<Position> stocks,
	        final LocalDate fromDate, final LocalDate toDate) throws IOException {
		return stocks.parallelStream().map(s -> AnalyseSnapshot.analyseStock(s, fromDate, toDate))
		        .collect(Collectors.toList());
	}

	public static Valuation analyseStock(final Position stock2, final LocalDate fromDate,
	        final LocalDate toDate) {
		TimeSeries series;
		try {
			Optional<Stock> stock = stock2.getStock();
			if (!stock.isPresent()) {
				stock = IntelligentStockFeed.getFlatCashSeries(stock2.getInstrument(), 1);
			}
			series = TimeseriesUtils.getTimeSeries(stock.get(), fromDate, toDate);
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
			final Valuation valuation = AnalyseSnapshot.createValuation(stock2, mostRecentTick);
			for (final AbstractStrategy strategy : strategies) {

				final int endIndex = series.getEnd();
				if (strategy.getStrategy().shouldEnter(endIndex)) {
					// Our strategy should enter
					valuation.addRecommendation(strategy.getName(), new Recommendation(
					        RecommendedTrade.BUY, strategy, stock2.getInstrument()));
					final boolean entered = tradingRecord.enter(endIndex,
					        mostRecentTick.getAmount(), Decimal.TEN);
					if (entered) {
						final Order entry = tradingRecord.getLastEntry();
						System.out.println("Entered on " + entry.getIndex() + " (price="
						        + entry.getPrice().toDouble() + ", amount="
						        + entry.getAmount().toDouble() + ")");
					}
				}
				else if (strategy.getStrategy().shouldExit(endIndex)) {
					// Our strategy should exit
					valuation.addRecommendation(strategy.getName(), new Recommendation(
					        RecommendedTrade.SELL, strategy, stock2.getInstrument()));
					final boolean exited = tradingRecord.exit(endIndex,
					        mostRecentTick.getClosePrice(), Decimal.TEN);
					if (exited) {
						final Order exit = tradingRecord.getLastExit();
						System.out.println("Exited on " + exit.getIndex() + " (price="
						        + exit.getPrice().toDouble() + ", amount="
						        + exit.getAmount().toDouble() + ")");
					}
				}
				else {
					valuation.addRecommendation(strategy.getName(), new Recommendation(
					        RecommendedTrade.HOLD, strategy, stock2.getInstrument()));
				}
			}

			valuation.addReturn(Period.days(1), AnalyseSnapshot.calculateReturn(series, 1));
			valuation.addReturn(Period.days(5), AnalyseSnapshot.calculateReturn(series, 5));
			valuation.addReturn(Period.days(21), AnalyseSnapshot.calculateReturn(series, 21));
			valuation.addReturn(Period.days(63), AnalyseSnapshot.calculateReturn(series, 63));
			valuation.addReturn(Period.days(365), AnalyseSnapshot.calculateReturn(series, 365));

			return valuation;
		}
		catch (final Exception e) {
			AnalyseSnapshot.logger.warning("Failed:" + e.getMessage());
			return new Valuation(stock2, BigDecimal.ZERO, LocalDate.now(), BigDecimal.ONE);
		}
	}

	public static BigDecimal calculateReturn(final TimeSeries series, final int timePeriod) {
		if (timePeriod > series.getEnd()) {
			return null;
		}
		final BigDecimal initialValue = BigDecimal
		        .valueOf(series.getFirstTick().getClosePrice().toDouble());
		final int i = timePeriod;

		final BigDecimal closePrice = BigDecimal
		        .valueOf(series.getTick(i).getClosePrice().toDouble());
		final BigDecimal diff = i > -1 ? closePrice.subtract(initialValue) : BigDecimal.ZERO;
		return NumberUtils.roundDecimal(diff.divide(initialValue, 4, RoundingMode.HALF_UP)
		        .multiply(BigDecimal.valueOf(100)));
	}

	public static Position createEmptyPortfolioPosition() {
		final String name = "Portfolio";
		final Instrument PORTFOLIO = Instrument.createPortfolioInstrument(name);

		final Optional<Stock> stock2 = Optional.of(new Stock(PORTFOLIO));
		final Position position = new Position(name, PORTFOLIO, BigDecimal.ONE, stock2, "");
		return position;
	}

	public static StringBuilder createPortfolioReport(final LocalDate fromDate,
	        final LocalDate toDate, final boolean interpolate, final boolean extendedReport,
	        final boolean createSeriesLinks) throws IOException, URISyntaxException {

		final List<Position> positions = AnalyseSnapshot.getPositions();
		final List<Instrument> heldInstruments = positions.stream()
		        .filter(p -> p.getInstrument().equals(Instrument.UNKNOWN))
		        .map(p -> p.getInstrument()).collect(Collectors.toList());

		List<Position> emptyPositions = Lists.newArrayList();

		if (extendedReport) {
			emptyPositions = AnalyseSnapshot.getListedInstruments(heldInstruments);
		}
		final StringBuilder sbBody = new StringBuilder();
		final StringBuilder sbHead = new StringBuilder();

		// IntelligentStockFeed.setRefresh(false);

		final List<Valuation> valuations = AnalyseSnapshot.analayzeAllEtfs(positions, fromDate,
		        toDate);

		AnalyseSnapshot.createValuationsTable(valuations, sbBody, true, createSeriesLinks, fromDate,
		        toDate, interpolate);
		sbBody.append("<hr/>");

		final Map<String, Double> assetTypeMap = valuations.parallelStream()
		        .collect(Collectors.groupingByConcurrent(
		                v -> v.getPosition().getInstrument().assetType().name(), Collectors
		                        .summingDouble((v -> v.getValuation().doubleValue()))));
		final Map<String, Double> underlyingTypeMap = valuations.parallelStream()
		        .collect(Collectors.groupingByConcurrent(
		                v -> v.getPosition().getInstrument().underlyingType().name(), Collectors
		                        .summingDouble((v -> v.getValuation().doubleValue()))));

		HtmlTools.addPieChartAndTable(assetTypeMap, sbBody, valuations, "Owned Assets", "Type",
		        "Value");
		HtmlTools.addPieChartAndTable(underlyingTypeMap, sbBody, valuations, "Underlying Assets",
		        "Type", "Value");

		AnalyseSnapshot.createValuationsTable(
		        AnalyseSnapshot.analayzeAllEtfs(emptyPositions, fromDate, toDate), sbBody, false,
		        createSeriesLinks, fromDate, toDate, interpolate);

		final StringBuilder buf = HtmlTools.createHtmlText(sbHead, sbBody);

		return buf;
	}

	public static StringBuilder createPortfolioReport(final String fromDate, final String toDate,
	        final boolean interpolate, final boolean extendedReport,
	        final boolean createSeriesLinks) throws IOException, URISyntaxException {
		final LocalDate fromLocalDate = StringUtils.isEmpty(fromDate)
		        ? LocalDate.now().minusYears(1) : LocalDate.parse(fromDate);
		final LocalDate toLocalDate = StringUtils.isEmpty(toDate) ? LocalDate.now()
		        : LocalDate.parse(toDate);
		return AnalyseSnapshot.createPortfolioReport(fromLocalDate, toLocalDate, interpolate,
		        extendedReport, createSeriesLinks);

	}

	public static Valuation createValuation(final Position position, final Tick lastTick) {
		BigDecimal price = BigDecimal.valueOf(lastTick.getClosePrice().toDouble());
		if (position.getInstrument().currency().equals("GBX")) {
			price = price.divide(BigDecimal.valueOf(100));
		}
		final BigDecimal volume = position.getAmount();
		final Valuation valuation = new Valuation(position,
		        NumberUtils.roundDecimal(price.multiply(volume)),
		        lastTick.getEndTime().toLocalDate(), NumberUtils.roundDecimal(price));
		return valuation;
	}

	protected static void createValuationsTable(final List<Valuation> valuations,
	        final StringBuilder sb, final boolean showPositionsHeld,
	        final boolean createSeriesLinks, final LocalDate fromDate, final LocalDate toDate,
	        final boolean interpolate) {

		final List<List<DataField>> records = Lists.newLinkedList();

		for (final Valuation valuation : valuations) {
			final List<DataField> fields = Lists.newLinkedList();
			records.add(fields);
			AnalyseSnapshot.logger.info(valuation.toString());
			final Instrument instrument = valuation.getPosition().getInstrument();

			final String ticker = instrument.code();

			final ValueFormatter formatter = (value -> {
				return new StringBuilder("<a href=\"/stock/ticker/").append(ticker)
				        .append("?fromDate=").append(fromDate).append("&toDate=").append(toDate)
				        .append("&interpolate=").append(interpolate).append("\">").append(value)
				        .append("</a>").toString();
			});

			fields.add(new DataField("Name", instrument.getName(), formatter));
			fields.add(new DataField("ISIN", instrument.getIsin(), formatter));
			fields.add(new DataField("Code", instrument.getCode(), formatter));
			fields.add(new DataField("Sector", instrument.getCategory(), formatter));
			fields.add(new DataField("Type", instrument.getAssetType().name(), formatter));

			fields.add(new DataField("Quantity Owned", valuation.getPosition().getAmount(),
			        formatter, showPositionsHeld));
			fields.add(new DataField("Value Owned", valuation.getValuation(), formatter,
			        showPositionsHeld));

			final String valuationDate = valuation.getValuationDate();

			fields.add(new DataField("Price", valuation.getPrice()));
			fields.add(new DataField("AsOf", valuationDate));

			for (final int day : new Integer[] { 1, 5, 21, 63, 365 }) {
				fields.add(new DataField(day + "D", valuation.getReturn(Period.days(day))));
			}

			for (final String name : new String[] { "SMA (12days)", "SMA (20days)", "SMA (50days)",
			        "Global Extrema", "Moving Momentum", }) {
				fields.add(new DataField(name, valuation.getRecommendation(name)));
			}
		}

		HtmlTools.printTable(sb, records);
	}

	public static List<Position> getListedInstruments(final List<Instrument> heldInstruments)
	        throws IOException {
		final List<Instrument> emptyInstruments = Lists.newArrayList(Instrument.values());
		final IntelligentStockFeed feed = new IntelligentStockFeed();
		emptyInstruments.removeAll(heldInstruments);
		return emptyInstruments.stream().map(instrument -> {
			return new Position("", instrument, BigDecimal.ZERO,
			        feed.get(instrument, AnalyseSnapshot.years), instrument.getIsin());
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public static Valuation getPortfolioValuation(final List<Valuation> valuedPositions,
	        final LocalDate valuationDate) {
		final Position portfolioPosition = AnalyseSnapshot.createEmptyPortfolioPosition();
		final Map<Period, BigDecimal> returns = Maps.newConcurrentMap();
		BigDecimal total = BigDecimal.ZERO;
		// so want to weight the returns to show value, 1d,5d,21d,63d,365d returns
		// turn relative return to absolute and add-up
		for (final Valuation valuation : valuedPositions) {
			for (final Entry<Period, BigDecimal> ret : valuation.getReturns().entrySet()) {
				final BigDecimal value = ret.getValue() == null ? BigDecimal.ZERO : ret.getValue();
				returns.put(ret.getKey(), returns.getOrDefault(ret.getKey(), BigDecimal.ZERO)
				        .add(value.multiply(valuation.getValuation())));
			}
			total = total.add(valuation.getValuation());
		}
		final Valuation portfolioValuation = new Valuation(portfolioPosition, total, valuationDate,
		        BigDecimal.ONE);
		for (final Entry<Period, BigDecimal> ret : returns.entrySet()) {
			returns.put(ret.getKey(), ret.getValue().divide(total, 2, RoundingMode.HALF_UP));
		}
		portfolioValuation.setValuation(returns);

		return portfolioValuation;
	}

	public static List<Position> getPositions() throws IOException {
		final List<Position> positions = InvestmentsFileReader
		        .getPositionsFromCSVFile("resources/data/portfolios.csv");
		return positions;
	}

	public static void main(final String[] args)
	        throws InterruptedException, IOException, URISyntaxException {
		final StringBuilder buf = AnalyseSnapshot.createPortfolioReport(LocalDate.now(),
		        LocalDate.now().minusYears(1), true, true, false);
		FileUtils.writeFile("recommendations.html", buf);
	}
}
