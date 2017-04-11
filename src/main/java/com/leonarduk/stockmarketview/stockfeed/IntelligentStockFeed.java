package com.leonarduk.stockmarketview.stockfeed;

import java.util.Optional;

import yahoofinance.Stock;

public class IntelligentStockFeed extends StockFeed {

	public Optional<Stock> get(Stock stock, int years) {
		return get(EXCHANGE.valueOf(stock.getStockExchange()), stock.getSymbol(), years);
	}

	public Optional<Stock> get(EXCHANGE exchange, String ticker, int years) {
		try {
			Optional<Instrument> instrument = Instrument.fromString(ticker);
			StockFeed feed = StockFeedFactory.getDataFeed(Source.Yahoo);
			if (instrument.isPresent()) {
				feed = StockFeedFactory.getDataFeed(instrument.get().source());
			}

			return feed.get(exchange, ticker, years);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

}
