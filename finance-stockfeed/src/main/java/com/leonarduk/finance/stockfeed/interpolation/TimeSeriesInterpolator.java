package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.util.List;

import org.ta4j.core.TimeSeries;

import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;

public interface TimeSeriesInterpolator {

	List<ExtendedHistoricalQuote> interpolate(List<ExtendedHistoricalQuote> series) throws IOException;

	TimeSeries interpolate(TimeSeries series);
}
