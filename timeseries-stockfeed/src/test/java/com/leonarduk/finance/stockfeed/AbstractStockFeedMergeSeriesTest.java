package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Test;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractStockFeedMergeSeriesTest {

    @Test
    void mergeSeriesHandlesDuplicateDates() {
        AbstractStockFeed feed = new AbstractStockFeed() {
            @Override
            public Optional<StockV1> get(
                    Instrument instrument, int years, boolean addLatestQuoteToTheSeries) {
                return Optional.empty();
            }

            @Override
            public Optional<StockV1> get(
                    Instrument instrument,
                    LocalDate fromDate,
                    LocalDate toDate,
                    boolean addLatestQuoteToTheSeries) {
                return Optional.empty();
            }

            @Override
            public Source getSource() {
                return Source.MANUAL;
            }

            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        List<Bar> original =
                Arrays.asList(
                        new ExtendedHistoricalQuote(
                                Instrument.CASH,
                                LocalDate.parse("2023-01-01"),
                                100,
                                100,
                                100,
                                100,
                                100,
                                0,
                                ""),
                        new ExtendedHistoricalQuote(
                                Instrument.CASH,
                                LocalDate.parse("2023-01-01"),
                                101,
                                101,
                                101,
                                101,
                                101,
                                0,
                                ""),
                        new ExtendedHistoricalQuote(
                                Instrument.CASH,
                                LocalDate.parse("2023-01-02"),
                                102,
                                102,
                                102,
                                102,
                                102,
                                0,
                                ""));
        List<Bar> newSeries =
                Arrays.asList(
                        new ExtendedHistoricalQuote(
                                Instrument.CASH,
                                LocalDate.parse("2023-01-03"),
                                103,
                                103,
                                103,
                                103,
                                103,
                                0,
                                ""));

        StockV1 stock = AbstractStockFeed.createStock(Instrument.CASH, original);
        feed.mergeSeries(stock, original, newSeries);

        List<Bar> history = stock.getHistory();
        assertEquals(3, history.size());
        assertEquals(100.0, history.get(0).getClosePrice().doubleValue());
    }
}

