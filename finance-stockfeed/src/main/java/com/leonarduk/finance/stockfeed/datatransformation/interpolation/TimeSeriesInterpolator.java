package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import java.io.IOException;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import com.leonarduk.finance.stockfeed.datatransformation.DataTransformer;

public interface TimeSeriesInterpolator extends DataTransformer {

	@Override
	default List<Bar> transform(List<Bar> history) throws IOException {
		return interpolate(history);
	}

	TimeSeries interpolate(TimeSeries series);

	List<Bar> interpolate(List<Bar> series) throws IOException;
}
