package com.leonarduk.finance.stockfeed;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeed extends StockFeed {
	public static final Logger log = Logger.getLogger(IntelligentStockFeed.class.getName());

	private static boolean refresh = true;

	public static Optional<Stock> getFlatCashSeries(final Instrument instrument, final String ticker) {
		final Stock cash = new Stock(instrument.getCode());
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new ComparableHistoricalQuote(ticker, Calendar.getInstance(), BigDecimal.ONE, BigDecimal.ONE,
				BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, 0l));
		cash.setHistory(history);
		final StockQuote quote = new StockQuote(ticker);
		quote.setPrice(BigDecimal.ONE);
		cash.setQuote(quote);
		return Optional.of(cash);
	}

	public static void setRefresh(final boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	@Override
	public Optional<Stock> get(final Exchange exchange, final String ticker, final int years) {
		return this.get(Instrument.fromString(ticker), years);
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		try {
			final String ticker = instrument.code();

			if (instrument.equals(Instrument.CASH)) {
				return getFlatCashSeries(instrument, ticker);
			}
			final CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory.getDataFeed(Source.MANUAL);

			final Optional<Stock> cachedData = dataFeed.get(instrument.getExchange(), ticker, years);

			final StockFeed feed = StockFeedFactory.getDataFeed(instrument.getSource());

			Optional<Stock> liveData;
			try {
				liveData = refresh ? feed.get(instrument, years) : Optional.empty();
			} catch (final Throwable e) {
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

	@Override
	public Optional<Stock> get(final Stock stock, final int years) {
		return this.get(Exchange.valueOf(stock.getStockExchange()), stock.getSymbol(), years);
	}

}
