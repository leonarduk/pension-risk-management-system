package com.leonarduk.finance.stockfeed;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeed extends StockFeed {
	public static final Logger log = Logger.getLogger(IntelligentStockFeed.class.getName());

	private static boolean refresh = true;

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument) {
		final Stock cash = new Stock(instrument);
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new HistoricalQuote(instrument, Calendar.getInstance(), BigDecimal.ONE, BigDecimal.ONE,
				BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0l));
		cash.setHistory(history);
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
				return getFlatCashSeries(instrument);
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
			if (cachedData.isPresent()) {
				if (liveData.isPresent()) {
					this.mergeSeries(cachedData.get(), cachedData.get().getHistory(), liveData.get().getHistory());
					dataFeed.storeSeries(cachedData.get());
				}
				return Optional.of(cachedData.get());
			}
			if (liveData.isPresent()) {
				dataFeed.storeSeries(liveData.get());
			}
			return liveData;
		} catch (final Exception e) {
			log.warning(e.getMessage());
			return Optional.empty();
		}
	}

}
