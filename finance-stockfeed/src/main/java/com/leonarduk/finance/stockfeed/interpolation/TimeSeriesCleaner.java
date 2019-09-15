package com.leonarduk.finance.stockfeed.interpolation;

import java.util.List;

import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;

public interface TimeSeriesCleaner {

	List<ExtendedHistoricalQuote> clean(List<ExtendedHistoricalQuote> series);

}
