package com.leonarduk.finance.stockfeed;

import java.io.IOException;

import yahoofinance.quotes.stock.StockQuote;

public interface QuoteFeed {

	StockQuote getStockQuote(Instrument instrument) throws IOException;

	boolean isAvailable();

}
