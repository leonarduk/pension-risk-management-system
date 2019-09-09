package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.yahoo.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.yahoo.ExtendedStockQuote;
import com.leonarduk.finance.stockfeed.yahoo.StockQuoteBuilder;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

import jersey.repackaged.com.google.common.collect.Lists;

public class IntelligentStockFeed extends AbstractStockFeed implements StockFeed {
	public static final Logger log = LoggerFactory.getLogger(IntelligentStockFeed.class.getName());

	public static boolean refresh = true;

	public static Optional<StockV1> getFlatCashSeries(final Instrument instrument, final int years) throws IOException {
		return IntelligentStockFeed.getFlatCashSeries(instrument, LocalDate.now().minusYears(years), LocalDate.now());
	}

	public static Optional<StockV1> getFlatCashSeries(final Instrument instrument, final LocalDate fromDate,
			final LocalDate toDate) throws IOException {
		final StockV1 cash = new StockV1(instrument);
		final List<ExtendedHistoricalQuote> history = Lists.newArrayList();
		history.add(new ExtendedHistoricalQuote(instrument, toDate, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
				BigDecimal.ONE, BigDecimal.ONE, 0l, "Manually created"));

		final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
		cash.setHistory(flatLineInterpolator.extendToFromDate(history, fromDate));
		cash.setQuote(new StockQuoteBuilder(instrument).setPrice(BigDecimal.ONE).build());
		return Optional.of(cash);
	}

	public static void setRefresh(final boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	public void addLatestQuoteToTheSeries(final StockV1 stock, final QuoteFeed dataFeed) throws IOException {
		// Add latest price to the series
		if ((dataFeed != null) && dataFeed.isAvailable()) {
			final ExtendedStockQuote quote = dataFeed.getStockQuote(stock.getInstrument());
			if ((quote != null) && quote.isPopulated()) {
				stock.getHistory()
						.add(new ExtendedHistoricalQuote(stock.getInstrument(),
								DateUtils.calendarToLocalDate(quote.getLastTradeTime()), quote.getOpen(),
								quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(),
								quote.getVolume(), Source.Yahoo.name()));
			} else {
				IntelligentStockFeed.log.warn(String.format("Failed to populate quote for %s", stock.getInstrument()));
			}
		}
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), false);
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final int years, final boolean interpolate)
			throws IOException {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), interpolate);
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate)
			throws IOException {
		return this.get(instrument, fromDate, toDate, false);
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate,
			final boolean interpolate) {
		try {

			if (instrument.equals(Instrument.CASH)) {
				return IntelligentStockFeed.getFlatCashSeries(instrument, fromDate, toDate);
			}
			final CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory.getDataFeed(Source.MANUAL);

			final Optional<StockV1> cachedData = this.getDataIfFeedAvailable(instrument, fromDate, toDate, dataFeed,
					true);

			StockFeed webDataFeed = StockFeedFactory.getDataFeed(instrument.getSource());

			// If we have the data already, don't bother to refresh
			// Note will need to update today's live quote still though,
			// so skip latest date point
			boolean getWebData = IntelligentStockFeed.refresh
					&& (webDataFeed.isAvailable() || StockFeedFactory.getDataFeed(Source.Google).isAvailable());
			if (getWebData && cachedData.isPresent()) {
				final List<ExtendedHistoricalQuote> cachedHistory = cachedData.get().getHistory();
				getWebData = !TimeseriesUtils.containsDatePoints(cachedHistory, fromDate,
						DateUtils.getPreviousDate(toDate));
			}

			Optional<StockV1> liveData = this.getDataIfFeedAvailable(instrument, fromDate, toDate, webDataFeed,
					IntelligentStockFeed.refresh);
			// Yahoo often give 503 errors when downloading history
			if (getWebData && webDataFeed.isAvailable() && !liveData.isPresent()
					&& webDataFeed.getSource().equals(Source.Yahoo)) {
				webDataFeed = StockFeedFactory.getDataFeed(Source.Google);
				liveData = this.getDataIfFeedAvailable(instrument, fromDate, toDate, webDataFeed,
						IntelligentStockFeed.refresh);
			}

			if (liveData.isPresent()) {
				final StockV1 stock = liveData.get();
				if (cachedData.isPresent()) {
					this.mergeSeries(cachedData.get(), stock.getHistory(), cachedData.get().getHistory());
				}
				TimeseriesUtils.cleanUpSeries(liveData);
				dataFeed.storeSeries(stock);
			} else {
				TimeseriesUtils.cleanUpSeries(cachedData);
				liveData = TimeseriesUtils.interpolateAndSortSeries(fromDate, toDate, interpolate, cachedData);
			}

			this.addLatestQuoteToTheSeries(liveData.get(), StockFeedFactory.getQuoteFeed(Source.Yahoo));

			TimeseriesUtils.cleanUpSeries(liveData);
			return TimeseriesUtils.interpolateAndSortSeries(fromDate, toDate, interpolate, liveData);
		} catch (final Exception e) {
			IntelligentStockFeed.log.warn(e.getMessage());
			return Optional.empty();
		}

	}

	public Optional<StockV1> get(final Instrument instrument, final String fromDate, final String toDate,
			final boolean interpolate) {
		return this.get(instrument, LocalDate.parse(fromDate), LocalDate.parse(toDate), interpolate);
	}

	public Optional<StockV1> getDataIfFeedAvailable(final Instrument instrument, final LocalDate fromDate,
			final LocalDate toDate, final StockFeed dataFeed, final boolean useFeed) throws IOException {
		final Optional<StockV1> data;
		if (useFeed) {
			if (dataFeed.isAvailable()) {
				data = dataFeed.get(instrument, fromDate, toDate);
			} else {
				IntelligentStockFeed.log.warn(dataFeed.getClass().getName() + " is not available");
				data = Optional.empty();
			}
		} else {
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
