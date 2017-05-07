package com.leonarduk.finance.stockfeed.interpolation;

import java.util.List;

import eu.verdelhan.ta4j.TimeSeries;
import yahoofinance.histquotes.HistoricalQuote;

public interface TimeSeriesInterpolator {

	List<HistoricalQuote> interpolate(List<HistoricalQuote> series);

	TimeSeries interpolate(TimeSeries series);
}
