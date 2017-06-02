package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.utils.TimeseriesUtils;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeed extends AbstractStockFeed
        implements StockFeed {
	public static final Logger	log		= Logger
	        .getLogger(IntelligentStockFeed.class.getName());

	public static boolean		refresh	= true;

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument,
	        final int years) {
		return IntelligentStockFeed.getFlatCashSeries(instrument,
		        LocalDate.now().minusYears(years), LocalDate.now());
	}

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate) {
		final Stock cash = new Stock(instrument);
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new HistoricalQuote(instrument, toDate, BigDecimal.ONE,
		        BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
		        0l, "Manually created"));

		final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
		cash.setHistory(
		        flatLineInterpolator.extendToFromDate(history, fromDate));
		cash.setQuote(new StockQuote.StockQuoteBuilder(instrument)
		        .setPrice(BigDecimal.ONE).build());
		return Optional.of(cash);
	}

	public static void setRefresh(final boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	public void addLatestQuoteToTheSeries(final Stock stock,
	        final QuoteFeed dataFeed) throws IOException {
		// Add latest price to the series
		if (dataFeed.isAvailable()) {
			final StockQuote quote = dataFeed
			        .getStockQuote(stock.getInstrument());
			if (quote.isPopulated()) {
				stock.getHistory()
				        .add(new HistoricalQuote(stock.getInstrument(),
				                LocalDate.fromCalendarFields(
				                        quote.getLastTradeTime()),
				                quote.getOpen(), quote.getDayLow(),
				                quote.getDayHigh(), quote.getPrice(),
				                quote.getPrice(), quote.getVolume(),
				                Source.Yahoo.name()));
			}
			else {
				throw new IOException(
				        String.format("Failed to populate quote for %s",
				                stock.getInstrument()));
			}
		}
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years),
		        LocalDate.now(), false);
	}

	public Optional<Stock> get(final Instrument instrument, final int years,
	        final boolean interpolate) throws IOException {
		return this.get(instrument, LocalDate.now().minusYears(years),
		        LocalDate.now(), interpolate);
	}

	@Override
	public Optional<Stock> get(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate)
	        throws IOException {
		return this.get(instrument, fromDate, toDate, false);
	}

	@Override
	public Optional<Stock> get(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate,
	        final boolean interpolate) {
		try {

			if (instrument.equals(Instrument.CASH)) {
				return IntelligentStockFeed.getFlatCashSeries(instrument,
				        fromDate, toDate);
			}
			final CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory
			        .getDataFeed(Source.MANUAL);

			final Optional<Stock> cachedData = this.getDataIfFeedAvailable(
			        instrument, fromDate, toDate, dataFeed, true);

			// If we have the data already, don't bother to refresh
			// Note will need to update today's live quote still though
			boolean getWebData = IntelligentStockFeed.refresh;
			if (getWebData && cachedData.isPresent()) {
				final List<HistoricalQuote> cachedHistory = cachedData.get()
				        .getHistory();
				getWebData = (cachedHistory.stream()
				        .filter(quote -> quote.getDate().isEqual(toDate)
				                || quote.getDate().isEqual(fromDate))
				        .collect(Collectors.toList()).size() != 2);
			}
			// Yahoo often give 503 errors when downloading history
			StockFeed webDataFeed = StockFeedFactory
			        .getDataFeed(instrument.getSource());
			Optional<Stock> liveData = this.getDataIfFeedAvailable(instrument,
			        fromDate, toDate, webDataFeed,
			        IntelligentStockFeed.refresh);
			if (webDataFeed.isAvailable() && !liveData.isPresent()
			        && webDataFeed.getSource().equals(Source.Yahoo)) {
				webDataFeed = StockFeedFactory.getDataFeed(Source.Google);
				liveData = this.getDataIfFeedAvailable(instrument, fromDate,
				        toDate, webDataFeed, IntelligentStockFeed.refresh);
			}

			final Stock stock = liveData.get();
			if (liveData.isPresent()) {
				if (cachedData.isPresent()) {
					this.mergeSeries(cachedData.get(), stock.getHistory(),
					        cachedData.get().getHistory());
				}
				dataFeed.storeSeries(stock);
			}
			else {
				return TimeseriesUtils.cleanUpSeries(fromDate, toDate,
				        interpolate, cachedData);
			}

			this.addLatestQuoteToTheSeries(stock,
			        StockFeedFactory.getQuoteFeed(Source.Yahoo));

			return TimeseriesUtils.cleanUpSeries(fromDate, toDate, interpolate,
			        liveData);
		}
		catch (final Exception e) {
			IntelligentStockFeed.log.warning(e.getMessage());
			return Optional.empty();
		}

	}

	public Optional<Stock> get(final Instrument instrument,
	        final String fromDate, final String toDate,
	        final boolean interpolate) {
		return this.get(instrument, LocalDate.parse(fromDate),
		        LocalDate.parse(toDate), interpolate);
	}

	public Optional<Stock> getDataIfFeedAvailable(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate,
	        final StockFeed dataFeed, final boolean useFeed)
	        throws IOException {
		final Optional<Stock> data;
		if (useFeed) {
			if (dataFeed.isAvailable()) {
				data = dataFeed.get(instrument, fromDate, toDate);
			}
			else {
				IntelligentStockFeed.log.warning(
				        dataFeed.getClass().getName() + " is not available");
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
