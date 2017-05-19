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

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.NumberUtils;

import yahoofinance.histquotes.HistoricalQuote;

public abstract class CsvStockFeed extends StockFeed {

	private Optional<BigDecimal>	close;

	private String					comment;

	private Date					date;

	private Date					endDate;

	private Optional<BigDecimal>	high;

	private Instrument				instrument;

	private Optional<BigDecimal>	low;

	private Optional<BigDecimal>	open;

	private BufferedReader			reader;

	private Date					startDate;

	private Optional<BigDecimal>	volume;

	public static final Logger		log	= Logger.getLogger(CsvStockFeed.class.getName());

	protected static String formatDate(final DateFormat formatter, final Date date) {
		synchronized (formatter) {
			return formatter.format(date);
		}
	}

	public HistoricalQuote asHistoricalQuote() {
		return new HistoricalQuote(this.instrument, LocalDate.fromDateFields(this.date),
		        this.getOpen().orElse(null), this.getLow().orElse(null),
		        this.getHigh().orElse(null), this.getClose().orElse(null),
		        this.getClose().orElse(null), this.getVolume().orElse(BigDecimal.ONE).longValue(),
		        this.getComment());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) throws IOException {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
	        final LocalDate toDate) throws IOException {
		if (!this.isAvailable()) {
			CsvStockFeed.log.warning("Feed is not available");
			return Optional.empty();
		}
		this.setInstrument(instrument);
		this.setStartDate(fromDate.toDate());
		this.setEndDate(toDate.toDate());

		final List<HistoricalQuote> quotes = new LinkedList<>();
		try {
			while (this.next()) {
				quotes.add(this.asHistoricalQuote());
			}

			Collections.sort(quotes, (o1, o2) -> {
				return o2.getDate().compareTo(o1.getDate());
			});

		}
		catch (final IOException e) {
			CsvStockFeed.log.warning("Failed:" + e.getMessage());
			return Optional.empty();
		}

		return Optional.of(StockFeed.createStock(instrument, quotes));
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

	public String getComment() {
		if (StringUtils.isEmpty(this.comment)) {
			return this.getClass().getName();
		}
		return this.comment;
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
		return this.instrument.getExchange();
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

	public Instrument getInstrument() {
		return this.instrument;
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

	protected abstract String getQueryName(final Instrument instrument);

	public BufferedReader getReader() {
		return this.reader;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public String getSymbol() {
		return this.instrument.code();
	}

	/**
	 * Get volume of stock quote
	 *
	 * @see #next()
	 * @return volume
	 */
	public Optional<BigDecimal> getVolume() {
		return this.volume;
	}

	/**
	 * Advance to next stock quote in response
	 * <p>
	 * This method will open a new request on the first call and will update the fields for open,
	 * close, high, low, and volume each time it is called.
	 *
	 * @return true if another quote was parsed, false if no more quotes exist to read
	 * @throws IOException
	 *             may fail to read file
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
			if ("-".equals(input)) {
				return Optional.empty();
			}
			return Optional.of(NumberUtils.getBigDecimal(input));
		}
		catch (final NumberFormatException e) {
			CsvStockFeed.log.warning("Failed to parse " + input);
			return Optional.empty();
		}
	}

	protected Date parseDate(final String fieldValue) throws ParseException {
		return DateUtils.parseDate(fieldValue);
	}

	protected boolean parseReader(final BufferedReader reader2) throws IOException {
		try {
			String line = reader2.readLine();
			if ((line == null) || (line.length() == 0)) {
				this.release();
				return false;
			}
			final String tab = "\t";
			if (line.contains(tab)) {
				CsvStockFeed.log.warning("Messed up Csv - found tabs");
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
		}
		catch (final Exception e) {
			throw new IOException(e);
		}
	}

	public CsvStockFeed release() {
		if (this.reader != null) {
			try {
				this.reader.close();
			}
			catch (final IOException ignored) {
				// Ignored
			}
		}
		this.reader = null;
		return this;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

	public CsvStockFeed setEndDate(final Calendar endDate) {
		return this.setEndDate(endDate != null ? endDate.getTime() : null);
	}

	public CsvStockFeed setEndDate(final Date endDate) {
		this.endDate = endDate;
		return this;
	}

	public void setInstrument(final Instrument instrument) {
		this.instrument = instrument;
	}

	public CsvStockFeed setStartDate(final Calendar startDate) {
		return this.setStartDate(startDate != null ? startDate.getTime() : null);
	}

	public CsvStockFeed setStartDate(final Date startDate) {
		this.startDate = startDate;
		return this;
	}

}
