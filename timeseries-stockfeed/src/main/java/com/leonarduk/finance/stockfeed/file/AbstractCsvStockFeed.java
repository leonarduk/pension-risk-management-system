package com.leonarduk.finance.stockfeed.file;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
public abstract class AbstractCsvStockFeed extends AbstractStockFeed {
    private Optional<BigDecimal> close;
    private String comment;
    private Date date;
    private Date endDate;
    private Optional<BigDecimal> high;
    private Instrument instrument;
    private Optional<BigDecimal> low;
    private Optional<BigDecimal> open;
    private BufferedReader reader;
    private Date startDate;
    private Optional<BigDecimal> volume;

    protected static @NotNull String formatDate(@NotNull final DateFormat formatter, @NotNull final Date date) {
        synchronized (formatter) {
            return formatter.format(date);
        }
    }

    public @NotNull ExtendedHistoricalQuote asHistoricalQuote() {
        return new ExtendedHistoricalQuote(this.instrument.code(), DateUtils.dateToCalendar(this.date),
                this.getOpen().orElse(null), this.getLow().orElse(null), this.getHigh().orElse(null),
                this.getClose().orElse(null), this.getClose().orElse(null),
                this.getVolume().orElse(BigDecimal.ONE).longValue(), this.getComment());
    }

