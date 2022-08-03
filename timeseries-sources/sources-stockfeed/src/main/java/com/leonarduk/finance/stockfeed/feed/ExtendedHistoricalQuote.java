package com.leonarduk.finance.stockfeed.feed;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;
import yahoofinance.histquotes.HistoricalQuote;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@DynamoDBTable(tableName = "HistoricalQuote")
@Measurement(name = "HistoricalQuote")
public class ExtendedHistoricalQuote extends HistoricalQuote
        implements Bar, Commentable, Comparable<ExtendedHistoricalQuote> {
    private static final long serialVersionUID = -6391604492688118701L;

    @DynamoDBHashKey(attributeName = "symbol")
    @Column(tag = true)
    private final String symbol;

    public String getSymbol() {
        return symbol;
    }

    @DynamoDBAttribute(attributeName = "date")
    @Column(timestamp = true)
    private Instant date;

    public LocalDate getDate() {
        return LocalDate.ofInstant(date, ZoneId.systemDefault());
    }

    private void setDate(Instant today) {
        this.date = today;
    }

    @DynamoDBAttribute(attributeName = "open")
    @Column
    private final BigDecimal open;

    public BigDecimal getOpen() {
        return open;
    }

    @Override
    public Num getOpenPrice() {
        return DoubleNum.valueOf(getOpen());
    }

    @DynamoDBAttribute(attributeName = "low")
    @Column
    private final BigDecimal low;

    public BigDecimal getLow() {
        return low;
    }

    @Override
    public Num getMinPrice() {
        return DoubleNum.valueOf(getLow());
    }

    @DynamoDBAttribute(attributeName = "high")
    @Column
    private final BigDecimal high;

    public BigDecimal getHigh() {
        return high;
    }

    @DynamoDBAttribute(attributeName = "close")
    @Column
    private final BigDecimal close;

    public BigDecimal getClose() {
        return close == null ? BigDecimal.ZERO : close;
    }

    @DynamoDBAttribute(attributeName = "adjClose")
    @Column
    private final BigDecimal adjClose;

    public BigDecimal getAdjClose() {
        return adjClose;
    }

    // Stored as String
    @Column
    private final Num volume;

    @DynamoDBAttribute
    @Override
    public Num getVolume() {
        return this.volume;
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
        this(original.getSymbol(), original.getDateAsCalendar(), original.getOpen(), original.getLow(), original.getHigh(),
                original.getClose(), original.getAdjClose(), original.getVolumeAsLong(), comment);
    }

    public ExtendedHistoricalQuote(Bar original) {
        this("", original.getEndTime().toLocalDate(), BigDecimal.valueOf(original.getOpenPrice().doubleValue()),
                BigDecimal.valueOf(original.getMinPrice().doubleValue()),
                BigDecimal.valueOf(original.getMaxPrice().doubleValue()),
                BigDecimal.valueOf(original.getClosePrice().doubleValue()),
                BigDecimal.valueOf(original.getClosePrice().doubleValue()), original.getVolume(), "");
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
                                   BigDecimal close, BigDecimal adjClose, Num volume, final String comment) {
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
                DoubleNum.valueOf(volume == null ? 0 : volume),
                comment);
    }

    public ExtendedHistoricalQuote(Instrument instrument, LocalDate date, BigDecimal open, BigDecimal low,
                                   BigDecimal high, BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
        this(instrument.getCode(), date, open, low, high, close, adjClose,
                DoubleNum.valueOf(volume == null ? 0 : volume), comment);
    }

    public ExtendedHistoricalQuote(Bar lastQuote, LocalDate today, String string) {
        this(lastQuote);
        this.setDate(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        this.setComment(string);
    }

    public ExtendedHistoricalQuote(Instrument instrument, LocalDate localDate, double open, double low, double high,
                                   double close, double adjustedClose, long volume, String comment) {
        this(instrument, localDate, BigDecimal.valueOf(open), BigDecimal.valueOf(low), BigDecimal.valueOf(high),
                BigDecimal.valueOf(close), BigDecimal.valueOf(adjustedClose), Long.valueOf(volume), comment);
    }

    public ExtendedHistoricalQuote(ExtendedHistoricalQuote original) {
        this(original.getSymbol(), original.getDate(), original.getOpen(), original.getLow(), original.getHigh(),
                original.getClose(), original.getAdjClose(), original.getVolume(), "");
    }


    public ExtendedHistoricalQuote(String string, LocalDate localDate, Num open, Num low, Num high, Num close,
                                   Num volume, String comment) throws IOException {
        this(Instrument.fromString(string), localDate, BigDecimal.valueOf(open.doubleValue()),
                BigDecimal.valueOf(low.doubleValue()), BigDecimal.valueOf(high.doubleValue()),
                BigDecimal.valueOf(close.doubleValue()), BigDecimal.valueOf(close.doubleValue()),
                Long.valueOf(volume.longValue()), comment);
    }

    public ExtendedHistoricalQuote(Instrument instrument, Map valuesMap) {
        this.symbol = instrument.code();
        this.date = (Instant) valuesMap.get("date");
        this.open = BigDecimal.valueOf((Double) valuesMap.getOrDefault("open", 0.0));
        this.low = BigDecimal.valueOf((Double) valuesMap.getOrDefault("low", 0.0));
        this.high = BigDecimal.valueOf((Double) valuesMap.getOrDefault("high", 0.0));
        this.close = BigDecimal.valueOf((Double) valuesMap.getOrDefault("close", 0.0));
        this.adjClose = BigDecimal.valueOf((Double) valuesMap.getOrDefault("adjClose", 0.0));
        this.volume = DoubleNum.valueOf((String) valuesMap.getOrDefault("volume", "0.0"));
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

    public Instant getLocaldate() {
        return this.date;
    }

    @Override
    public Num getMaxPrice() {
        return DoubleNum.valueOf(getHigh());
    }

    @DynamoDBAttribute
    @Override
    public Num getClosePrice() {
        return DoubleNum.valueOf(getClose());
    }

    @Override
    public Num getAmount() {
        return DoubleNum.valueOf(getClose());
    }

    @Override
    public int getTrades() {
        return getVolume().intValue();
    }

    @Override
    public Duration getTimePeriod() {
        throw new UnsupportedOperationException();

    }

    @Override
    public ZonedDateTime getBeginTime() {
        throw new UnsupportedOperationException();

    }

    @Override
    public ZonedDateTime getEndTime() {
        return getDate().atStartOfDay(ZoneId.systemDefault());
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
