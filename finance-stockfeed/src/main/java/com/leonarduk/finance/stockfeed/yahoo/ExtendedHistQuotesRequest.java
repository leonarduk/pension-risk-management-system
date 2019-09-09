package com.leonarduk.finance.stockfeed.yahoo;

import java.util.Calendar;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.Interval;

public class ExtendedHistQuotesRequest extends HistQuotesRequest {

	public ExtendedHistQuotesRequest(String symbol, Calendar from, Calendar to, Interval interval) {
		super(symbol, from, to, interval);
	}

}
