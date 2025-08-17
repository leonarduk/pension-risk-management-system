package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.StockFeed;

import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.leonarduk.finance.stockfeed.HistoricalDataService;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import java.util.Optional;
import java.util.LinkedHashMap;


/**
 * Helper class used by AWS components to retrieve historical data and render it as
 * HTML. Parameter parsing and record generation are delegated to
 * {@link HistoricalDataService}.
 */
@Slf4j
public class QueryRunner {
    public static final String INTERPOLATE = "interpolate";
    public static final String CLEAN_DATA = "cleanData";
    public static final String YEARS = "years";
    public static final String TICKER = "ticker";

    private final StockFeed stockFeed;
    private final HistoricalDataService historicalDataService;

    public QueryRunner() {
        this(DependencyFactory.stockFeed());
    }

    public QueryRunner(StockFeed stockFeed) {
        this.stockFeed = stockFeed;
        this.historicalDataService = new HistoricalDataService(stockFeed);
    }

    /**
     * Retrieves results for a given set of parameters.
     *
     * @param inputParams map of parameters – must include {@code ticker}
     * @return HTML representation of the historical data
     * @throws IOException if the underlying feed cannot be accessed
     */
    public String getResults(final Map<String, String> inputParams) throws IOException {
        if (inputParams == null) {
            throw new IllegalArgumentException("No parameters provided. Expect at least ticker");
        }
        log.debug("Input parameters: {}", inputParams);


      String fromDate = inputParams.get("fromDate");
        String toDate = inputParams.get("toDate");
        boolean interpolate = Boolean.parseBoolean(StringUtils.defaultIfEmpty(inputParams.get(QueryRunner.INTERPOLATE), "False"));
        boolean cleanData = Boolean.parseBoolean(StringUtils.defaultIfEmpty(inputParams.get(QueryRunner.CLEAN_DATA), "False"));

        String region = StringUtils.defaultIfEmpty(inputParams.get("region"), "L");
        String type = StringUtils.defaultIfEmpty(inputParams.get("type"), "UNKNOWN");
        String currency = inputParams.get("currency");

        if (ticker.contains(".")) {
            final String[] parts = ticker.split("\\.");
            ticker = parts[0];
            region = parts[1];
            if ("N".equalsIgnoreCase(region)) {
                region = "NY";
            }
        }

        if (ticker.contains("/")) {
            final String[] parts = ticker.split("/");
            ticker = parts[0];
            region = parts[1];

            if (2 < parts.length) {
                type = parts[2];
            }
            if (3 < parts.length) {
                currency = parts[3];
            }
        }

        if (StringUtils.isBlank(currency)) {
            currency = Instrument.resolveCurrency(inputParams.get(QueryRunner.TICKER));
        }
        Map<String, String> regionCurrencyMap = Map.of("NY", "USD", "L", "GBP");
        if ((StringUtils.isBlank(currency) || "UNKNOWN".equalsIgnoreCase(currency))
                && regionCurrencyMap.containsKey(region.toUpperCase())) {
            currency = regionCurrencyMap.get(region.toUpperCase());
        }
        Instrument instrument = Instrument.fromString(ticker, region, type, currency);

        final LocalDate toLocalDate;
        LocalDate fromLocalDate;

        if (!StringUtils.isEmpty(fromDate)) {
            fromLocalDate = LocalDate.parse(fromDate);
            if (StringUtils.isEmpty(toDate)) {
                toLocalDate = LocalDate.now();
            } else {
                toLocalDate = LocalDate.parse(toDate);
            }
        } else {
            toLocalDate = LocalDate.now();
            if (0 < days) {
                fromLocalDate = LocalDate.now().plusDays(-1 * days);
            } else if (0 < weeks) {
                fromLocalDate = LocalDate.now().plusWeeks(-1 * weeks);
            } else if (0 < months) {
                fromLocalDate = LocalDate.now().plusMonths(-1 * months);
            } else {
                fromLocalDate = LocalDate.now().plusYears(-1 * years);
            }
        }


        List<Map<String, Object>> results = this.generateResults(fromLocalDate, toLocalDate, interpolate, cleanData, instrument);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(results);
    }

    private List<Map<String, Object>> generateResults(LocalDate fromLocalDate, LocalDate toLocalDate,
                                                      boolean interpolate, boolean cleanData,
                                                      Instrument instrument)
            throws IOException {
        List<Map<String, Object>> records = new ArrayList<>();

        List<Bar> historyData;

        historyData = getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, false);

        for (Bar historicalQuote : historyData) {
            Map<String, Object> record = new LinkedHashMap<>();
            records.add(record);
            record.put("Date", historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString());
            record.put("Open", historicalQuote.getOpenPrice());
            record.put("High", historicalQuote.getHighPrice());
            record.put("Low", historicalQuote.getLowPrice());
            record.put("Close", historicalQuote.getClosePrice());
            record.put("Volume", historicalQuote.getVolume());

            if (historicalQuote instanceof final Commentable commentable) {
                record.put("Comment", commentable.getComment());

            }
        }

        return records;
    }
}
