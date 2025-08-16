package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class QueryRunner {
    public static final String INTERPOLATE = "interpolate";
    public static final String CLEAN_DATA = "cleanData";
    public static final String YEARS = "years";
    public static final String TICKER = "ticker";
    private final StockFeed stockFeed;

    public QueryRunner() {
        stockFeed = DependencyFactory.stockFeed();

    }

    /**
     * Run this class as a command line application to test the functionality
     * of this class.
     * <p>
     * This method is not intended to be called from outside this class.
     * @param args command line args, not used
     * @throws IOException if an IO error occurs
     */
    public static void main(final String[] args) throws IOException {
        QueryRunner.log.info(new QueryRunner().getResults(Map.of(
                QueryRunner.TICKER, "PHGP.L",
                QueryRunner.YEARS, "1",
                "interpolate", "true",
                QueryRunner.CLEAN_DATA, "true"
        )));
    }

    /**
     * Retrieves results for a given financial instrument based on the input parameters.
     *
     * @param inputParams a map containing the parameters such as ticker, years, months, weeks, days,
     *                    fromDate, toDate, interpolate, cleanData, region, type, and currency.
     *                    <strong>The map must include the {@code ticker} key.</strong>
     * @return a String representing the generated results in HTML format.
     * @throws IOException if an IO error occurs during the retrieval of results.
     * @throws IllegalArgumentException if the {@code inputParams} map is {@code null} or missing the required
     *                                  {@code ticker} key.
     */
    public String getResults(final Map<String, String> inputParams) throws IOException {

        if (null == inputParams)
        {
            throw new IllegalArgumentException("No parameters provided. Expect at least ticker");
        }
        QueryRunner.log.debug("Input parameters: {}", inputParams);

        if (!inputParams.containsKey(QueryRunner.TICKER) || StringUtils.isBlank(inputParams.get(QueryRunner.TICKER))) {
            throw new IllegalArgumentException("Ticker parameter is required");
        }


        String ticker = inputParams.get(QueryRunner.TICKER);

        int years = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get(QueryRunner.YEARS), "10"));
        int months = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("months"), "0"));
        int weeks = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("weeks"), "0"));
        int days = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("days"), "0"));

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


        return this.generateResults(fromLocalDate, toLocalDate, interpolate, cleanData, instrument);
    }

    private String generateResults(LocalDate fromLocalDate, LocalDate toLocalDate,
                                   boolean interpolate, boolean cleanData,
                                   Instrument instrument)
            throws IOException {
        StringBuilder sbBody = new StringBuilder();
        List<List<DataField>> records = new ArrayList<>();

        List<Bar> historyData;

        historyData = getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, false);

        for (Bar historicalQuote : historyData) {
            ArrayList<DataField> record = new ArrayList<>();
            records.add(record);
            record.add(new DataField("Date", historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString()));
            record.add(new DataField("Open", historicalQuote.getOpenPrice()));
            record.add(new DataField("High", historicalQuote.getHighPrice()));
            record.add(new DataField("Low", historicalQuote.getLowPrice()));
            record.add(new DataField("Close", historicalQuote.getClosePrice()));
            record.add(new DataField("Volume", historicalQuote.getVolume()));

            if (historicalQuote instanceof final Commentable commentable) {
                record.add(new DataField("Comment", commentable.getComment()));

            }
        }

        HtmlTools.printTable(sbBody, records);
        return HtmlTools.createHtmlText(null, sbBody).toString();
    }

    private List<Bar> getHistoryData(final Instrument instrument, final LocalDate fromLocalDate, final LocalDate toLocalDate,
                                     final boolean interpolate, final boolean cleanData, final boolean addLatestQuoteToTheSeries) throws IOException {
        Optional<StockV1> stock = stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate,
                cleanData, addLatestQuoteToTheSeries);
        if (stock.isPresent()) {
            return stock.get().getHistory();
        }
        return new ArrayList<>();
    }

}
