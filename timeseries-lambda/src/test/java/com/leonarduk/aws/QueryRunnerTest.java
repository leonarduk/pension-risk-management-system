package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

class QueryRunnerTest {

    static class RecordingStockFeed implements StockFeed {
        Instrument lastInstrument;

        @Override
        public Optional<StockV1> get(Instrument fromString, int i, boolean addLatestQuoteToTheSeries) {
            this.lastInstrument = fromString;
            return Optional.empty();
        }

        @Override
        public Optional<StockV1> get(Instrument instrument, int years, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) {
            this.lastInstrument = instrument;
            return Optional.empty();
        }

        @Override
        public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) {
            this.lastInstrument = instrument;
            return Optional.empty();
        }

        @Override
        public Optional<StockV1> get(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) {
            this.lastInstrument = instrument;
            return Optional.empty();
        }

        @Override
        public Object getSource() {
            return null;
        }

        @Override
        public boolean isAvailable() {
            return true;
        }
    }

    @Test
    void providedCurrencyParameterIsRespected() throws Exception {
        QueryRunner runner = new QueryRunner();
        RecordingStockFeed feed = new RecordingStockFeed();
        Field field = QueryRunner.class.getDeclaredField("stockFeed");
        field.setAccessible(true);
        field.set(runner, feed);

        Map<String, String> params = Map.of(
                QueryRunner.TICKER, "TEST",
                "currency", "USD"
        );

        runner.getResults(params);

        Assertions.assertEquals("USD", feed.lastInstrument.getCurrency());
    }
}
