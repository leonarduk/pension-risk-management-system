package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class NullValueRemover implements TimeSeriesCleaner {

    @Override
    public List<Bar> clean(final List<Bar> history) {
        final int thisYear = LocalDate.now().getYear();
        return TimeseriesUtils.sortQuoteList(
                history.stream().filter((q) -> !q.getClosePrice().isZero())
                        .collect(Collectors.toCollection(LinkedList::new)));
    }

}
