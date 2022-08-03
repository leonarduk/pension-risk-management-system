package com.leonarduk.finance.stockfeed.feed.yahoofinance;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.Interval;

import java.util.Calendar;

public class ExtendedHistQuotesRequest extends HistQuotesRequest {

    public ExtendedHistQuotesRequest(String symbol, Calendar from, Calendar to, Interval interval) {
        super(symbol, from, to, interval);
    }

}