    @Override
    public @NotNull Optional<StockV1> get(@NotNull final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) throws IOException {
        Objects.requireNonNull(instrument, "instrument");
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    @Override
    public @NotNull Optional<StockV1> get(@NotNull final Instrument instrument, @NotNull final LocalDate fromDate, @NotNull final LocalDate toDate,
                                 boolean addLatestQuoteToTheSeries)
            throws IOException {
        Objects.requireNonNull(instrument, "instrument");
        Objects.requireNonNull(fromDate, "fromDate");
        Objects.requireNonNull(toDate, "toDate");
        if (!this.isAvailable()) {
            log.warn("Feed is not available");
            return Optional.empty();
        }
        try {
            this.setInstrument(instrument);
            this.setStartDate(DateUtils.convertToDateViaInstant(fromDate));
            this.setEndDate(DateUtils.convertToDateViaInstant(toDate));

            final Set<LocalDate> parsedDates = new HashSet<>();
            final List<Bar> quotes = new LinkedList<>();
            while (this.next()) {
                ExtendedHistoricalQuote asHistoricalQuote = this.asHistoricalQuote();
                if (asHistoricalQuote.getLocalDate().getDayOfWeek() != DayOfWeek.SATURDAY
                        && asHistoricalQuote.getLocalDate().getDayOfWeek() != DayOfWeek.SUNDAY) {
                    if (parsedDates.contains(asHistoricalQuote.getLocalDate())) {
                          log.warn("Duplicate date {} - skipping", asHistoricalQuote.getLocalDate().toString());
                    } else {
                        quotes.add(asHistoricalQuote);
                        parsedDates.add(asHistoricalQuote.getLocalDate());
                    }
                }
            }

            quotes.sort((o1, o2) -> o2.getEndTime().compareTo(o1.getEndTime()));
            return Optional.of(AbstractStockFeed.createStock(instrument, quotes));
        } catch (final Exception e) {
              log.warn("Failed:{} : {}", this, e.getMessage());
            return Optional.empty();
        }

    }

    @Override
    public String toString() {
        return "CsvStockFeed [close=" + close + ", comment=" + comment + ", date=" + date + ", endDate=" + endDate
                + ", high=" + high + ", instrument=" + instrument + ", low=" + low + ", open=" + open + ", reader="
                + reader + ", startDate=" + startDate + ", volume=" + volume + "]";
    }

    /**
     * Get close price of stock quote
     *
     * @return close
     * @see #next()
     */
    public @NotNull Optional<BigDecimal> getClose() {
        return this.close;
    }

    public @NotNull String getComment() {
        if (StringUtils.isEmpty(this.comment)) {
            return this.getClass().getName();
        }
        return this.comment;
    }

    public void setComment(@Nullable final String comment) {
        this.comment = comment;
    }

    /**
     * Get date of stock quote
     *
     * @return date
     * @see #next()
     */
    public @Nullable Date getDate() {
        return this.date;
    }

    public @Nullable Date getEndDate() {
        return this.endDate;
    }

    public @NotNull AbstractCsvStockFeed setEndDate(@Nullable final Calendar endDate) {
        return this.setEndDate(endDate != null ? endDate.getTime() : null);
    }

    public @NotNull AbstractCsvStockFeed setEndDate(@Nullable final Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public @NotNull Exchange getExchange() {
        return this.instrument.getExchange();
    }

    /**
     * Get high price of stock quote
     *
     * @return high
     * @see #next()
     */
    public @NotNull Optional<BigDecimal> getHigh() {
        return this.high;
    }

    public @NotNull Instrument getInstrument() {
        return this.instrument;
    }

    public void setInstrument(@NotNull final Instrument instrument) {
        this.instrument = Objects.requireNonNull(instrument, "instrument");
    }

    /**
     * Get low price of stock quote
     *
     * @return low
     * @see #next()
     */
    public @NotNull Optional<BigDecimal> getLow() {
        return this.low;
    }

    /**
     * Get open price of stock quote
     *
     * @return open
     * @see #next()
     */
    public @NotNull Optional<BigDecimal> getOpen() {
        return this.open;
    }

    protected abstract @NotNull String getQueryName(@NotNull final Instrument instrument);

    public @Nullable BufferedReader getReader() {
        return this.reader;
    }

    public @Nullable Date getStartDate() {
        return this.startDate;
    }

    public @NotNull AbstractCsvStockFeed setStartDate(@Nullable final Calendar startDate) {
        return this.setStartDate(startDate != null ? startDate.getTime() : null);
    }

    public @NotNull AbstractCsvStockFeed setStartDate(@Nullable final Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public @NotNull String getSymbol() {
        return this.instrument.code();
    }

    /**
     * Get volume of stock quote
     *
     * @return volume
     * @see #next()
     */
    public @NotNull Optional<BigDecimal> getVolume() {
        return this.volume;
    }

    /**
     * Advance to next stock quote in response
     * <p>
     * This method will open a new request on the first call and will update the
     * fields for open, close, high, low, and volume each time it is called.
     *
     * @return true if another quote was parsed, false if no more quotes exist to
     * read
     * @throws IOException may fail to read file
     */
    public boolean next() throws IOException {
        if (this.reader == null) {
            this.reader = this.openReader();
        }

        return this.parseReader(this.reader);
    }

    protected abstract @NotNull BufferedReader openReader() throws IOException;

    private @NotNull Optional<BigDecimal> parseBigDecimal(@NotNull final String input) {
        try {
            if ("-".equals(input)) {
                return Optional.empty();
            }
            return Optional.of(NumberUtils.getBigDecimal(input));
        } catch (final NumberFormatException e) {
              log.warn("Failed to parse {}", input);
            return Optional.empty();
        }
    }

    protected @NotNull Date parseDate(@NotNull final String fieldValue) throws ParseException {
        return DateUtils.parseDate(fieldValue);
    }

    protected boolean parseReader(@NotNull final BufferedReader reader2) throws IOException {
        try {
            String line = reader2.readLine();
            if ((line == null) || (line.isEmpty())) {
                this.release();
                return false;
            }
            final String tab = "\t";
            if (line.contains(tab)) {
                  log.warn("Messed up Csv - found tabs");
                line = line.replace(tab, ",");
            }

            final int length = line.length();
            int start = 0;
            int comma = line.indexOf(',');
            int column = 0;
            this.comment = "";
            while (start < length) {
                final String fieldValue = line.substring(start, comma);
                switch (column++) {
                    case 0:
                        this.date = this.parseDate(fieldValue);
                        break;
                    case 1:
                        this.open = this.parseBigDecimal(fieldValue);
                        break;
                    case 2:
                        this.high = this.parseBigDecimal(fieldValue);
                        break;
                    case 3:
                        this.low = this.parseBigDecimal(fieldValue);
                        break;
                    case 4:
                        this.close = this.parseBigDecimal(fieldValue);
                        break;
                    case 5:
                        this.volume = this.parseBigDecimal(fieldValue);
                        break;
                    case 6:
                        this.comment = fieldValue;
                        break;
                    default:
                        // ignore
                }
                start = comma + 1;
                comma = line.indexOf(',', start);
                if (comma == -1) {
                    comma = length;
                }
            }
            return true;
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    public @NotNull AbstractCsvStockFeed release() {
        if (this.reader != null) {
            try {
                this.reader.close();
            } catch (final IOException ignored) {
                // Ignored
            }
        }
        this.reader = null;
        return this;
    }

}
