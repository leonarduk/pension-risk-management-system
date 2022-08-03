package com.leonarduk.finance.stockfeed;

import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.List;

public interface FxFeed {

    List<Bar> getFxSeries(final String currencyOne, final String currencyTwo, final LocalDate fromDate,
                          final LocalDate toDate);
}
