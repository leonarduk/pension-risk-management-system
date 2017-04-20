package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

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

	public void mergeSeries(Stock stock, List<HistoricalQuote> original) throws IOException {
		List<HistoricalQuote> newSeries = stock.getHistory();
		mergeSeries(stock, original, newSeries);
	}

	public void mergeSeries(Stock stock, List<HistoricalQuote> original, List<HistoricalQuote> newSeries) {
		Map<Calendar, HistoricalQuote> dates = original.stream()
				.collect(Collectors.toMap(HistoricalQuote::getDate, Function.identity()));
		Map<Calendar, HistoricalQuote> newdates = newSeries.stream()
				.collect(Collectors.toMap(HistoricalQuote::getDate, Function.identity()));
		dates.keySet().removeAll(newdates.keySet());
		newdates.putAll(dates);

		List<HistoricalQuote> sortedList = new LinkedList<>(newdates.values());
		sortedList.sort((quote1, quote2) -> quote1.getDate().compareTo(quote2.getDate()));
		stock.setHistory(sortedList);
		
	}

	public static Stock createStock(EXCHANGE exchange, String ticker, String name, List<HistoricalQuote> quotes) {
		Stock stock = new Stock(ticker);

		stock.setName(name);
		// stock.setCurrency();
		stock.setStockExchange(exchange.name());
		StockQuote quote = new StockQuote(ticker);

		if (!quotes.isEmpty()) {
			HistoricalQuote historicalQuote = quotes.get(quotes.size() - 1);

			quote.setDayHigh(historicalQuote.getHigh());
			quote.setDayLow(historicalQuote.getLow());
			quote.setOpen(historicalQuote.getOpen());
			quote.setAvgVolume(historicalQuote.getVolume());
			quote.setPrice(historicalQuote.getClose());
			stock.setQuote(quote);
			// stock.setQuote(this.getQuote());
			// stock.setStats(this.getStats());
			// stock.setDividend(this.getDividend())
			stock.setHistory(quotes);
		}
		return stock;
	}

}
