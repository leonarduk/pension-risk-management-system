package com.leonarduk.stockmarketview.stockfeed.yahoo;

import java.io.IOException;
import java.math.BigDecimal;

import com.leonarduk.stockmarketview.stockfeed.StockFeed;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

public class YahooFeed implements StockFeed {
	@Override
	public Stock get(StockFeed.EXCHANGE exchange, String ticker) throws IOException {
		return YahooFinance.get(getQueryName(exchange, ticker));
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
