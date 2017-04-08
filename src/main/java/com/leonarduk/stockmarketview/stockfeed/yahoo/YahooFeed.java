package com.leonarduk.stockmarketview.stockfeed.yahoo;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Optional;

import com.leonarduk.stockmarketview.stockfeed.StockFeed;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.Interval;

public class YahooFeed extends StockFeed {
	@Override
	public Optional<Stock> get(StockFeed.EXCHANGE exchange, String ticker) {
		try {
			Stock stock = YahooFinance.get(getQueryName(exchange, ticker));
			Calendar from = Calendar.getInstance();

			from.add(Calendar.YEAR, -20);

			stock.getHistory(from, HistQuotesRequest.DEFAULT_TO, Interval.DAILY);

			return Optional.of(stock);
		} catch (Exception e) {
			return Optional.empty();
		}
	}

	private String getQueryName(StockFeed.EXCHANGE exchange, String ticker) {
		switch (exchange) {
		case London:
			return ticker + ".L";
		}
		throw new IllegalArgumentException("Don't know how to handle " + exchange);
	}

	public static void main(String[] args) throws IOException {
		Stock stock = YahooFinance.get("PHGP.L");

		BigDecimal price = stock.getQuote().getPrice();
		BigDecimal change = stock.getQuote().getChangeInPercent();
		BigDecimal peg = stock.getStats().getPeg();
		BigDecimal dividend = stock.getDividend().getAnnualYieldPercent();

		stock.print();
		System.out.println(stock.getHistory());
	}

}
