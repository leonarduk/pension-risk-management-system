package com.leonarduk.finance.stockfeed.datatransformation.interpolation;

import com.leonarduk.finance.stockfeed.datatransformation.DataTransformer;
import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import java.io.IOException;
import java.util.List;

public interface TimeSeriesInterpolator extends DataTransformer {

    @Override
    default List<Bar> transform(List<Bar> history) throws IOException {
        return interpolate(history);
    }

    TimeSeries interpolate(TimeSeries series);

    List<Bar> interpolate(List<Bar> series) throws IOException;
}
