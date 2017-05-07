package com.leonarduk.finance.stockfeed.interpolation;

import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import yahoofinance.histquotes.HistoricalQuote;

public class BadDateRemover implements TimeSeriesCleaner {

	@Override
	public List<HistoricalQuote> clean(final List<HistoricalQuote> series) {
		final int thisYear = LocalDate.now().getYear();
		return series.stream().filter((q) -> q.getDate().getYear() > 1970)
				.filter((q) -> q.getDate().getYear() < thisYear).collect(Collectors.toList());
	}

}
