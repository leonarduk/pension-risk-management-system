package com.leonarduk.finance.stockfeed;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.interpolation.BadDateRemover;
import com.leonarduk.finance.stockfeed.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.interpolation.LinearInterpolator;
import com.leonarduk.finance.utils.DateUtils;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeed extends StockFeed {
	public static final Logger log = Logger.getLogger(IntelligentStockFeed.class.getName());

	private static boolean refresh = true;

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument, final int years) {
		final Stock cash = new Stock(instrument);
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new HistoricalQuote(instrument, LocalDate.now(), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
				BigDecimal.ONE, BigDecimal.ONE, 0l, "Manually created"));

		final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
		final LocalDate fromDate = LocalDate.now().minusYears(years);
		cash.setHistory(flatLineInterpolator.extendToFromDate(history, fromDate));
		final StockQuote quote = new StockQuote(instrument);
		quote.setPrice(BigDecimal.ONE);
		cash.setQuote(quote);
		return Optional.of(cash);
	}

	public static void setRefresh(final boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		try {

			if (instrument.equals(Instrument.CASH)) {
				return getFlatCashSeries(instrument, years);
			}
			final CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory.getDataFeed(Source.MANUAL);

			final Optional<Stock> cachedData = dataFeed.get(instrument, years);

			final StockFeed feed = StockFeedFactory.getDataFeed(instrument.getSource());

			Optional<Stock> liveData;
			try {
				liveData = refresh ? feed.get(instrument, years) : Optional.empty();
			} catch (final Throwable e) {
				log.warning(e.getMessage());
				liveData = Optional.empty();
			}
			if (liveData.isPresent()) {
				if (cachedData.isPresent()) {
					this.mergeSeries(cachedData.get(), liveData.get().getHistory(), cachedData.get().getHistory());
				}
				dataFeed.storeSeries(liveData.get());
			} else {
				liveData = cachedData;
			}

			if (!liveData.isPresent()) {
				return liveData;
			}
			final LocalDate today = DateUtils.skipWeekends(LocalDate.now());
			final List<HistoricalQuote> history = liveData.get().getHistory();

			final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
			final LocalDate fromDate = today.minusYears(years);

			final List<HistoricalQuote> interpolate = new LinearInterpolator()
					.interpolate(new BadDateRemover().clean(history));
			final List<HistoricalQuote> extendToToDate = flatLineInterpolator.extendToToDate(interpolate, today);
			final List<HistoricalQuote> extendToFromDate = flatLineInterpolator.extendToFromDate(extendToToDate,
					fromDate);
			liveData.get().setHistory(extendToFromDate);
			return liveData;
		} catch (final Exception e) {
			log.warning(e.getMessage());
			return Optional.empty();
		}
	}

}
