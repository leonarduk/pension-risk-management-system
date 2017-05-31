package com.leonarduk.finance.stockfeed.interpolation;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import com.leonarduk.finance.utils.TimeseriesUtils;

import yahoofinance.histquotes.HistoricalQuote;

public class BadDateRemover implements TimeSeriesCleaner {

	@Override
	public List<HistoricalQuote> clean(final List<HistoricalQuote> series) {
		final int thisYear = LocalDate.now().getYear();
		return TimeseriesUtils.sortQuoteList(
		        series.stream().filter((q) -> q.getDate().getYear() > 1970)
		                .filter((q) -> q.getDate().getYear() <= thisYear)
		                .collect(Collectors.toCollection(LinkedList::new)));
	}

}
