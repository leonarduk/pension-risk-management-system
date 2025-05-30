package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public interface StockFeed {
    enum Exchange {
        L(".L", ".UK"),
        LONDON(".L", ".UK"),
        NY("", ".US"),
        NYQ("", ".US"),
        NA("", ".US");

        private final String yahooSuffix;
        private final String stooqSuffix;

        public String getYahooSuffix() {
            return yahooSuffix;
        }

        Exchange(String yahooSuffix, String stooqSuffix) {
            this.yahooSuffix = yahooSuffix;
            this.stooqSuffix = stooqSuffix;
        }


        public String getStooqSuffix() {
            return stooqSuffix;
        }
    }

    Optional<StockV1> get(Instrument fromString, int i, boolean addLatestQuoteToTheSeries) throws IOException;

    Optional<StockV1> get(Instrument instrument, int years, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException;

    Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException;

    Optional<StockV1> get(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate, boolean interpolate,
                          boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException;

    Object getSource();

    boolean isAvailable();

}
