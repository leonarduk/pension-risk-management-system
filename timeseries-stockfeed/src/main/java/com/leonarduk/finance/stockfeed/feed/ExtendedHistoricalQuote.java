package com.leonarduk.finance.stockfeed.feed;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.time.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Measurement(name = "HistoricalQuote")
public class ExtendedHistoricalQuote extends HistoricalQuote
        implements Bar, Commentable, Comparable<ExtendedHistoricalQuote> {
    @Serial
    private static final long serialVersionUID = -6391604492688118701L;

    @Column(tag = true)
    private final String symbol;

    public String getSymbol() {
        return symbol;
    }

    @Column(timestamp = true)
    private Instant date;

    @Override
    public Calendar getDate() {
        return DateUtils.localDateToCalendar(getLocalDate());
    }

    public Calendar getCalendarDate() {
        return getDate();
    }

    public LocalDate getLocalDate() {
        return date.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setDate(Instant today) {
        this.date = today;
    }

    @Column
    private final BigDecimal open;

    public BigDecimal getOpen() {
        return open;
    }

    @Override
    public Num getOpenPrice() {
        return DoubleNum.valueOf(getOpen());
    }

    @Column
    private final BigDecimal low;

    public BigDecimal getLow() {
        return low;
    }

    @Override
    public Num getLowPrice() {
        return DoubleNum.valueOf(getLow());
    }

    @Column
    private final BigDecimal high;

    public BigDecimal getHigh() {
        return high;
    }

    @Column
    private final BigDecimal close;

    public BigDecimal getClose() {
        return close == null ? BigDecimal.ZERO : close;
    }

    @Column
    private final BigDecimal adjClose;

    public BigDecimal getAdjClose() {
        return adjClose;
    }

    @Column
    private final Long volume;

    @Override
    public Long getVolume() {
        return this.volume;
    }

    public Num getVolumeAsNum() {
        return DoubleNum.valueOf(getVolume() == null ? 0 : getVolume());
    }

    @Column(tag = true)
    private String comment;

    @Override
    public String getComment() {
        return this.comment;
    }

    private void setComment(String string) {
        this.comment = string;
    }

    public ExtendedHistoricalQuote(HistoricalQuote original, String comment) {
        this(original.getSymbol(), original.getDate(), original.getOpen(), original.getLow(), original.getHigh(),
                original.getClose(), original.getAdjClose(), original.getVolume(), comment);
    }

    public ExtendedHistoricalQuote(Bar original) {
        this("", original.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                BigDecimal.valueOf(original.getOpenPrice().doubleValue()),
                BigDecimal.valueOf(original.getLowPrice().doubleValue()),
                BigDecimal.valueOf(original.getHighPrice().doubleValue()),
                BigDecimal.valueOf(original.getClosePrice().doubleValue()),
                BigDecimal.valueOf(original.getClosePrice().doubleValue()),
                original.getVolume().longValue(), "");
    }

    /**
     * @param symbol
     * @param date
     * @param open
     * @param low
     * @param high
     * @param close
     * @param adjClose
     * @param volume
     * @param comment
     */
    public ExtendedHistoricalQuote(String symbol, LocalDate date, BigDecimal open, BigDecimal low, BigDecimal high,
                                   BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
        super();
        this.symbol = symbol;
        this.date = date.atStartOfDay(ZoneId.systemDefault()).toInstant();
        this.open = open;
        this.low = low;
        this.high = high;
        this.close = close;
        this.adjClose = adjClose;
        this.volume = volume;
        this.comment = comment;
    }

    public ExtendedHistoricalQuote(String symbol, Calendar date, BigDecimal open, BigDecimal low, BigDecimal high,
                                   BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
        this(symbol, DateUtils.calendarToLocalDate(date), open, low, high, close, adjClose,
                volume == null ? 0L : volume,
                comment);
    }

    public ExtendedHistoricalQuote(Instrument instrument, LocalDate date, BigDecimal open, BigDecimal low,
                                   BigDecimal high, BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
        this(instrument.getCode(), date, open, low, high, close, adjClose,
                volume == null ? 0L : volume, comment);
    }

    public ExtendedHistoricalQuote(Bar lastQuote, LocalDate today, String string) {
        this(lastQuote);
        this.setDate(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.setComment(string);
    }

    public ExtendedHistoricalQuote(Instrument instrument, LocalDate localDate, double open, double low, double high,
                                   double close, double adjustedClose, long volume, String comment) {
        this(instrument, localDate, BigDecimal.valueOf(open), BigDecimal.valueOf(low), BigDecimal.valueOf(high),
                BigDecimal.valueOf(close), BigDecimal.valueOf(adjustedClose), volume, comment);
    }

    public ExtendedHistoricalQuote(ExtendedHistoricalQuote original) {
        this(original.getSymbol(), original.getLocalDate(), original.getOpen(), original.getLow(), original.getHigh(),
                original.getClose(), original.getAdjClose(), original.getVolume(), "");
    }


    public ExtendedHistoricalQuote(String string, LocalDate localDate, Num open, Num low, Num high, Num close,
                                   Num volume, String comment)  {
        this(Instrument.fromString(string), localDate, BigDecimal.valueOf(open.doubleValue()),
                BigDecimal.valueOf(low.doubleValue()), BigDecimal.valueOf(high.doubleValue()),
                BigDecimal.valueOf(close.doubleValue()), BigDecimal.valueOf(close.doubleValue()),
                volume.longValue(), comment);
    }

    public ExtendedHistoricalQuote(Instrument instrument, Map<String, Object> valuesMap) {
        this.symbol = instrument.code();
        this.date = (Instant) valuesMap.get("date");
        this.open = BigDecimal.valueOf((Double) valuesMap.getOrDefault("open", 0.0));
        this.low = BigDecimal.valueOf((Double) valuesMap.getOrDefault("low", 0.0));
        this.high = BigDecimal.valueOf((Double) valuesMap.getOrDefault("high", 0.0));
        this.close = BigDecimal.valueOf((Double) valuesMap.getOrDefault("close", 0.0));
        this.adjClose = BigDecimal.valueOf((Double) valuesMap.getOrDefault("adjClose", 0.0));
        this.volume = Long.valueOf((String) valuesMap.getOrDefault("volume", "0"));
        this.comment = "MAP" + valuesMap.getOrDefault("comment", "").toString();
    }

    public static List<Bar> from(List<HistoricalQuote> original) {
        return original.stream().map(o -> new ExtendedHistoricalQuote(o, "")).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ExtendedHistoricalQuote [symbol=" + symbol + ", date=" + date + ", open=" + open + ", low=" + low
                + ", high=" + high + ", close=" + close + ", adjClose=" + adjClose + ", volume=" + volume + ", comment="
                + comment + "]";
    }


    public Instrument getInstrument() throws IOException {
        return Instrument.fromString(getSymbol());
    }

    public Instant getTimestamp() {
        return this.date;
    }

    @Override
    public Num getHighPrice() {
        return DoubleNum.valueOf(getHigh());
    }

    @Override
    public Num getClosePrice() {
        return DoubleNum.valueOf(getClose());
    }

    @Override
    public Num getAmount() {
        return DoubleNum.valueOf(getClose());
    }

    @Override
    public long getTrades() {
        return getVolume() == null ? 0L : getVolume();
    }

    @Override
    public Duration getTimePeriod() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Instant getBeginTime() {
        throw new UnsupportedOperationException();

    }

    @Override
    public Instant getEndTime() {
        return date;
    }

    @Override
    public void addTrade(Num tradeVolume, Num tradePrice) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPrice(Num price) {
        throw new UnsupportedOperationException();

    }

    @Override
    public int compareTo(ExtendedHistoricalQuote that) {
        return this.getEndTime().compareTo(that.getEndTime());

    }

}
