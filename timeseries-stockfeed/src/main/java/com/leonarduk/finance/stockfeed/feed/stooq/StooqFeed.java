package com.leonarduk.finance.stockfeed.feed.stooq;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StooqFeed extends AbstractCsvStockFeed {
    public static final String BASE_URL = "https://stooq.com/q/d/l/";

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
            params.put(
                    StooqFeed.PARAM_START_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getStartDate()));
        }
        if (this.getEndDate() != null) {
            params.put(
                    StooqFeed.PARAM_END_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getEndDate()));
        }

        LocalDate today = LocalDate.now();
        if (!today.equals(LAST_REQUEST_DATE)) {
            resetDailyLimitCounter();
            LAST_REQUEST_DATE = today;
        }

        String url = buildUrl(params);
        if (CACHE.containsKey(url)) {
            return new BufferedReader(new StringReader(CACHE.get(url)));
        }

        if (REQUEST_COUNT.incrementAndGet() > DAILY_LIMIT) {
            throw new DailyLimitExceededException("Exceeded the daily request limit for Stooq");
        }

        final StooqHttpResponse response = this.createRequest(url);
        if (!response.ok()) {
            throw new IOException("Bad response " + response.code());
        }

        String body = response.body();
        if (body.contains("Exceeded the daily hits limit")) {
            throw new DailyLimitExceededException("Exceeded the daily hits limit for Stooq");
        }
        CACHE.put(url, body);
        return new BufferedReader(new StringReader(body));
    }

    protected StooqHttpResponse createRequest(final String uri) throws IOException {
        try {
            log.info("Request: {}", uri);
            HttpRequest request = HttpRequest.newBuilder(URI.create(uri)).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return new StooqHttpResponse(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while querying Stooq", e);
        }
    }

    private String buildUrl(Map<Object, Object> params) {
        String query = params.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
        return query.isBlank() ? BASE_URL : BASE_URL + "?" + query;
    }

    private String encode(Object value) {
        return URLEncoder.encode(String.valueOf(value), StandardCharsets.UTF_8);
    }

    protected record StooqHttpResponse(int code, String body) {
        boolean ok() {
            return code >= 200 && code < 300;
        }
    }
}
