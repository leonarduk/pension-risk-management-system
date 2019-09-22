package com.leonarduk.finance.stockfeed.feed;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;
import org.ta4j.core.num.Num;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * @author steph
 *
 */
public class ExtendedHistoricalQuote implements Bar, Commentable, Comparable<ExtendedHistoricalQuote> {
	@Override
	public String toString() {
		return "ExtendedHistoricalQuote [symbol=" + symbol + ", date=" + date + ", open=" + open + ", low=" + low
				+ ", high=" + high + ", close=" + close + ", adjClose=" + adjClose + ", volume=" + volume + ", comment="
				+ comment + "]";
	}

	private String symbol;

	private LocalDate date;

	private BigDecimal open;
	private BigDecimal low;
	private BigDecimal high;
	private BigDecimal close;

	private BigDecimal adjClose;

	public String getSymbol() {
		return symbol;
	}

	public LocalDate getDate() {
		return date;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public BigDecimal getLow() {
		return low;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public BigDecimal getClose() {
		return close;
	}

	public BigDecimal getAdjClose() {
		return adjClose;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	private Num volume;

	/**
	 * 
	 */
	private static final long serialVersionUID = -6391604492688118701L;
	private String comment;

	public ExtendedHistoricalQuote(HistoricalQuote original) {
		this(original.getSymbol(), original.getDate(), original.getOpen(), original.getLow(), original.getHigh(),
				original.getClose(), original.getAdjClose(), original.getVolume(), "");
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
		this.symbol = symbol;
		this.date = date;
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
		this(symbol, DateUtils.calendarToLocalDate(date), open, low, high, close, adjClose, DoubleNum.valueOf(volume),
				comment);
	}

	public ExtendedHistoricalQuote(Instrument instrument, LocalDate date, BigDecimal open, BigDecimal low,
			BigDecimal high, BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
		this(instrument.getCode(), date, open, low, high, close, adjClose, DoubleNum.valueOf(volume), comment);
	}

	public ExtendedHistoricalQuote(Bar lastQuote, LocalDate today, String string) {
		this(lastQuote);
		this.setDate(today);
		this.setComment(string);
	}

	private void setDate(LocalDate today) {
		this.date = today;
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

	private void setComment(String string) {
		this.comment = string;
	}

	@Override
	public String getComment() {
		return this.comment;
	}

	public Instrument getInstrument() throws IOException {
		return Instrument.fromString(getSymbol());
	}

	public LocalDate getLocaldate() {
		return this.date;
	}

	public static List<Bar> from(List<HistoricalQuote> original) {
		return original.stream().map(o -> new ExtendedHistoricalQuote(o)).collect(Collectors.toList());
	}

	@Override
	public Num getOpenPrice() {
		return DoubleNum.valueOf(getOpen());
	}

	@Override
	public Num getMinPrice() {
		return DoubleNum.valueOf(getLow());
	}

	@Override
	public Num getMaxPrice() {
		return DoubleNum.valueOf(getHigh());
	}

	@Override
	public Num getClosePrice() {
		return DoubleNum.valueOf(getClose());
	}

	@Override
	public Num getVolume() {
		return this.volume;
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
