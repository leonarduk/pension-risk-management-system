package com.leonarduk.finance.api;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.AnalysisCriterion;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.Order;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.TimeSeriesManager;
import org.ta4j.core.TradingRecord;
import org.ta4j.core.analysis.criteria.TotalProfitCriterion;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.leonarduk.finance.analysis.TraderOrderUtils;
import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.portfolio.Recommendation;
import com.leonarduk.finance.portfolio.RecommendedTrade;
import com.leonarduk.finance.portfolio.Valuation;
import com.leonarduk.finance.portfolio.ValuationReport;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockV1;
import com.leonarduk.finance.stockfeed.StockFeed;
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

/**
 * This class is an example of a dummy trading bot using ta4j.
 */
public class SnapshotAnalyser {

	private final IntelligentStockFeed feed;

	private final static Logger logger = LoggerFactory.getLogger(SnapshotAnalyser.class.getName());

	private final static String TYPE = "Type";

	private final static String VALUE = "Value";
	private final static int years = 20;

	public SnapshotAnalyser() {
		this.feed = new IntelligentStockFeed();
	}

	public SnapshotAnalyser(final IntelligentStockFeed intelligentStockFeed) {
		this.feed = intelligentStockFeed;
	}

	private void addPortfolioDetails(final LocalDate fromDate, final LocalDate toDate, final boolean interpolate,
			final boolean createSeriesLinks, final StringBuilder sbBody, final List<Valuation> valuations,
			final String portfolioName) {
		sbBody.append("Portfolio: " + portfolioName + "<br>");
		final ValuationReport report = this.createValuationReport(fromDate, toDate, valuations, portfolioName);

		final List<Valuation> valuations2 = report.getValuations().stream()
				.filter(val -> val.getPosition().getPortfolios().contains(portfolioName)).collect(Collectors.toList());

		valuations2.add(report.getPortfolioValuation());
		this.createValuationsTable(valuations2, sbBody, true, createSeriesLinks, fromDate, toDate, interpolate);
		sbBody.append("<hr/>");

		// final Map<String, Double> assetTypeMap = valuations.parallelStream()
		// .collect(Collectors.groupingByConcurrent(
		// v -> v.getPosition().getInstrument().assetType().name(), Collectors
		// .summingDouble((v -> v.getValuation().doubleValue()))));
		// final Map<String, Double> underlyingTypeMap =
		// valuations.parallelStream()
		// .collect(Collectors.groupingByConcurrent(
		// v -> v.getPosition().getInstrument().underlyingType().name(),
		// Collectors
		// .summingDouble((v -> v.getValuation().doubleValue()))));

		// try {
		// HtmlTools.addPieChartAndTable(assetTypeMap, sbBody, valuations,
		// "Owned Assets",
		// SnapshotAnalyser.TYPE, SnapshotAnalyser.VALUE);
		// HtmlTools.addPieChartAndTable(underlyingTypeMap, sbBody, valuations,
		// "Underlying Assets", SnapshotAnalyser.TYPE, SnapshotAnalyser.VALUE);
		//
		// }
		// catch (final IOException e) {
		// sbBody.append("Failed to create images:" + e.getMessage());
		// }
	}

	public List<Valuation> analayzeAllEtfs(final List<Position> stocks, final LocalDate fromDate,
			final LocalDate toDate) throws IOException {
		return stocks.parallelStream().map(s -> this.analyseStock(s, fromDate, toDate)).collect(Collectors.toList());
	}

