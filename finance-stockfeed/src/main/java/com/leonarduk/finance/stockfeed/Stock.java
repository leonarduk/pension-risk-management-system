package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockDividend;
import yahoofinance.quotes.stock.StockQuote;

/**
 *
 * @author Stijn Strickx
 */
public class Stock {

	private String					currency;

	private StockDividend			dividend;

	private List<HistoricalQuote>	history;
	private final Instrument		instrument;
	private StockQuote				quote;

	public static final Logger		logger	= Logger
	        .getLogger(Stock.class.getName());

	public Stock(final Instrument instrument) {
		this.instrument = instrument;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Stock)) {
			return false;
		}
		final Stock castOther = (Stock) other;
		return new EqualsBuilder().append(this.currency, castOther.currency)
		        .append(this.dividend, castOther.dividend)
		        .append(this.history, castOther.history)
		        .append(this.instrument, castOther.instrument)
		        .append(this.quote, castOther.quote).isEquals();
	}

	/**
	 * Get the currency of the stock
	 *
	 * @return the currency or null if the data is not available
	 */
	public String getCurrency() {
		return this.currency;
	}

	/**
	 * Returns the dividend data available for this stock.
	 *
	 * @return dividend data available for this stock
	 * @see #getDividend(boolean)
	 */
	public StockDividend getDividend() {
		return this.dividend;
	}

	/**
	 * This method will return historical quotes from this stock. If the
	 * historical quotes are not available yet, they will be requested first
	 * from Yahoo Finance.
	 * <p>
	 * If the historical quotes are not available yet, the following
	 * characteristics will be used for the request:
	 * <ul>
	 * <li>from: 1 year ago (default)
	 * <li>to: today (default)
	 * <li>interval: MONTHLY (default)
	 * </ul>
	 * <p>
	 * There are several more methods available that allow you to define some
	 * characteristics of the historical data. Calling one of those methods will
	 * result in a new request being sent to Yahoo Finance.
	 *
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory(yahoofinance.histquotes.Interval)
	 * @see #getHistory(java.util.Calendar)
	 * @see #getHistory(java.util.Calendar, java.util.Calendar)
	 * @see #getHistory(java.util.Calendar, yahoofinance.histquotes.Interval)
	 * @see #getHistory(java.util.Calendar, java.util.Calendar,
	 *      yahoofinance.histquotes.Interval)
	 */
	public List<HistoricalQuote> getHistory() throws IOException {
		if (this.history == null) {
			return Lists.newArrayList();
		}
		return this.history;
	}

	/**
	 * Requests the historical quotes for this stock with the following
	 * characteristics.
	 * <ul>
	 * <li>from: specified value
	 * <li>to: today (default)
	 * <li>interval: MONTHLY (default)
	 * </ul>
	 *
	 * @param from
	 *            start date of the historical data
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory()
	 */
	public List<HistoricalQuote> getHistory(final Calendar from)
	        throws IOException {
		return this.getHistory(from, HistQuotesRequest.DEFAULT_TO);
	}

	/**
	 * Requests the historical quotes for this stock with the following
	 * characteristics.
	 * <ul>
	 * <li>from: specified value
	 * <li>to: specified value
	 * <li>interval: MONTHLY (default)
	 * </ul>
	 *
	 * @param from
	 *            start date of the historical data
	 * @param to
	 *            end date of the historical data
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory()
	 */
	public List<HistoricalQuote> getHistory(final Calendar from,
	        final Calendar to) throws IOException {
		return this.getHistory(from, to, Interval.MONTHLY);
	}

	/**
	 * Requests the historical quotes for this stock with the following
	 * characteristics.
	 * <ul>
	 * <li>from: specified value
	 * <li>to: specified value
	 * <li>interval: specified value
	 * </ul>
	 *
	 * @param from
	 *            start date of the historical data
	 * @param to
	 *            end date of the historical data
	 * @param interval
	 *            the interval of the historical data
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory()
	 */
	public List<HistoricalQuote> getHistory(final Calendar from,
	        final Calendar to, final Interval interval) throws IOException {
		final HistQuotesRequest hist = new HistQuotesRequest(this.instrument,
		        from, to, interval);
		this.setHistory(hist.getResult());
		return this.history;
	}

	/**
	 * Requests the historical quotes for this stock with the following
	 * characteristics.
	 * <ul>
	 * <li>from: specified value
	 * <li>to: today (default)
	 * <li>interval: specified value
	 * </ul>
	 *
	 * @param from
	 *            start date of the historical data
	 * @param interval
	 *            the interval of the historical data
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory()
	 */
	public List<HistoricalQuote> getHistory(final Calendar from,
	        final Interval interval) throws IOException {
		return this.getHistory(from, HistQuotesRequest.DEFAULT_TO, interval);
	}

	/**
	 * Requests the historical quotes for this stock with the following
	 * characteristics.
	 * <ul>
	 * <li>from: 1 year ago (default)
	 * <li>to: today (default)
	 * <li>interval: specified value
	 * </ul>
	 *
	 * @param interval
	 *            the interval of the historical data
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory()
	 */
	public List<HistoricalQuote> getHistory(final Interval interval)
	        throws IOException {
		return this.getHistory(HistQuotesRequest.DEFAULT_FROM, interval);
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	/**
	 * Get the full name of the stock
	 *
	 * @return the name or null if the data is not available
	 */
	public String getName() {
		return this.instrument.getName();
	}

	/**
	 * Returns the basic quotes data available for this stock.
	 *
	 * @return basic quotes data available for this stock
	 * @see #getQuote(boolean)
	 */
	public StockQuote getQuote() {
		if (this.quote == null) {
			return new StockQuote.StockQuoteBuilder(this.instrument).build();
		}
		return this.quote;
	}

	/**
	 * Get the exchange on which the stock is traded
	 *
	 * @return the exchange or null if the data is not available
	 */
	public Exchange getStockExchange() {
		return this.instrument.getExchange();
	}

	public String getSymbol() {
		return this.instrument.code();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.currency).append(this.dividend)
		        .append(this.history).append(this.instrument).append(this.quote)
		        .toHashCode();
	}

	public void print() {
		System.out.println(this.getSymbol());
		System.out.println("--------------------------------");
		for (final Field f : this.getClass().getDeclaredFields()) {
			try {
				System.out.println(f.getName() + ": " + f.get(this));
			}
			catch (final IllegalArgumentException ex) {
				Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null,
				        ex);
			}
			catch (final IllegalAccessException ex) {
				Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null,
				        ex);
			}
		}
		System.out.println("--------------------------------");
	}

	public void setCurrency(final String currency) {
		this.currency = currency;
	}

	public void setDividend(final StockDividend dividend) {
		this.dividend = dividend;
	}

	public void setHistory(final List<HistoricalQuote> history) {
		this.history = history;
	}

	public void setQuote(final StockQuote quote) {
		this.quote = quote;
	}

	@Override
	public String toString() {
		return this.getSymbol() + ": " + this.getQuote().getPrice();
	}

}
