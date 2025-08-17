package com.leonarduk.finance.stockfeed;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
public class CachedStockFeed extends AbstractStockFeed {
    private final DataStore dataStore;

    public CachedStockFeed(final DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Bar> loadSeries(final StockV1 stock)
            throws IOException {
        boolean addLatestQuoteToTheSeries = false;
        final Optional<StockV1> optional = this.get(stock.getInstrument(), 1000, addLatestQuoteToTheSeries);
        if (optional.isPresent()) {
            return optional.get().getHistory();
        }
        return Lists.newArrayList();
    }

    private void mergeSeries(final StockV1 stock) throws IOException {
        final List<Bar> original = this.loadSeries(stock);
        this.mergeSeries(stock, original);
    }

    public void storeSeries(final StockV1 stock) throws IOException {
        if (this.dataStore.contains(stock)) {
            this.mergeSeries(stock);
        }
        this.dataStore.storeSeries(stock);
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, int years, boolean addLatestQuoteToTheSeries) throws IOException {
        return this.dataStore.get(instrument, years, addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException {
        return this.dataStore.get(instrument, fromDate, toDate, addLatestQuoteToTheSeries);
    }

    @Override
    public Source getSource() {
        return Source.CACHE;
    }


    @Override
    public boolean isAvailable() {
        return this.dataStore.isAvailable();
    }
}
