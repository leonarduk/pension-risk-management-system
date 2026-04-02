package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.datatransformation.correction.ValueScalingTransformer;
import com.leonarduk.finance.stockfeed.feed.Commentable;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DataField;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Service for retrieving and formatting historical data from a {@link StockFeed}.
 * This centralises the parameter parsing and record generation used by
 * different entry points (lambda and REST).
 */
public class HistoricalDataService {
    private final StockFeed stockFeed;

    public HistoricalDataService(@NotNull StockFeed stockFeed) {
        this.stockFeed = Objects.requireNonNull(stockFeed, "stockFeed");
    }

    /**
     * Fetch historical data based on the supplied parameters.
     * <p>
     * Expected parameters include:
     * <ul>
     *   <li>ticker (required)</li>
     *   <li>years/months/weeks/days or fromDate/toDate</li>
     *   <li>interpolate, cleanData</li>
     *   <li>region, type, currency</li>
     *   <li>scaling, category</li>
     * </ul>
     *
     * @param params request parameters
     * @return list of {@link DataField} records
     * @throws IOException when the underlying feed cannot be accessed
     */
    public @NotNull List<List<DataField>> getRecords(@NotNull Map<String, String> params) throws IOException {
        Objects.requireNonNull(params, "params");
        if (!params.containsKey("ticker") || StringUtils.isBlank(params.get("ticker"))) {
            throw new IllegalArgumentException("Ticker parameter is required");
        }

        String ticker = params.get("ticker");
        String region = StringUtils.defaultIfEmpty(params.get("region"), "L");
        String type = StringUtils.defaultIfEmpty(params.get("type"), "UNKNOWN");
        String currency = params.get("currency");

        if (ticker.contains(".")) {
            String[] parts = ticker.split("\\.");
            ticker = parts[0];
            region = parts[1];
            if ("N".equalsIgnoreCase(region)) {
                region = "NY";
            }
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

        if (StringUtils.isBlank(currency)) {
            currency = Instrument.resolveCurrency(params.get("ticker"));
        }
        Instrument instrument = Instrument.fromString(ticker, region, type, currency);

        String category = params.get("category");
        if (category != null && !category.equalsIgnoreCase(instrument.category())) {
            return Collections.emptyList();
        }

        // Date handling
        String fromDate = params.get("fromDate");
        String toDate = params.get("toDate");
        int years = Integer.parseInt(StringUtils.defaultIfEmpty(params.get("years"), "10"));
        int months = Integer.parseInt(StringUtils.defaultIfEmpty(params.get("months"), "0"));
        int weeks = Integer.parseInt(StringUtils.defaultIfEmpty(params.get("weeks"), "0"));
        int days = Integer.parseInt(StringUtils.defaultIfEmpty(params.get("days"), "0"));

        LocalDate toLocalDate;
        LocalDate fromLocalDate;
        if (StringUtils.isNotBlank(fromDate)) {
            fromLocalDate = LocalDate.parse(fromDate);
            toLocalDate = StringUtils.isBlank(toDate) ? LocalDate.now() : LocalDate.parse(toDate);
        } else {
            toLocalDate = LocalDate.now();
            if (days > 0) {
                fromLocalDate = LocalDate.now().plusDays(-days);
            } else if (weeks > 0) {
                fromLocalDate = LocalDate.now().plusWeeks(-weeks);
            } else if (months > 0) {
                fromLocalDate = LocalDate.now().plusMonths(-months);
            } else {
                fromLocalDate = LocalDate.now().plusYears(-years);
            }
        }

        boolean interpolate = Boolean.parseBoolean(StringUtils.defaultIfEmpty(params.get("interpolate"), "false"));
        boolean cleanData = Boolean.parseBoolean(StringUtils.defaultIfEmpty(params.get("cleanData"), "false"));
        Double scaling = params.containsKey("scaling") ? Double.valueOf(params.get("scaling")) : null;

        return generateRecords(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, scaling);
    }

    /**
     * Generates {@link DataField} records for the supplied instrument and period.
     */
    public @NotNull List<List<DataField>> generateRecords(@NotNull Instrument instrument,
                                                 @NotNull LocalDate fromLocalDate,
                                                 @NotNull LocalDate toLocalDate,
                                                 boolean interpolate,
                                                 boolean cleanData,
                                                 @Nullable Double scaling) throws IOException {
        Objects.requireNonNull(instrument, "instrument");
        Objects.requireNonNull(fromLocalDate, "fromLocalDate");
        Objects.requireNonNull(toLocalDate, "toLocalDate");
        List<Bar> historyData = getHistoryData(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, scaling);
        List<List<DataField>> records = new ArrayList<>();
        for (Bar historicalQuote : historyData) {
            List<DataField> record = new ArrayList<>();
            record.add(new DataField("Date", historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate().toString()));
            record.add(new DataField("Open", historicalQuote.getOpenPrice()));
            record.add(new DataField("High", historicalQuote.getHighPrice()));
            record.add(new DataField("Low", historicalQuote.getLowPrice()));
            record.add(new DataField("Close", historicalQuote.getClosePrice()));
            record.add(new DataField("Volume", historicalQuote.getVolume()));
            if (historicalQuote instanceof Commentable commentable) {
                record.add(new DataField("Comment", commentable.getComment()));
            }
            records.add(record);
        }
        return records;
    }

    private @NotNull List<Bar> getHistoryData(@NotNull Instrument instrument,
                                     @NotNull LocalDate fromLocalDate,
                                     @NotNull LocalDate toLocalDate,
                                     boolean interpolate,
                                     boolean cleanData,
                                     @Nullable Double scaling) throws IOException {
        Objects.requireNonNull(instrument, "instrument");
        Objects.requireNonNull(fromLocalDate, "fromLocalDate");
        Objects.requireNonNull(toLocalDate, "toLocalDate");
        Optional<StockV1> stock = stockFeed.get(instrument, fromLocalDate, toLocalDate, interpolate, cleanData, false);
        if (stock.isPresent()) {
            List<Bar> history = stock.get().getHistory();
            if (scaling != null) {
                return new ValueScalingTransformer(instrument, scaling).clean(history);
            }
            return history;
        }
        return new ArrayList<>();
    }
}

