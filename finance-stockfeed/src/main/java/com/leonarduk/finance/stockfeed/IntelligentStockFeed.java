package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.interpolation.BadDateRemover;
import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.interpolation.LinearInterpolator;
import com.leonarduk.finance.utils.TimeseriesUtils;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeed extends StockFeed {
	public static final Logger	log		= Logger.getLogger(IntelligentStockFeed.class.getName());

	public static boolean		refresh	= true;

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument, final int years) {
		return IntelligentStockFeed.getFlatCashSeries(instrument, LocalDate.now().minusYears(years),
		        LocalDate.now());
	}

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate) {
		final Stock cash = new Stock(instrument);
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new HistoricalQuote(instrument, toDate, BigDecimal.ONE, BigDecimal.ONE,
		        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0l, "Manually created"));

		final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
		cash.setHistory(flatLineInterpolator.extendToFromDate(history, fromDate));
		final StockQuote quote = new StockQuote(instrument);
		quote.setPrice(BigDecimal.ONE);
		cash.setQuote(quote);
		return Optional.of(cash);
	}

	public static void setRefresh(final boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	public Optional<Stock> cleanUpSeries(final LocalDate fromDate, final LocalDate toDate,
	        final boolean interpolate, final Optional<Stock> liveData) throws IOException {
		List<HistoricalQuote> history = liveData.get().getHistory();

		history = new BadDateRemover().clean(history);
		if (interpolate) {
			final LinearInterpolator linearInterpolator = new LinearInterpolator();
			final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();

			history = linearInterpolator.interpolate(flatLineInterpolator.extendToToDate(
			        flatLineInterpolator.extendToFromDate(history, fromDate), toDate));
		}
		final List<HistoricalQuote> subSeries = history.stream()
		        .filter(q -> (q.getDate().isAfter(fromDate) && q.getDate().isBefore(toDate))
		                || q.getDate().isEqual(fromDate) || q.getDate().isEqual(toDate))
		        .collect(Collectors.toList());
		TimeseriesUtils.sortQuoteList(subSeries);
		liveData.get().setHistory(subSeries);
		return liveData;
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), false);
	}

	public Optional<Stock> get(final Instrument instrument, final int years,
	        final boolean interpolate) throws IOException {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(),
		        interpolate);
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
	        final LocalDate toDate) throws IOException {
		return this.get(instrument, fromDate, toDate, false);
	}

	public Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
	        final LocalDate toDate, final boolean interpolate) {
		try {

			if (instrument.equals(Instrument.CASH)) {
				return IntelligentStockFeed.getFlatCashSeries(instrument, fromDate, toDate);
			}
			final CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory
			        .getDataFeed(Source.MANUAL);

			final Optional<Stock> cachedData = this.getDataIfFeedAvailable(instrument, fromDate,
			        toDate, dataFeed, true);

			// Yahoo often give 503 errors when downloading history
			StockFeed webDataFeed = StockFeedFactory.getDataFeed(instrument.getSource());
			Optional<Stock> liveData = this.getDataIfFeedAvailable(instrument, fromDate, toDate,
			        webDataFeed, IntelligentStockFeed.refresh);
			if (webDataFeed.isAvailable() && !liveData.isPresent()
			        && webDataFeed.getSource().equals(Source.Yahoo)) {
				webDataFeed = StockFeedFactory.getDataFeed(Source.Google);
				liveData = this.getDataIfFeedAvailable(instrument, fromDate, toDate, webDataFeed,
				        IntelligentStockFeed.refresh);
			}

			if (liveData.isPresent()) {
				if (cachedData.isPresent()) {
					this.mergeSeries(cachedData.get(), liveData.get().getHistory(),
					        cachedData.get().getHistory());
				}
				dataFeed.storeSeries(liveData.get());
			}
			else {
				return this.cleanUpSeries(fromDate, toDate, interpolate, cachedData);
			}
			return this.cleanUpSeries(fromDate, toDate, interpolate, liveData);
		}
		catch (final Exception e) {
			IntelligentStockFeed.log.warning(e.getMessage());
			return Optional.empty();
		}

	}

	public Optional<Stock> get(final Instrument instrument, final String fromDate,
	        final String toDate, final boolean interpolate) {
		return this.get(instrument, LocalDate.parse(fromDate), LocalDate.parse(toDate),
		        interpolate);
	}

	public Optional<Stock> getDataIfFeedAvailable(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate, final StockFeed dataFeed,
	        final boolean useFeed) throws IOException {
		final Optional<Stock> data;
		if (useFeed) {
			if (dataFeed.isAvailable()) {
				data = dataFeed.get(instrument, fromDate, toDate);
			}
			else {
				IntelligentStockFeed.log
				        .warning(dataFeed.getClass().getName() + " is not available");
				data = Optional.empty();
			}
		}
		else {
			data = Optional.empty();
		}
		return data;
	}

	@Override
	public Source getSource() {
		return Source.MANUAL;
	}

	@Override
	public boolean isAvailable() {
		return StockFeedFactory.getDataFeed(Source.MANUAL).isAvailable()
		        || StockFeedFactory.getDataFeed(Source.Google).isAvailable()
		        || StockFeedFactory.getDataFeed(Source.Yahoo).isAvailable();
	}

}
