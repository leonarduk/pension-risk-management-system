package com.leonarduk.finance.stockfeed.file;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Base class for CSV based stock feeds. Provides common parsing logic and
 * handles basic date and number conversion.
 */
@Slf4j
public abstract class AbstractCsvStockFeed extends AbstractStockFeed {

    private Instrument instrument;
    private LocalDate startDate;
    private LocalDate endDate;

    protected static String formatDate(final DateTimeFormatter formatter, final LocalDate date) {
        return formatter.format(date);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) throws IOException {
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate,
                                 boolean addLatestQuoteToTheSeries) throws IOException {
        if (!this.isAvailable()) {
            log.warn("Feed is not available");
            return Optional.empty();
        }
        this.instrument = instrument;
        this.startDate = fromDate;
        this.endDate = toDate;

        final Set<LocalDate> parsedDates = new HashSet<>();
        final List<Bar> quotes = new LinkedList<>();

        try (BufferedReader reader = openReader()) {
            reader.lines()
                    .map(line -> parseLine(line).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(q -> {
                        DayOfWeek dow = q.getEndTime().atZone(java.time.ZoneId.systemDefault()).getDayOfWeek();
                        return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
                    })
                    .forEach(q -> {
                        LocalDate date = q.getEndTime().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                        if (!parsedDates.add(date)) {
                            log.warn("Duplicate date {} - skipping", date);
                        } else {
                            quotes.add(q);
                        }
                    });
        }

        quotes.sort((o1, o2) -> o2.getEndTime().compareTo(o1.getEndTime()));
        return Optional.of(AbstractStockFeed.createStock(instrument, quotes));
    }

    protected abstract BufferedReader openReader() throws IOException;

    protected abstract String getQueryName(final Instrument instrument);

    public Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(final Instrument instrument) {
        this.instrument = instrument;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public AbstractCsvStockFeed setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public AbstractCsvStockFeed setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    private Optional<ExtendedHistoricalQuote> parseLine(final String line) {
        if (line == null || line.isEmpty()) {
            return Optional.empty();
        }
        String cleanedLine = line.replace("\t", ",");
        String[] parts = cleanedLine.split(",");
        try {
            LocalDate date = parseDate(parts[0]);
            Optional<BigDecimal> open = parseBigDecimal(getField(parts, 1));
            Optional<BigDecimal> high = parseBigDecimal(getField(parts, 2));
            Optional<BigDecimal> low = parseBigDecimal(getField(parts, 3));
            Optional<BigDecimal> close = parseBigDecimal(getField(parts, 4));
            Optional<BigDecimal> volume = parseBigDecimal(getField(parts, 5));
            String comment = getField(parts, 6);
            return Optional.of(new ExtendedHistoricalQuote(this.instrument, date,
                    open.orElse(null), low.orElse(null), high.orElse(null),
                    close.orElse(null), close.orElse(null),
                    volume.orElse(BigDecimal.ONE).longValue(), comment));
        } catch (Exception e) {
            log.warn("Failed to parse {}", line, e);
            return Optional.empty();
        }
    }

    private String getField(String[] parts, int index) {
        return index < parts.length ? parts[index] : "";
    }

    private Optional<BigDecimal> parseBigDecimal(final String input) {
        try {
            if ("-".equals(input) || StringUtils.isBlank(input)) {
                return Optional.empty();
            }
            return Optional.of(NumberUtils.getBigDecimal(input));
        } catch (final NumberFormatException e) {
            log.warn("Failed to parse {}", input);
            return Optional.empty();
        }
    }

    protected LocalDate parseDate(final String fieldValue) throws ParseException {
        return DateUtils.parseDate(fieldValue);
    }
}
