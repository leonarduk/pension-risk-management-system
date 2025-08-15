package com.leonarduk.finance.stockfeed.feed.stooq;

import com.github.kevinsawicki.http.HttpRequest;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.DataStore;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.StockFeedFactory;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.CachedStockFeed;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;

public class StooqFeedLimiterTest {

    @Before
    public void setup() {
        StooqFeed.setDailyLimit(2);
        StooqFeed.resetDailyLimitCounter();
    }

    @Test(expected = DailyLimitExceededException.class)
    public void testLimitExceeded() throws IOException {
        TestStooqFeed feed = new TestStooqFeed();
        feed.get(Instrument.fromString("AAA"), LocalDate.now().minusDays(1), LocalDate.now(), false);
        feed.get(Instrument.fromString("BBB"), LocalDate.now().minusDays(1), LocalDate.now(), false);
        feed.get(Instrument.fromString("CCC"), LocalDate.now().minusDays(1), LocalDate.now(), false);
    }

    @Test(expected = DailyLimitExceededException.class)
    public void testIntelligentFeedPropagatesError() throws Exception {
        StooqFeed.setDailyLimit(0);
        StooqFeed.resetDailyLimitCounter();
        DummyDataStore ds = new DummyDataStore();
        IntelligentStockFeed feed = new IntelligentStockFeed(ds);
        TestStooqFeed stooq = new TestStooqFeed();
        TestStockFeedFactory factory = new TestStockFeedFactory(ds, stooq);
        java.lang.reflect.Field field = IntelligentStockFeed.class.getDeclaredField("stockFeedFactory");
        field.setAccessible(true);
        field.set(feed, factory);
        feed.get(Instrument.fromString("AAA"), 1, false, false, false);
    }

    private static class TestStooqFeed extends StooqFeed {
        @Override
        protected HttpRequest createRequest(CharSequence uri) {
            try {
                return new DummyHttpRequest();
            } catch (HttpRequest.HttpRequestException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class DummyHttpRequest extends HttpRequest {
        protected DummyHttpRequest() throws HttpRequest.HttpRequestException {
            super("http://localhost", "GET");
        }

        @Override
        public boolean ok() {
            return true;
        }

        @Override
        public int code() {
            return 200;
        }

        @Override
        public String body() {
            return "date,open,high,low,close,volume\n2020-01-01,1,1,1,1,100\n";
        }
    }

    private static class DummyDataStore implements DataStore {
        @Override
        public void storeSeries(StockV1 stock) {}

        @Override
        public boolean isAvailable() { return false; }

        @Override
        public java.util.Optional<StockV1> get(Instrument instrument, int years, boolean addLatest) { return java.util.Optional.empty(); }

        @Override
        public java.util.Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatest) { return java.util.Optional.empty(); }

        @Override
        public boolean contains(StockV1 stock) { return false; }
    }

    private static class StubStockFeed implements StockFeed {
        private final Source source;
        StubStockFeed(Source source) { this.source = source; }
        @Override
        public java.util.Optional<StockV1> get(Instrument fromString, int i, boolean addLatestQuoteToTheSeries) throws IOException { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<StockV1> get(Instrument instrument, int years, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException { return java.util.Optional.empty(); }
        @Override
        public java.util.Optional<StockV1> get(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate, boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException { return java.util.Optional.empty(); }
        @Override
        public Source getSource() { return source; }
        @Override
        public boolean isAvailable() { return false; }
    }

    private static class TestStockFeedFactory extends StockFeedFactory {
        private final DataStore ds;
        private final StockFeed stooq;
        TestStockFeedFactory(DataStore ds, StockFeed stooq) {
            super(ds);
            this.ds = ds;
            this.stooq = stooq;
        }
        @Override
        public StockFeed getDataFeed(final Source source) {
            switch (source) {
                case MANUAL:
                    return new CachedStockFeed(ds);
                case STOOQ:
                    return stooq;
                default:
                    return new StubStockFeed(source);
            }
        }
    }
}
