package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class FTFeed  extends AbstractStockFeed {
    @Override
    public Optional<StockV1> get(Instrument instrument, int years, boolean addLatestQuoteToTheSeries) throws IOException {
        return Optional.empty();
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException {
        return Optional.empty();
    }

    @Override
    public Source getSource() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
