package com.leonarduk.stockmarketview.stockfeed;

import java.util.Optional;
import java.util.logging.Logger;

import yahoofinance.Stock;

public class IntelligentStockFeed extends StockFeed {
	public static final Logger log = Logger.getLogger(IntelligentStockFeed.class.getName());

	public Optional<Stock> get(Stock stock, int years) {
		return get(EXCHANGE.valueOf(stock.getStockExchange()), stock.getSymbol(), years);
	}

	public Optional<Stock> get(EXCHANGE exchange, String ticker, int years) {
		try {
			Instrument instrument = Instrument.fromString(ticker);

			CachedStockFeed dataFeed = (CachedStockFeed) StockFeedFactory.getDataFeed(Source.MANUAL);
			Optional<Stock> cachedData = dataFeed.get(exchange, ticker, years);

			StockFeed feed = StockFeedFactory.getDataFeed(Source.Yahoo);
			if (!instrument.equals(Instrument.UNKNOWN)) {
				feed = StockFeedFactory.getDataFeed(instrument.source());
			}

			Optional<Stock> liveData = feed.get(exchange, ticker, years);
			if (cachedData.isPresent()) {
				if (liveData.isPresent()) {
					mergeSeries(cachedData.get(), liveData.get().getHistory(), cachedData.get().getHistory());
					dataFeed.storeSeries(cachedData.get());
				}
				return Optional.of(cachedData.get());
			}
			dataFeed.storeSeries(liveData.get());
			return liveData;
		} catch (Exception e) {
			log.warning(e.getMessage());
			return Optional.empty();
		}
	}

	public Optional<Stock> get(Instrument instrument, int years) {
		return get(EXCHANGE.London, instrument.getCode(), years);
	}

}
