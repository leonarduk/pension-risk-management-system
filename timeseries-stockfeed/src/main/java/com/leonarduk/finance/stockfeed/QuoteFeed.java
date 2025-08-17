package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.ExtendedStockQuote;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;

public interface QuoteFeed {

    @NotNull ExtendedStockQuote getStockQuote(@NotNull Instrument instrument) throws IOException;

    boolean isAvailable();

}
