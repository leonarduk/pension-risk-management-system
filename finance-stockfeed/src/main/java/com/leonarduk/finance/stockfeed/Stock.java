package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockDividend;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockQuotesData;
import yahoofinance.quotes.stock.StockQuotesRequest;
import yahoofinance.quotes.stock.StockStats;

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

	private StockStats				stats;

	public static final Logger		logger	= Logger.getLogger(Stock.class.getName());

	public Stock(final Instrument instrument) {
		this.instrument = instrument;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Stock other = (Stock) obj;
		if (this.currency == null) {
			if (other.currency != null) {
				return false;
			}
		}
		else if (!this.currency.equals(other.currency)) {
			return false;
		}
		if (this.dividend == null) {
			if (other.dividend != null) {
				return false;
			}
		}
		else if (!this.dividend.equals(other.dividend)) {
			return false;
		}
		if (this.history == null) {
			if (other.history != null) {
				return false;
			}
		}
		else if (!this.history.equals(other.history)) {
			return false;
		}
		if (this.instrument == null) {
			if (other.instrument != null) {
				return false;
			}
		}
		else if (!this.instrument.equals(other.instrument)) {
			return false;
		}
		if (this.quote == null) {
			if (other.quote != null) {
				return false;
			}
		}
		else if (!this.quote.equals(other.quote)) {
			return false;
		}
		if (this.stats == null) {
			if (other.stats != null) {
				return false;
			}
		}
		else if (!this.stats.equals(other.stats)) {
			return false;
		}
		return true;
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
	 * Returns the dividend data available for this stock.
	 *
	 * This method will return null in the following situations:
	 * <ul>
	 * <li>the data hasn't been loaded yet in a previous request and refresh is set to false.
	 * <li>refresh is true and the data cannot be retrieved from Yahoo Finance for whatever reason
	 * (symbol not recognized, no network connection, ...)
	 * </ul>
	 * <p>
	 * When the dividend data get refreshed, it will automatically also refresh the quote and
	 * statistics data of the stock from Yahoo Finance in the same request.
	 *
	 * @param refresh
	 *            indicates whether the data should be requested again to Yahoo Finance
	 * @return dividend data available for this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public StockDividend getDividend(final boolean refresh) throws IOException {
		if (refresh) {
			this.update();
		}
		return this.dividend;
	}

	/**
	 * This method will return historical quotes from this stock. If the historical quotes are not
	 * available yet, they will be requested first from Yahoo Finance.
	 * <p>
	 * If the historical quotes are not available yet, the following characteristics will be used
	 * for the request:
	 * <ul>
	 * <li>from: 1 year ago (default)
	 * <li>to: today (default)
	 * <li>interval: MONTHLY (default)
	 * </ul>
	 * <p>
	 * There are several more methods available that allow you to define some characteristics of the
	 * historical data. Calling one of those methods will result in a new request being sent to
	 * Yahoo Finance.
	 *
	 * @return a list of historical quotes from this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 * @see #getHistory(yahoofinance.histquotes.Interval)
	 * @see #getHistory(java.util.Calendar)
	 * @see #getHistory(java.util.Calendar, java.util.Calendar)
	 * @see #getHistory(java.util.Calendar, yahoofinance.histquotes.Interval)
	 * @see #getHistory(java.util.Calendar, java.util.Calendar, yahoofinance.histquotes.Interval)
	 */
	public List<HistoricalQuote> getHistory() throws IOException {
		if (this.history == null) {
			return Lists.newArrayList();
		}
		return this.history;
	}

	/**
	 * Requests the historical quotes for this stock with the following characteristics.
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
	public List<HistoricalQuote> getHistory(final Calendar from) throws IOException {
		return this.getHistory(from, HistQuotesRequest.DEFAULT_TO);
	}

	/**
	 * Requests the historical quotes for this stock with the following characteristics.
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
	public List<HistoricalQuote> getHistory(final Calendar from, final Calendar to)
	        throws IOException {
		return this.getHistory(from, to, Interval.MONTHLY);
	}

	/**
	 * Requests the historical quotes for this stock with the following characteristics.
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
	public List<HistoricalQuote> getHistory(final Calendar from, final Calendar to,
	        final Interval interval) throws IOException {
		final HistQuotesRequest hist = new HistQuotesRequest(this.instrument, from, to, interval);
		this.setHistory(hist.getResult());
		return this.history;
	}

	/**
	 * Requests the historical quotes for this stock with the following characteristics.
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
	public List<HistoricalQuote> getHistory(final Calendar from, final Interval interval)
	        throws IOException {
		return this.getHistory(from, HistQuotesRequest.DEFAULT_TO, interval);
	}

	/**
	 * Requests the historical quotes for this stock with the following characteristics.
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
	public List<HistoricalQuote> getHistory(final Interval interval) throws IOException {
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
			return new StockQuote(this.instrument);
		}
		return this.quote;
	}

	/**
	 * Returns the basic quotes data available for this stock. This method will return null in the
	 * following situations:
	 * <ul>
	 * <li>the data hasn't been loaded yet in a previous request and refresh is set to false.
	 * <li>refresh is true and the data cannot be retrieved from Yahoo Finance for whatever reason
	 * (symbol not recognized, no network connection, ...)
	 * </ul>
	 * <p>
	 * When the quote data gets refreshed, it will automatically also refresh the statistics and
	 * dividend data of the stock from Yahoo Finance in the same request.
	 *
	 * @param refresh
	 *            indicates whether the data should be requested again to Yahoo Finance
	 * @return basic quotes data available for this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public StockQuote getQuote(final boolean refresh) throws IOException {
		if (refresh) {
			this.update();
		}
		return this.quote;
	}

	/**
	 * Returns the statistics available for this stock.
	 *
	 * @return statistics available for this stock
	 * @see #getStats(boolean)
	 */
	public StockStats getStats() {
		return this.stats;
	}

	/**
	 * Returns the statistics available for this stock. This method will return null in the
	 * following situations:
	 * <ul>
	 * <li>the data hasn't been loaded yet in a previous request and refresh is set to false.
	 * <li>refresh is true and the data cannot be retrieved from Yahoo Finance for whatever reason
	 * (symbol not recognized, no network connection, ...)
	 * </ul>
	 * <p>
	 * When the statistics get refreshed, it will automatically also refresh the quote and dividend
	 * data of the stock from Yahoo Finance in the same request.
	 *
	 * @param refresh
	 *            indicates whether the data should be requested again to Yahoo Finance
	 * @return statistics available for this stock
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public StockStats getStats(final boolean refresh) throws IOException {
		if (refresh) {
			this.update();
		}
		return this.stats;
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
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.currency == null) ? 0 : this.currency.hashCode());
		result = (prime * result) + ((this.dividend == null) ? 0 : this.dividend.hashCode());
		result = (prime * result) + ((this.history == null) ? 0 : this.history.hashCode());
		result = (prime * result) + ((this.instrument == null) ? 0 : this.instrument.hashCode());
		result = (prime * result) + ((this.quote == null) ? 0 : this.quote.hashCode());
		result = (prime * result) + ((this.stats == null) ? 0 : this.stats.hashCode());
		return result;
	}

	public void print() {
		System.out.println(this.getSymbol());
		System.out.println("--------------------------------");
		for (final Field f : this.getClass().getDeclaredFields()) {
			try {
				System.out.println(f.getName() + ": " + f.get(this));
			}
			catch (final IllegalArgumentException ex) {
				Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null, ex);
			}
			catch (final IllegalAccessException ex) {
				Logger.getLogger(Stock.class.getName()).log(Level.SEVERE, null, ex);
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

	public void setStats(final StockStats stats) {
		this.stats = stats;
	}

	@Override
	public String toString() {
		return this.getSymbol() + ": " + this.getQuote().getPrice();
	}

	private void update() throws IOException {
		final StockQuotesRequest request = new StockQuotesRequest(this.instrument);
		final StockQuotesData data = request.getSingleResult();
		if (data != null) {
			this.setQuote(data.getQuote());
			this.setStats(data.getStats());
			this.setDividend(data.getDividend());
			Stock.logger.log(Level.INFO, "Updated Stock with symbol: {0}", this.instrument.isin());
		}
		else {
			Stock.logger.log(Level.SEVERE, "Failed to update Stock with symbol: {0}",
			        this.instrument.isin());
		}
	}

}
