package com.leonarduk.finance.stockfeed.feed.stooq;

import com.github.kevinsawicki.http.HttpRequest;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StooqFeed extends AbstractCsvStockFeed {
    public static final String BASE_URL = "https://stooq.com/q/d/l/";
    /**
     * The logger
     */
    public static final Logger log = LoggerFactory.getLogger(StooqFeed.class.getName());

    private static final String OUTPUT_DOWNLOAD = "d";

    private static final String PARAM_END_DATE = "d2";

    private static final String PARAM_SYMBOL = "s";

    private static final DateFormat PARAM_FORMATTER = new SimpleDateFormat("yyyyMMdd");

    private static final String PARAM_INTERFACE = "i";

    private static final String PARAM_START_DATE = "d1";

    private static final Map<String, String> CACHE = new ConcurrentHashMap<>();
    private static final AtomicInteger REQUEST_COUNT = new AtomicInteger();
    private static LocalDate LAST_REQUEST_DATE = LocalDate.now();
    private static int DAILY_LIMIT = 100;

    public static void setDailyLimit(int limit) {
        DAILY_LIMIT = limit;
    }

    public static void resetDailyLimitCounter() {
        REQUEST_COUNT.set(0);
        CACHE.clear();
        LAST_REQUEST_DATE = LocalDate.now();
    }

    @Override
    public Source getSource() {
        return Source.STOOQ;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected String getQueryName(Instrument instrument) {
        return instrument.code() + instrument.getExchange().getStooqSuffix();
    }

    @Override
    protected BufferedReader openReader() throws IOException {

        final Map<Object, Object> params = new HashMap<>(4, 1);
        params.put(StooqFeed.PARAM_INTERFACE, StooqFeed.OUTPUT_DOWNLOAD);
        params.put(StooqFeed.PARAM_SYMBOL, getQueryName(this.getInstrument()));
        if (this.getStartDate() != null) {
            params.put(StooqFeed.PARAM_START_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getStartDate()));
        }
        if (this.getEndDate() != null) {
            params.put(StooqFeed.PARAM_END_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getEndDate()));
        }

        LocalDate today = LocalDate.now();
        if (!today.equals(LAST_REQUEST_DATE)) {
            resetDailyLimitCounter();
            LAST_REQUEST_DATE = today;
        }

        String url = HttpRequest.append(StooqFeed.BASE_URL, params);
        if (CACHE.containsKey(url)) {
            return new BufferedReader(new StringReader(CACHE.get(url)));
        }

        if (REQUEST_COUNT.incrementAndGet() > DAILY_LIMIT) {
            throw new DailyLimitExceededException("Exceeded the daily request limit for Stooq");
        }

        final HttpRequest request = this.createRequest(url);
        if (!request.ok()) {
            throw new IOException("Bad response " + request.code());
        }

        String body;
        try {
            body = request.body();
        } catch (final HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
        if (body.contains("Exceeded the daily hits limit")) {
            throw new DailyLimitExceededException("Exceeded the daily hits limit for Stooq");
        }
        CACHE.put(url, body);
        return new BufferedReader(new StringReader(body));
    }

    protected HttpRequest createRequest(final CharSequence uri) throws IOException {
        try {
            StooqFeed.log.info("Request: " + uri);
            return HttpRequest.get(uri);
        } catch (final HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
    }
}
