package com.leonarduk.finance.stockfeed.interpolation;

import eu.verdelhan.ta4j.TimeSeries;

public interface TimeSeriesInterpolator {

	TimeSeries interpolate(TimeSeries series);
}
