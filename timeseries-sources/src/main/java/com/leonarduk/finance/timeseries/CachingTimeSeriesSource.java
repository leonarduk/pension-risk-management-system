package com.leonarduk.finance.timeseries;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import yahoofinance.histquotes.HistoricalQuote;

/**
 * Decorator that caches results from another {@link TimeSeriesSource} in memory
 * using Guava's {@link LoadingCache}.
 */
public class CachingTimeSeriesSource implements TimeSeriesSource {

    private final TimeSeriesSource delegate;
    private final LoadingCache<String, List<HistoricalQuote>> cache;

    /**
     * Create a caching layer around the provided {@code delegate}.
     *
     * @param delegate source used to load data on cache miss
     */
    public CachingTimeSeriesSource(TimeSeriesSource delegate) {
        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder().build(new CacheLoader<String, List<HistoricalQuote>>() {
            @Override
            public List<HistoricalQuote> load(String key) throws Exception {
                return delegate.getTimeSeries(key);
            }
        });
    }

    @Override
    public List<HistoricalQuote> getTimeSeries(String symbol) throws IOException {
        try {
            return cache.get(symbol);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException io) {
                throw io;
            }
            throw new IOException("Failed to load time series for " + symbol, cause);
        }
    }
}
