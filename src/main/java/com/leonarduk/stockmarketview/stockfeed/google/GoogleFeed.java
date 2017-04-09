package com.leonarduk.stockmarketview.stockfeed.google;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.leonarduk.stockmarketview.stockfeed.StockFeed;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class GoogleFeed extends StockFeed {

	@Override
	public Optional<Stock> get(EXCHANGE exchange, String ticker, int years) {
		GoogleQuoteRequest request = new GoogleQuoteRequest();
		request.setSymbol(getQueryName(exchange, ticker));
		Calendar from = Calendar.getInstance();
		from.add(Calendar.YEAR, -1 * years);
		request.setStartDate(from);
		request.setEndDate(Calendar.getInstance());

		List<HistoricalQuote> quotes = new LinkedList<>();
		try {
			while (request.next()) {
				quotes.add(request.asHistoricalQuote());
			}
		} catch (IOException e) {
			return Optional.empty();
		}

		return Optional.of(createStock(exchange, ticker, ticker, quotes));
	}

	private String getQueryName(StockFeed.EXCHANGE exchange, String ticker) {
		switch (exchange) {
		case London:
			return "LON:" + ticker;
		}
		throw new IllegalArgumentException("Don't know how to handle " + exchange);
	}
}
