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

	public static void setRefresh(boolean refresh) {
		IntelligentStockFeed.refresh = refresh;
	}

	public Optional<Stock> get(Stock stock, int years) {
		return get(EXCHANGE.valueOf(stock.getStockExchange()), stock.getSymbol(), years);
	}

	public Optional<Stock> get(EXCHANGE exchange, String ticker, int years) {
		return get(Instrument.fromString(ticker), years);
	}

	public Optional<Stock> get(Instrument instrument, int years) {
		try {
			EXCHANGE exchange = EXCHANGE.London;
			String ticker = instrument.code();

			if (instrument.equals(Instrument.CASH)) {
				Stock cash = new Stock(instrument.getCode());
				List<HistoricalQuote> history = Lists.newArrayList();
				history.add(new ComparableHistoricalQuote(ticker, Calendar.getInstance(), BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
						BigDecimal.ONE, BigDecimal.ONE, 0l));
				cash.setHistory(history);
				StockQuote quote = new StockQuote(ticker);
				quote.setPrice(BigDecimal.ONE);
				cash.setQuote(quote);
				return Optional.of(cash);
			}
			CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory.getDataFeed(Source.MANUAL);

			Optional<Stock> cachedData = dataFeed.get(exchange, ticker, years);

			StockFeed feed = StockFeedFactory.getDataFeed(instrument.getSource());

			Optional<Stock> liveData = refresh ? feed.get(exchange, ticker, years) : Optional.empty();
			if (cachedData.isPresent()) {
				if (liveData.isPresent()) {
					mergeSeries(cachedData.get(), liveData.get().getHistory(), cachedData.get().getHistory());
					dataFeed.storeSeries(cachedData.get());
				}
				return Optional.of(cachedData.get());
			}
			if (liveData.isPresent())
				dataFeed.storeSeries(liveData.get());
			return liveData;
		} catch (Exception e) {
			log.warning(e.getMessage());
			return Optional.empty();
		}
	}

}
