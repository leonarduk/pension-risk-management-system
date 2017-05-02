package com.leonarduk.finance.stockfeed.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.ComparableHistoricalQuote;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.DateUtils;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public abstract class CsvStockFeed extends StockFeed {

	public static final Logger log = Logger.getLogger(CsvStockFeed.class.getName());

	protected static String formatDate(final DateFormat formatter, final Date date) {
		synchronized (formatter) {
			return formatter.format(date);
		}
	}

	private Optional<BigDecimal> close;

	private Date date;

	private Date endDate;

	private Optional<BigDecimal> high;

	private Optional<BigDecimal> low;

	private Optional<BigDecimal> open;

	private BufferedReader reader;

	private Date startDate;

	private String symbol;

	private Optional<Long> volume;

	private Exchange exchange;

	public HistoricalQuote asHistoricalQuote() {
		return new ComparableHistoricalQuote(this.symbol, DateUtils.dateToCalendar(this.date),
				this.getOpen().orElse(null), this.getLow().orElse(null), this.getHigh().orElse(null),
				this.getClose().orElse(null), this.getClose().orElse(null), this.getVolume().orElse(0L));
	}

	@Override
	public Optional<Stock> get(final Exchange exchange, final String ticker, final int years) throws IOException {
		this.setSymbol(ticker);
		this.setExchange(exchange);
		final Calendar from = Calendar.getInstance();
		from.add(Calendar.YEAR, -1 * years);
		this.setStartDate(from);
		this.setEndDate(Calendar.getInstance());

		final List<HistoricalQuote> quotes = new LinkedList<>();
		try {
			while (this.next()) {
				quotes.add(this.asHistoricalQuote());
			}

			Collections.sort(quotes, (o1, o2) -> {
				return o2.getDate().compareTo(o1.getDate());
			});

		} catch (final IOException e) {
			return Optional.empty();
		}

		return Optional.of(createStock(exchange, ticker, ticker, quotes));
	}

	/**
	 * Get close price of stock quote
	 *
	 * @see #next()
	 * @return close
	 */
	public Optional<BigDecimal> getClose() {
		return this.close;
	}

	/**
	 * Get date of stock quote
	 *
	 * @see #next()
	 * @return date
	 */
	public Date getDate() {
		return this.date;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public Exchange getExchange() {
		return this.exchange;
	}

	/**
	 * Get high price of stock quote
	 *
	 * @see #next()
	 * @return high
	 */
	public Optional<BigDecimal> getHigh() {
		return this.high;
	}

	/**
	 * Get low price of stock quote
	 *
	 * @see #next()
	 * @return low
	 */
	public Optional<BigDecimal> getLow() {
		return this.low;
	}

	/**
	 * Get open price of stock quote
	 *
	 * @see #next()
	 * @return open
	 */
	public Optional<BigDecimal> getOpen() {
		return this.open;
	}

	protected abstract String getQueryName(StockFeed.Exchange exchange, String ticker);

	public BufferedReader getReader() {
		return this.reader;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public String getSymbol() {
		return this.symbol;
	}

	/**
	 * Get volume of stock quote
	 *
	 * @see #next()
	 * @return volume
	 */
	public Optional<Long> getVolume() {
		return this.volume;
	}

	/**
	 * Advance to next stock quote in response
	 * <p>
	 * This method will open a new request on the first call and will update the
	 * fields for open, close, high, low, and volume each time it is called.
	 *
	 * @return true if another quote was parsed, false if no more quotes exist
	 *         to read
	 * @throws IOException
	 * @
	 */
	public boolean next() throws IOException {
		if (this.reader == null) {
			this.reader = this.openReader();
		}

		return this.parseReader(this.reader);
	}

	protected abstract BufferedReader openReader() throws IOException;

	private Optional<BigDecimal> parseBigDecimal(final String input) {
		try {
			if (input.equals("-")) {
				return Optional.empty();
			}
			return Optional.of(BigDecimal.valueOf(Double.valueOf(input)));
		} catch (final NumberFormatException e) {
			log.warning("Failed to parse " + input);
			return Optional.empty();
		}
	}

	protected Date parseDate(final String fieldValue) throws ParseException {
		return LocalDate.parse(fieldValue).toDate();
	}

	private Optional<Long> parseLong(final String input) {
		try {
			return Optional.of(Long.parseLong(input));
		} catch (final NumberFormatException e) {
			log.warning("Failed to parse " + input);
			return Optional.empty();
		}
	}

	protected boolean parseReader(final BufferedReader reader2) throws IOException {
		try {
			String line = reader2.readLine();
			if ((line == null) || (line.length() == 0)) {
				this.release();
				return false;
			}
			if (line.contains("\t")) {
				log.warning("Messed up Csv - found tabs");
				line = line.replace("\t", ",");
			}

			final int length = line.length();
			int start = 0;
			int comma = line.indexOf(',');
			int column = 0;
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
					this.volume = this.parseLong(fieldValue);
					break;
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

	/**
	 * Release the resources held by this request
	 *
	 * @return this request
	 */
	public CsvStockFeed release() {
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

	/**
	 * Set end date of request
	 *
	 * @param endDate
	 * @return this request
	 */
	public CsvStockFeed setEndDate(final Calendar endDate) {
		return this.setEndDate(endDate != null ? endDate.getTime() : null);
	}

	/**
	 * Set end date of request
	 *
	 * @param endDate
	 * @return this request
	 */
	public CsvStockFeed setEndDate(final Date endDate) {
		this.endDate = endDate;
		return this;
	}

	public void setExchange(final Exchange exchange2) {
		this.exchange = exchange2;
	}

	/**
	 * Set start date of request
	 *
	 * @param startDate
	 * @return this request
	 */
	public CsvStockFeed setStartDate(final Calendar startDate) {
		return this.setStartDate(startDate != null ? startDate.getTime() : null);
	}

	/**
	 * Set start date of request
	 *
	 * @param startDate
	 * @return this request
	 */
	public CsvStockFeed setStartDate(final Date startDate) {
		this.startDate = startDate;
		return this;
	}

	/**
	 * Set symbol of request
	 *
	 * @param symbol
	 * @return this request
	 */
	public CsvStockFeed setSymbol(final String symbol) {
		this.symbol = symbol;
		return this;
	}

}
