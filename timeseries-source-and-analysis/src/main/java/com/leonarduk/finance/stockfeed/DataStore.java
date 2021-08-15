package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public interface DataStore {

    void storeSeries(final StockV1 stock) throws IOException;

    boolean isAvailable();

    Optional<StockV1> get(Instrument instrument, int years, boolean addLatest) throws  IOException;

    Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatest)  throws  IOException;

    boolean contains(StockV1 stock) throws IOException;
}
