package com.leonarduk.finance.stockfeed;

import java.io.IOException;

import com.leonarduk.finance.stockfeed.yahoo.ExtendedStockQuote;

public interface QuoteFeed {

	ExtendedStockQuote getStockQuote(Instrument instrument) throws IOException;

	boolean isAvailable();

}
