package com.leonarduk.finance.stockfeed.file;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.ComparableHistoricalQuote;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.google.DateUtils;

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

	private EXCHANGE exchange;

	public HistoricalQuote asHistoricalQuote() {
		return new ComparableHistoricalQuote(this.symbol, DateUtils.dateToCalendar(date), getOpen().orElse(null),
				getLow().orElse(null), getHigh().orElse(null), getClose().orElse(null), getClose().orElse(null),
				getVolume().orElse(0L));
	}

	/**
	 * Get close price of stock quote
	 *
	 * @see #next()
	 * @return close
	 */
	public Optional<BigDecimal> getClose() {
		return close;
	}

	/**
	 * Get date of stock quote
	 *
	 * @see #next()
	 * @return date
	 */
	public Date getDate() {
		return date;
	}

	public Date getEndDate() {
		return endDate;
	}

	/**
	 * Get high price of stock quote
	 *
	 * @see #next()
	 * @return high
	 */
	public Optional<BigDecimal> getHigh() {
		return high;
	}

	/**
	 * Get low price of stock quote
	 *
	 * @see #next()
	 * @return low
	 */
	public Optional<BigDecimal> getLow() {
		return low;
	}

	/**
	 * Get open price of stock quote
	 *
	 * @see #next()
	 * @return open
	 */
	public Optional<BigDecimal> getOpen() {
		return open;
	}

	public BufferedReader getReader() {
		return reader;
	}

	public Date getStartDate() {
		return startDate;
	}

	public String getSymbol() {
		return symbol;
	}

	public EXCHANGE getExchange() {
		return exchange;
	}

	/**
	 * Get volume of stock quote
	 *
	 * @see #next()
	 * @return volume
	 */
	public Optional<Long> getVolume() {
		return volume;
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
		if (reader == null)
			reader = openReader();

		return parseReader(reader);
	}

	protected abstract BufferedReader openReader() throws IOException;

	private Optional<BigDecimal> parseBigDecimal(final String input) {
		try {
			if (input.equals("-"))
				return Optional.empty();
			return Optional.of(BigDecimal.valueOf(Double.valueOf(input)));
		} catch (NumberFormatException e) {
			log.warning("Failed to parse " + input);
			return Optional.empty();
		}
	}

	private Optional<Long> parseLong(final String input) {
		try {
			return Optional.of(Long.parseLong(input));
		} catch (NumberFormatException e) {
			log.warning("Failed to parse " + input);
			return Optional.empty();
		}
	}

	protected abstract String getQueryName(StockFeed.EXCHANGE exchange, String ticker);

	@Override
	public Optional<Stock> get(EXCHANGE exchange, String ticker, int years) throws IOException {
		this.setSymbol(ticker);
		this.setExchange(exchange);
		Calendar from = Calendar.getInstance();
		from.add(Calendar.YEAR, -1 * years);
		this.setStartDate(from);
		this.setEndDate(Calendar.getInstance());

		List<HistoricalQuote> quotes = new LinkedList<>();
		try {
			while (this.next()) {
				quotes.add(this.asHistoricalQuote());
			}
		} catch (IOException e) {
			return Optional.empty();
		}

		return Optional.of(createStock(exchange, ticker, ticker, quotes));
	}

	public void setExchange(EXCHANGE exchange2) {
		this.exchange = exchange2;
	}

	protected boolean parseReader(BufferedReader reader2) throws IOException {
		try {
			String line = reader2.readLine();
			if (line == null || line.length() == 0) {
				release();
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
				String fieldValue = line.substring(start, comma);
				switch (column++) {
				case 0:
					date = parseDate(fieldValue);
					break;
				case 1:
					open = parseBigDecimal(fieldValue);
					break;
				case 2:
					high = parseBigDecimal(fieldValue);
					break;
				case 3:
					low = parseBigDecimal(fieldValue);
					break;
				case 4:
					close = parseBigDecimal(fieldValue);
					break;
				case 5:
					volume = parseLong(fieldValue);
					break;
				}
				start = comma + 1;
				comma = line.indexOf(',', start);
				if (comma == -1)
					comma = length;
			}
			return true;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	protected Date parseDate(String fieldValue) throws ParseException {
		return LocalDate.parse(fieldValue).toDate();
	}

	/**
	 * Release the resources held by this request
	 *
	 * @return this request
	 */
	public CsvStockFeed release() {
		if (reader != null)
			try {
				reader.close();
			} catch (IOException ignored) {
				// Ignored
			}
		reader = null;
		return this;
	}

	/**
	 * Set end date of request
	 *
	 * @param endDate
	 * @return this request
	 */
	public CsvStockFeed setEndDate(final Calendar endDate) {
		return setEndDate(endDate != null ? endDate.getTime() : null);
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

	/**
	 * Set start date of request
	 *
	 * @param startDate
	 * @return this request
	 */
	public CsvStockFeed setStartDate(final Calendar startDate) {
		return setStartDate(startDate != null ? startDate.getTime() : null);
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
