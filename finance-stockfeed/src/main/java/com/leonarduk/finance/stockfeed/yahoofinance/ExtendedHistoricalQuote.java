package com.leonarduk.finance.stockfeed.yahoofinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.DateUtils;

import yahoofinance.histquotes.HistoricalQuote;

public class ExtendedHistoricalQuote extends HistoricalQuote {

	private String comment;

	public ExtendedHistoricalQuote(HistoricalQuote original) {
		super(original.getSymbol(), original.getDate(), original.getOpen(), original.getLow(), original.getHigh(),
				original.getClose(), original.getAdjClose(), original.getVolume());
	}

	public ExtendedHistoricalQuote(String symbol, LocalDate date, BigDecimal open, BigDecimal low, BigDecimal high,
			BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
		super(symbol, DateUtils.dateToCalendar(date), open, low, high, close, adjClose, volume);
		this.comment = comment;
	}

	public ExtendedHistoricalQuote(String symbol, Calendar date, BigDecimal open, BigDecimal low, BigDecimal high,
			BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
		super(symbol, date, open, low, high, close, adjClose, volume);
		this.comment = comment;
	}

	public ExtendedHistoricalQuote(Instrument instrument, LocalDate date, BigDecimal open, BigDecimal low,
			BigDecimal high, BigDecimal close, BigDecimal adjClose, Long volume, final String comment) {
		super(instrument.getCode(), DateUtils.dateToCalendar(date), open, low, high, close, adjClose, volume);
		this.comment = comment;
	}

	public ExtendedHistoricalQuote(ExtendedHistoricalQuote lastQuote, LocalDate today, String string) {
		this(lastQuote);
		this.setDate(DateUtils.dateToCalendar(today));
		this.setComment(string);
	}

	public ExtendedHistoricalQuote(Instrument instrument, LocalDate localDate, double open, double low, double high,
			double close, double adjustedClose, long volume, String comment) {
		this(instrument, localDate, BigDecimal.valueOf(open), BigDecimal.valueOf(low), BigDecimal.valueOf(high),
				BigDecimal.valueOf(close), BigDecimal.valueOf(adjustedClose), Long.valueOf(volume), comment);
	}

	private void setComment(String string) {
		this.comment = string;
	}

	public String getComment() {
		return this.comment;
	}

	public Instrument getInstrument() throws IOException {
		return Instrument.fromString(getSymbol());
	}

	public LocalDate getLocaldate() {
		return LocalDateTime.ofInstant(getDate().toInstant(), getDate().getTimeZone().toZoneId()).toLocalDate();
	}

	public static List<ExtendedHistoricalQuote> from(List<HistoricalQuote> original) {
		return original.stream().map(o -> new ExtendedHistoricalQuote(o)).collect(Collectors.toList());
	}
}
