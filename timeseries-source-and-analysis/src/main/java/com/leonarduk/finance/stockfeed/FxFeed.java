package com.leonarduk.finance.stockfeed;

import java.time.LocalDate;
import java.util.List;

import org.ta4j.core.Bar;

public interface FxFeed {

	public List<Bar> getFxSeries(final String currencyOne, final String currencyTwo, final LocalDate fromDate,
			final LocalDate toDate);
}
