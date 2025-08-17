package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public interface StockFeed {
    enum Exchange {
        L(".L", ".UK"),
        LONDON(".L", ".UK"),
        NY("", ".US"),
        NYQ("", ".US"),
        NA("", ".US");

        private final String yahooSuffix;
        private final String stooqSuffix;

        public @NotNull String getYahooSuffix() {
            return yahooSuffix;
        }

        Exchange(String yahooSuffix, String stooqSuffix) {
            this.yahooSuffix = yahooSuffix;
            this.stooqSuffix = stooqSuffix;
        }


        public @NotNull String getStooqSuffix() {
            return stooqSuffix;
        }
    }

    @NotNull Optional<StockV1> get(@NotNull Instrument fromString, int i, boolean addLatestQuoteToTheSeries) throws IOException;

    @NotNull Optional<StockV1> get(@NotNull Instrument instrument, int years, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException;

    @NotNull Optional<StockV1> get(@NotNull Instrument instrument, @NotNull LocalDate fromDate, @NotNull LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException;

    @NotNull Optional<StockV1> get(@NotNull Instrument instrument, @NotNull LocalDate fromLocalDate, @NotNull LocalDate toLocalDate, boolean interpolate,
                          boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException;

    @NotNull Object getSource();

    boolean isAvailable();

}
