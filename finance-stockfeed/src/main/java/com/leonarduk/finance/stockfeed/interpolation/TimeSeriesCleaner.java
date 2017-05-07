package com.leonarduk.finance.stockfeed.interpolation;

import java.util.List;

import yahoofinance.histquotes.HistoricalQuote;

public interface TimeSeriesCleaner {

	List<HistoricalQuote> clean(List<HistoricalQuote> series);

}
