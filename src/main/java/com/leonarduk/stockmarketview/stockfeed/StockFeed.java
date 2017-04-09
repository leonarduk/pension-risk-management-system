package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public abstract class StockFeed {
	public enum EXCHANGE {
		London
	}

	/**
	 * Bit of a hack - supply one Stock with minimal details in it to get new
	 * instance fully populated
	 * 
	 * @param stock
	 * @return
	 * @throws IOException
	 */
	public Optional<Stock> get(Stock stock, int years) {
		try {
			return get(EXCHANGE.valueOf(stock.getStockExchange()), stock.getSymbol(), years);
		} catch (IOException e) {
			return Optional.empty();
		}
	}

	public abstract Optional<Stock> get(EXCHANGE exchange, String ticker, int years) throws IOException;

	public static Stock createStock(EXCHANGE exchange, String ticker, String name) {
		return createStock(exchange, ticker, name, null);
	}

	public static Stock createStock(EXCHANGE exchange, String ticker, String name, List<HistoricalQuote> quotes) {
		Stock stock = new Stock(ticker);

		stock.setName(name);
		// stock.setCurrency();
		stock.setStockExchange(exchange.name());

		// stock.setQuote(this.getQuote());
		// stock.setStats(this.getStats());
		// stock.setDividend(this.getDividend())
		stock.setHistory(quotes);
		return stock;
	}

}
