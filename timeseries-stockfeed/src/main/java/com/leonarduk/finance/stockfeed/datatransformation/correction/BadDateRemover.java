package com.leonarduk.finance.stockfeed.datatransformation.correction;

import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class BadDateRemover implements TimeSeriesCleaner {

    @Override
    public List<Bar> clean(final List<Bar> history) {
        final int thisYear = LocalDate.now().getYear();
        return TimeseriesUtils.sortQuoteList(
                history.stream().filter((q) -> q.getEndTime().atZone(ZoneId.systemDefault()).getYear() > 1970)
                        .filter((q) -> q.getEndTime().atZone(ZoneId.systemDefault()).getYear() <= thisYear)
                        .collect(Collectors.toCollection(LinkedList::new)));
    }

}