	public Valuation analyseStock(final Position stock2, final LocalDate fromDate, final LocalDate toDate) {
		TimeSeries series;
		try {
			Optional<StockV1> stock = stock2.getStock();
			if (!stock.isPresent()) {
				stock = IntelligentStockFeed.getFlatCashSeries(stock2.getInstrument(), 1);
			}
			series = TimeseriesUtils.getTimeSeries(stock.get(), fromDate, toDate);
			if ((null == series) || (series.getBarCount() < 1)) {
				throw new IllegalArgumentException("No data");
			}

			final List<AbstractStrategy> strategies = new ArrayList<>();
			strategies.add(GlobalExtremaStrategy.buildStrategy(series));
			strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
			strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

			// IndicatorsToCsv.exportIndicatorsToCsv(series);
			final TradingRecord tradingRecord = new BaseTradingRecord();

			final Bar mostRecentBar = series.getLastBar();
			final Valuation valuation = this.createValuation(stock2, mostRecentBar);
			for (final AbstractStrategy strategy : strategies) {

				final int endIndex = series.getEndIndex();
				if (strategy.getStrategy().shouldEnter(endIndex)) {
					// Our strategy should enter
					valuation.addRecommendation(strategy.getName(),
							new Recommendation(RecommendedTrade.BUY, strategy, stock2.getInstrument()));
					final boolean entered = tradingRecord.enter(endIndex, mostRecentBar.getAmount(),
							DoubleNum.valueOf(10));
					if (entered) {
						final Order entry = tradingRecord.getLastEntry();
						this.showTradeAction(entry, "Enter");
					}
				} else if (strategy.getStrategy().shouldExit(endIndex)) {
					// Our strategy should exit
					valuation.addRecommendation(strategy.getName(),
							new Recommendation(RecommendedTrade.SELL, strategy, stock2.getInstrument()));
					final boolean exited = tradingRecord.exit(endIndex, mostRecentBar.getClosePrice(),
							DoubleNum.valueOf(10));
					if (exited) {
						final Order exit = tradingRecord.getLastExit();
						this.showTradeAction(exit, "Exit");
					}
				} else {
					valuation.addRecommendation(strategy.getName(),
							new Recommendation(RecommendedTrade.HOLD, strategy, stock2.getInstrument()));
				}
			}

			valuation.addReturn(Period.ofDays(1), this.calculateReturn(series, 1));
			valuation.addReturn(Period.ofDays(5), this.calculateReturn(series, 5));
			valuation.addReturn(Period.ofDays(21), this.calculateReturn(series, 21));
			valuation.addReturn(Period.ofDays(63), this.calculateReturn(series, 63));
			valuation.addReturn(Period.ofDays(365), this.calculateReturn(series, 365));

			return valuation;
		} catch (final Exception e) {
			SnapshotAnalyser.logger.warn("Failed:" + e.getMessage());
			return new Valuation(stock2, BigDecimal.ZERO, LocalDate.now(), BigDecimal.ONE);
		}
	}

	/**
	 * @param series the time series
	 * @return a map (key: strategy, value: name) of trading strategies
	 */
	public List<AbstractStrategy> buildStrategiesList(final TimeSeries series) {
		// {Moving Momentum=24916, RSI-2=-81064, Global Extrema=23748, CCI
		// Correction=-28035}
		final List<AbstractStrategy> strategies = new ArrayList<>();
		strategies.add(GlobalExtremaStrategy.buildStrategy(series));
		strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));
		return strategies;
	}

	public BigDecimal calculateReturn(final TimeSeries series, final int timePeriod) {
		if (timePeriod > series.getEndIndex()) {
			return null;
		}
		final BigDecimal initialValue = BigDecimal.valueOf(series.getFirstBar().getClosePrice().doubleValue());
		final int i = timePeriod;

		final BigDecimal closePrice = BigDecimal.valueOf(series.getBar(i).getClosePrice().doubleValue());
		final BigDecimal diff = i > -1 ? closePrice.subtract(initialValue) : BigDecimal.ZERO;
		return NumberUtils
				.roundDecimal(diff.divide(initialValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)));
	}

	private void calculateSubseries(final List<AbstractStrategy> strategies, final AnalysisCriterion profitCriterion,
			final TimeSeries slice, final Map<String, AtomicInteger> scores) {
		final StringBuilder buf = new StringBuilder("Sub-series: " + slice.getSeriesPeriodDescription() + "\n");
		for (final AbstractStrategy entry : strategies) {
			final Strategy strategy = entry.getStrategy();
			final String name = entry.getName();
			// For each strategy...
			TimeSeriesManager manager = new TimeSeriesManager(slice);
			final TradingRecord tradingRecord = manager.run(strategy);
			final Num profit = profitCriterion.calculate(slice, tradingRecord);
			if (profit != DoubleNum.valueOf(1)) {
				if (DoubleNum.valueOf(1).isLessThan(profit)) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).incrementAndGet();
					System.out
							.println(TraderOrderUtils.getOrdersList(tradingRecord.getTrades(), slice, strategy, name));
				}
				if (DoubleNum.valueOf(1).isGreaterThan(profit)) {
					scores.putIfAbsent(name, new AtomicInteger());
					scores.get(name).decrementAndGet();
				}
			}
			buf.append("\tProfit for " + name + ": " + profit + "\n");
		}

	}

	public void computeForStrategies(final Map<String, AtomicInteger> totalscores, final StockFeed feed,
			final String Barer) throws IOException {
		final StockV1 stock = feed.get(Instrument.fromString(Barer), 2).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);
//		final List<TimeSeries> subseries = series.getSubSeries(0, 20);

		// Building the map of strategies
		final List<AbstractStrategy> strategies = this.buildStrategiesList(series);

		// The analysis criterion
		final AnalysisCriterion profitCriterion = new TotalProfitCriterion();
		final Map<String, AtomicInteger> scores = new ConcurrentHashMap<>();
