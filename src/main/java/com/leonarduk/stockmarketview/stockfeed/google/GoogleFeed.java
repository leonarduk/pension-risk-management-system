package com.leonarduk.stockmarketview.stockfeed.google;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class GoogleFeed implements StockFeed {

	public static void main(String[] args) throws IOException {
		GoogleQuoteRequest request = new GoogleQuoteRequest();
		request.setSymbol("LON:IMV");
		request.setStartDate(DateUtils.yearStart());
		request.setEndDate(DateUtils.yearEnd());

		List<HistoricalQuote> quotes = new LinkedList<>();
		while (request.next()) {
			quotes.add(request.asHistoricalQuote());
		}

	}

	@Override
	public Stock get(EXCHANGE exchange, String ticker) throws IOException {
		GoogleQuoteRequest request = new GoogleQuoteRequest();
		request.setSymbol(getQueryName(exchange, ticker));
		request.setStartDate(DateUtils.yearStart());
		request.setEndDate(DateUtils.yearEnd());

		List<HistoricalQuote> quotes = new LinkedList<>();
		while (request.next()) {
			quotes.add(request.asHistoricalQuote());
		}

		Stock stock = new Stock(ticker);

		stock.setName(ticker);
		// stock.setCurrency();
		stock.setStockExchange(exchange.name());

		// stock.setQuote(this.getQuote());
		// stock.setStats(this.getStats());
		// stock.setDividend(this.getDividend())
		stock.setHistory(quotes);
		return stock;
	}

	private String getQueryName(StockFeed.EXCHANGE exchange, String ticker) {
		switch (exchange) {
		case London:
			return "LON:" + ticker;
		}
		throw new IllegalArgumentException("Don't know how to handle " + exchange);
	}
}
