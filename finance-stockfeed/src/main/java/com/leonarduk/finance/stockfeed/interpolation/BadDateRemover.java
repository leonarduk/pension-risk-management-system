package com.leonarduk.finance.stockfeed.interpolation;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.ta4j.core.Bar;

import com.leonarduk.finance.utils.TimeseriesUtils;

public class BadDateRemover implements TimeSeriesCleaner {

	@Override
	public List<Bar> clean(final List<Bar> history) {
		final int thisYear = LocalDate.now().getYear();
		return TimeseriesUtils.sortQuoteList(
		        history.stream().filter((q) -> q.getEndTime().getYear() > 1970)
		                .filter((q) -> q.getEndTime().getYear() <= thisYear)
		                .collect(Collectors.toCollection(LinkedList::new)));
	}

}
