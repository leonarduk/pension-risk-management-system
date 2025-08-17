package com.leonarduk.finance.timeseries;

import java.io.IOException;
import java.util.List;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Defines the contract for acquiring time‑series data for financial instruments.
 * Implementations may retrieve data from remote services or local stores.
 */
public interface TimeSeriesSource {
    /**
     * Retrieve the time‑series for the given symbol.
     *
     * @param symbol instrument identifier
     * @return list of historical quotes ordered by date
     * @throws IOException if the data cannot be retrieved
     */
    List<HistoricalQuote> getTimeSeries(String symbol) throws IOException;
}
