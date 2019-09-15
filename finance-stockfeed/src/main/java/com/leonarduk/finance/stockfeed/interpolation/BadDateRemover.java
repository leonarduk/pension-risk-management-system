package com.leonarduk.finance.stockfeed.interpolation;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.TimeseriesUtils;

public class BadDateRemover implements TimeSeriesCleaner {

	@Override
	public List<ExtendedHistoricalQuote> clean(final List<ExtendedHistoricalQuote> history) {
		final int thisYear = LocalDate.now().getYear();
		return TimeseriesUtils.sortQuoteList(
		        history.stream().filter((q) -> q.getLocaldate().getYear() > 1970)
		                .filter((q) -> q.getLocaldate().getYear() <= thisYear)
		                .collect(Collectors.toCollection(LinkedList::new)));
	}

}
