package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.stockfeed.datatransformation.DataTransformer;
import org.ta4j.core.Bar;

import java.util.List;

public interface TimeSeriesCleaner extends DataTransformer {

    @Override
    default List<Bar> transform(List<Bar> history) {
        return clean(history);

    }

    List<Bar> clean(List<Bar> history);

}
