package com.leonarduk.aws;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import com.leonarduk.finance.utils.HtmlTools;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QueryRunner {
    public static final String INTERPOLATE = "interpolate";
    public static final String CLEAN_DATA = "cleanData";
    public static final String YEARS = "years";
    public static final String TICKER = "ticker";
    private final StockFeed stockFeed;

    public QueryRunner() {
        this.stockFeed = DependencyFactory.stockFeed();

    }

    public static void main(String[] args) throws IOException {
        System.out.println(new QueryRunner().getResults(ImmutableMap.of(
                TICKER, "PHGP.L",
                YEARS, "1",
                "interpolate", "true",
                CLEAN_DATA, "true"
        )));
    }

    public String getResults(Map<String, String> inputParams) throws IOException {

        if (inputParams == null)
        {
            throw new IllegalArgumentException("No parameters provided. Expect at least ticker");
        }
        System.out.println(inputParams);


        String ticker = inputParams.get(TICKER);

        final int years = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get(YEARS), "10"));
        final int months = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("months"), "0"));
        final int weeks = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("weeks"), "0"));
        final int days = Integer.parseInt(StringUtils.defaultIfEmpty(inputParams.get("days"), "0"));

        final String fromDate = inputParams.get("fromDate");
        final String toDate = inputParams.get("toDate");
        final boolean interpolate = Boolean.parseBoolean(StringUtils.defaultIfEmpty(inputParams.get(INTERPOLATE), "False"));
        final boolean cleanData = Boolean.parseBoolean(StringUtils.defaultIfEmpty(inputParams.get(CLEAN_DATA), "False"));

        String region = StringUtils.defaultIfEmpty(inputParams.get("region"), "L");
        String type = StringUtils.defaultIfEmpty(inputParams.get("type"), "UNKNOWN");
        String currency = StringUtils.defaultIfEmpty(inputParams.get("type"), "GBP");

        if (ticker.contains(".")) {
            String[] parts = ticker.split("\\.");
            ticker = parts[0];
            region = parts[1];
        }

        if (ticker.contains("/")) {
            String[] parts = ticker.split("/");
            ticker = parts[0];
            region = parts[1];

            if (parts.length > 2) {
                type = parts[2];
            }
            if (parts.length > 3) {
                currency = parts[3];
            }
        }
        final Instrument instrument = Instrument.fromString(ticker, region, type, currency);

        LocalDate toLocalDate;
        final LocalDate fromLocalDate;

        if (!StringUtils.isEmpty(fromDate)) {
            fromLocalDate = LocalDate.parse(fromDate);
            if (StringUtils.isEmpty(toDate)) {
                toLocalDate = LocalDate.now();
            } else {
                toLocalDate = LocalDate.parse(toDate);
            }
        } else {
            toLocalDate = LocalDate.now();
            if (days > 0) {
                fromLocalDate = LocalDate.now().plusDays(-1 * days);
            } else if (weeks > 0) {
                fromLocalDate = LocalDate.now().plusWeeks(-1 * weeks);
            } else if (months > 0) {
                fromLocalDate = LocalDate.now().plusMonths(-1 * months);
            } else {
                fromLocalDate = LocalDate.now().plusYears(-1 * years);
            }
        }


        return generateResults(fromLocalDate, toLocalDate, interpolate, cleanData, instrument);
    }

    private String generateResults(final LocalDate fromLocalDate, final LocalDate toLocalDate,
                                   final boolean interpolate, final boolean cleanData,
                                   final Instrument instrument)
            throws IOException {
        final StringBuilder sbBody = new StringBuilder();
        final List<List<DataField>> records = Lists.newArrayList();

        final List<Bar> historyData;

        historyData = this.getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, false);

        for (final Bar historicalQuote : historyData) {
            final ArrayList<DataField> record = Lists.newArrayList();
            records.add(record);
            record.add(new DataField("Date", historicalQuote.getEndTime().toLocalDate().toString()));
            record.add(new DataField("Open", historicalQuote.getOpenPrice()));
            record.add(new DataField("High", historicalQuote.getMaxPrice()));
            record.add(new DataField("Low", historicalQuote.getMinPrice()));
            record.add(new DataField("Close", historicalQuote.getClosePrice()));
            record.add(new DataField("Volume", historicalQuote.getVolume()));

            if (historicalQuote instanceof Commentable) {
                Commentable commentable = (Commentable) historicalQuote;
                record.add(new DataField("Comment", commentable.getComment()));

            }
        }

        HtmlTools.printTable(sbBody, records);
        return HtmlTools.createHtmlText(null, sbBody).toString();
    }

    private List<Bar> getHistoryData(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate,
                                     boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
        final Optional<StockV1> stock = this.stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate,
                cleanData, addLatestQuoteToTheSeries);
        if (stock.isPresent()) {
            return stock.get().getHistory();
        }
        return Lists.newArrayList();
    }

}
