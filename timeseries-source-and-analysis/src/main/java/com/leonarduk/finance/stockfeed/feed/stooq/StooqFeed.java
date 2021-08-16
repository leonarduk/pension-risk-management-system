package com.leonarduk.finance.stockfeed.feed.stooq;

import com.github.kevinsawicki.http.HttpRequest;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.file.AbstractCsvStockFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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
        params.put(StooqFeed.PARAM_SYMBOL, getQueryName(Instrument.fromString(this.getSymbol())));
        if (this.getStartDate() != null) {
            params.put(StooqFeed.PARAM_START_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getStartDate()));
        }
        if (this.getEndDate() != null) {
            params.put(StooqFeed.PARAM_END_DATE,
                    AbstractCsvStockFeed.formatDate(StooqFeed.PARAM_FORMATTER, this.getEndDate()));
        }

        final HttpRequest request = this.createRequest(HttpRequest.append(StooqFeed.BASE_URL, params));
        if (!request.ok()) {
            throw new IOException("Bad response " + request.code());
        }

        final BufferedReader reader;
        try {
            reader = request.bufferedReader();
        } catch (final HttpRequest.HttpRequestException e) {
            throw e.getCause();
        }
        // Skip first line that contains column names
        reader.readLine();
        return reader;
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
