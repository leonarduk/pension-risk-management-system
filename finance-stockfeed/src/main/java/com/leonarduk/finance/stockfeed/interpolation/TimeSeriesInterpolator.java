package com.leonarduk.finance.stockfeed.interpolation;

import java.io.IOException;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

public interface TimeSeriesInterpolator {

	TimeSeries interpolate(TimeSeries series);

	List<Bar> interpolate(List<Bar> series) throws IOException;
}
