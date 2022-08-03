package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.ExtendedStockQuote;

import java.io.IOException;

public interface QuoteFeed {

    ExtendedStockQuote getStockQuote(Instrument instrument) throws IOException;

    boolean isAvailable();

}