//
//		for (final TimeSeries slice : subseries) {
//			// For each sub-series...
//			this.calculateSubseries(strategies, profitCriterion, slice, scores);
//		}

		for (final Entry<String, AtomicInteger> timeSeries : scores.entrySet()) {
			totalscores.putIfAbsent(timeSeries.getKey(), new AtomicInteger());
			totalscores.get(timeSeries.getKey()).addAndGet(timeSeries.getValue().get());
		}
		System.out.println(Barer + scores);
	}

	public Position createEmptyPortfolioPosition(final String name) {
		final Instrument PORTFOLIO = Instrument.createPortfolioInstrument(name);

		final Optional<StockV1> stock2 = Optional.of(new StockV1(PORTFOLIO));
		final Position position = new Position(name, PORTFOLIO, BigDecimal.ONE, stock2, "");
		return position;
	}

	public StringBuilder createPortfolioReport(final LocalDate fromDate, final LocalDate toDate,
			final boolean interpolate, final boolean extendedReport, final boolean createSeriesLinks)
			throws IOException, URISyntaxException {

		final List<Position> positions = this.getPositions();
		final List<Instrument> heldInstruments = positions.stream()
				.filter(p -> p.getInstrument().equals(Instrument.UNKNOWN)).map(p -> p.getInstrument())
				.collect(Collectors.toList());

		List<Position> emptyPositions = Lists.newArrayList();

		if (extendedReport) {
			emptyPositions = this.getListedInstruments(heldInstruments);
		}
		final StringBuilder sbBody = new StringBuilder();
		final StringBuilder sbHead = new StringBuilder();

		final List<Valuation> valuations = this.analayzeAllEtfs(positions, fromDate, toDate);

		this.getPortfolios().stream().forEach(portfolioName -> this.addPortfolioDetails(fromDate, toDate, interpolate,
				createSeriesLinks, sbBody, valuations, portfolioName));

		this.createValuationsTable(this.analayzeAllEtfs(emptyPositions, fromDate, toDate), sbBody, false,
				createSeriesLinks, fromDate, toDate, interpolate);

		final StringBuilder buf = HtmlTools.createHtmlText(sbHead, sbBody);

		return buf;
	}

	public StringBuilder createPortfolioReport(final String fromDate, final String toDate, final boolean interpolate,
			final boolean extendedReport, final boolean createSeriesLinks) throws IOException, URISyntaxException {
		final LocalDate fromLocalDate = StringUtils.isEmpty(fromDate) ? LocalDate.now().minusYears(2)
				: LocalDate.parse(fromDate);
		final LocalDate toLocalDate = StringUtils.isEmpty(toDate) ? LocalDate.now() : LocalDate.parse(toDate);
		return this.createPortfolioReport(fromLocalDate, toLocalDate, interpolate, extendedReport, createSeriesLinks);

	}

	public Valuation createValuation(final Position position, final Bar lastBar) {
		BigDecimal price = BigDecimal.valueOf(lastBar.getClosePrice().doubleValue());
		if (position.getInstrument().currency().equals("GBX")) {
			price = price.divide(BigDecimal.valueOf(100));
		}
		final BigDecimal volume = position.getAmount();
		final Valuation valuation = new Valuation(position, NumberUtils.roundDecimal(price.multiply(volume)),
				lastBar.getEndTime().toLocalDate(), NumberUtils.roundDecimal(price));
		return valuation;
	}

	public ValuationReport createValuationReport(final LocalDate fromDate, final LocalDate toDate,
			final List<Valuation> valuations, final String portfolioName) {
		final List<Valuation> portfolioPositions = valuations.stream()
				.filter(val -> val.getPosition().getPortfolios().contains(portfolioName)).collect(Collectors.toList());
		final Valuation portfolioValuation = this.getPortfolioValuation(portfolioPositions, toDate, portfolioName);
		return new ValuationReport(valuations, portfolioValuation, fromDate, toDate);
	}

	protected void createValuationsTable(final List<Valuation> valuations, final StringBuilder sb,
			final boolean showPositionsHeld, final boolean createSeriesLinks, final LocalDate fromDate,
			final LocalDate toDate, final boolean interpolate) {

		final List<List<DataField>> records = Lists.newLinkedList();

		for (final Valuation valuation : valuations) {
			final List<DataField> fields = Lists.newLinkedList();
			records.add(fields);
			SnapshotAnalyser.logger.info(valuation.toString());
			final Instrument instrument = valuation.getPosition().getInstrument();

			final String Barer = instrument.code();

			final ValueFormatter formatter = (value -> {
				return new StringBuilder("<a href=\"/stock/Barer/").append(Barer).append("?fromDate=").append(fromDate)
						.append("&toDate=").append(toDate).append("&interpolate=").append(interpolate).append("\">")
						.append(value).append("</a>").toString();
			});

			fields.add(new DataField("Name", instrument.getName(), formatter));
			fields.add(new DataField("ISIN", instrument.getIsin(), formatter));
			fields.add(new DataField("Code", instrument.getCode(), formatter));
			fields.add(new DataField("Sector", instrument.getCategory(), formatter));
			fields.add(new DataField(SnapshotAnalyser.TYPE, instrument.getAssetType().name(), formatter));

			fields.add(
					new DataField("Quantity Owned", valuation.getPosition().getAmount(), formatter, showPositionsHeld));
			fields.add(new DataField("Value Owned", valuation.getValuation(), formatter, showPositionsHeld));

			final String valuationDate = valuation.getValuationDate();

			fields.add(new DataField("Price", valuation.getPrice()));
			fields.add(new DataField("AsOf", valuationDate));

			for (final int day : new Integer[] { 1, 5, 21, 63, 365 }) {
				fields.add(new DataField(day + "D", valuation.getReturn(Period.ofDays(day))));
			}

			for (final String name : new String[] { "SMA12days", "SMA20days", "SMA50days", "GlobalExtrema",
					"MovingMomentum", }) {
				fields.add(new DataField(name, valuation.getRecommendation(name)));
			}
		}

		HtmlTools.printTable(sb, records);
	}

	public List<Position> getListedInstruments(final List<Instrument> heldInstruments) throws IOException {
		final List<Instrument> emptyInstruments = Lists.newArrayList(Instrument.values());
		emptyInstruments.removeAll(heldInstruments);
		return emptyInstruments.stream().map(instrument -> {
			return new Position("", instrument, BigDecimal.ZERO, this.feed.get(instrument, SnapshotAnalyser.years),
					instrument.getIsin());
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	public Set<String> getPortfolios() throws IOException {
		return this.getPositions().stream().map(position -> position.getPortfolios()).flatMap(Set::stream)
				.collect(Collectors.toSet());
	}

	public Valuation getPortfolioValuation(final List<Valuation> valuedPositions, final LocalDate valuationDate,
			final String name) {
		final Position portfolioPosition = this.createEmptyPortfolioPosition(name);
		final Map<Period, BigDecimal> returns = Maps.newConcurrentMap();
		BigDecimal total = BigDecimal.ZERO;
		// so want to weight the returns to show value, 1d,5d,21d,63d,365d
		// returns
		// turn relative return to absolute and add-up
		for (final Valuation valuation : valuedPositions) {
			for (final Entry<Period, BigDecimal> ret : valuation.getReturns().entrySet()) {
				final BigDecimal value = ret.getValue() == null ? BigDecimal.ZERO : ret.getValue();
				returns.put(ret.getKey(), returns.getOrDefault(ret.getKey(), BigDecimal.ZERO)
						.add(value.multiply(valuation.getValuation())));
			}
			total = total.add(valuation.getValuation());
		}
		final Valuation portfolioValuation = new Valuation(portfolioPosition, total, valuationDate, BigDecimal.ONE);
		for (final Entry<Period, BigDecimal> ret : returns.entrySet()) {
			returns.put(ret.getKey(), ret.getValue().divide(total, 2, RoundingMode.HALF_UP));
		}
		portfolioValuation.setValuation(returns);

		return portfolioValuation;
	}

	public List<Position> getPositions() throws IOException {
		final List<Position> positions = InvestmentsFileReader.getPositionsFromCSVFile("resources/data/portfolios.csv");
		return positions;
	}

	public List<Position> getPositions(final String portfolio) throws IOException {
		return this.getPositions().stream().filter(position -> position.getPortfolios().contains(portfolio))
				.collect(Collectors.toList());
	}

	public void main(final String[] args) throws InterruptedException, IOException, URISyntaxException {
		final StringBuilder buf = this.createPortfolioReport(LocalDate.now(), LocalDate.now().minusYears(1), true, true,
				false);
		FileUtils.writeFile("recommendations.html", buf);
	}

	public void showTradeAction(final Order entry, final String action) {
		SnapshotAnalyser.logger.info(action + "ed on " + entry.getIndex() + " (price=" + entry.getPrice().doubleValue()
				+ ", amount=" + entry.getAmount().doubleValue() + ")");
	}
}
