package com.leonarduk.stockmarketview.stockfeed.google;
/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import yahoofinance.histquotes.HistoricalQuote;

/**
 * Request class for retrieving historical stock quotes from the Google Finance
 * API
 */
public class GoogleQuoteRequest {

	/** The logger */
	protected final Logger log = LoggerFactory.getLogger(getClass());
	private static final String BASE_URL = "http://www.google.com/finance/historical";

	private static final String PARAM_START_DATE = "startdate";

	private static final String PARAM_END_DATE = "enddate";

	private static final String PARAM_OUTPUT = "output";

	private static final String PARAM_SYMBOL = "q";

	private static final String OUTPUT_CSV = "csv";

	private static final String FORMAT_DATE_INPUT = "MMM'+'d'%2c+'yyyy";

	private static final String FORMAT_DATE_OUTPUT = "d'-'MMM'-'yy";

	private static final DateFormat INPUT_FORMATTER = new SimpleDateFormat(FORMAT_DATE_INPUT);

	private static final DateFormat OUTPUT_FORMATTER = new SimpleDateFormat(FORMAT_DATE_OUTPUT);

	private static String formatDate(final DateFormat formatter, final Date date) {
		synchronized (formatter) {
			return formatter.format(date);
		}
	}

	private static Date parseDate(final DateFormat formatter, final String date) throws IOException {
		try {
			synchronized (formatter) {
				return formatter.parse(date);
			}
		} catch (ParseException e) {
			IOException ioException = new IOException(e.getMessage());
			ioException.initCause(e);
			throw ioException;
		}
	}

	private String symbol;

	private Date startDate;

	private Date endDate;

	private BufferedReader reader;

	private Date date;

	private Optional<BigDecimal> open;

	private Optional<BigDecimal> high;

	private Optional<BigDecimal> low;

	private Optional<BigDecimal> close;

	private Optional<Long> volume;

	/**
	 * Get date of stock quote
	 *
	 * @see #next()
	 * @return date
	 */
	public Date getDate() {
		return date;
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
	 * Get volume of stock quote
	 *
	 * @see #next()
	 * @return volume
	 */
	public Optional<Long> getVolume() {
		return volume;
	}

	/**
	 * Set symbol of request
	 *
	 * @param symbol
	 * @return this request
	 */
	public GoogleQuoteRequest setSymbol(final String symbol) {
		this.symbol = symbol;
		return this;
	}

	/**
	 * Set start date of request
	 *
	 * @param startDate
	 * @return this request
	 */
	public GoogleQuoteRequest setStartDate(final Date startDate) {
		this.startDate = startDate;
		return this;
	}

	/**
	 * Set start date of request
	 *
	 * @param startDate
	 * @return this request
	 */
	public GoogleQuoteRequest setStartDate(final Calendar startDate) {
		return setStartDate(startDate != null ? startDate.getTime() : null);
	}

	/**
	 * Set end date of request
	 *
	 * @param endDate
	 * @return this request
	 */
	public GoogleQuoteRequest setEndDate(final Date endDate) {
		this.endDate = endDate;
		return this;
	}

	/**
	 * Set end date of request
	 *
	 * @param endDate
	 * @return this request
	 */
	public GoogleQuoteRequest setEndDate(final Calendar endDate) {
		return setEndDate(endDate != null ? endDate.getTime() : null);
	}

	/**
	 * Release the resources held by this request
	 *
	 * @return this request
	 */
	public GoogleQuoteRequest release() {
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
	 * Create request to uri
	 * <p>
	 * Sub-classes may override this method
	 *
	 * @param uri
	 * @return request
	 * @throws IOException
	 * @
	 */
	protected HttpRequest createRequest(final CharSequence uri) throws IOException {
		try {
			return HttpRequest.get(uri);
		} catch (HttpRequestException e) {
			throw e.getCause();
		}
	}

	/**
	 * Open reader to configured request parameters
	 *
	 * @return reader
	 * @throws IOException
	 * @throws HttpRequestException
	 * @
	 */
	protected BufferedReader openReader() throws IOException {
		Map<Object, Object> params = new HashMap<Object, Object>(4, 1);
		params.put(PARAM_OUTPUT, OUTPUT_CSV);
		params.put(PARAM_SYMBOL, symbol);
		if (startDate != null)
			params.put(PARAM_START_DATE, formatDate(INPUT_FORMATTER, startDate));
		if (endDate != null)
			params.put(PARAM_END_DATE, formatDate(INPUT_FORMATTER, endDate));

		final HttpRequest request = createRequest(HttpRequest.append(BASE_URL, params));
		if (!request.ok())
			throw new IOException("Bad response " + request.code());

		final BufferedReader reader;
		try {
			reader = request.bufferedReader();
		} catch (HttpRequestException e) {
			throw e.getCause();
		}
		// Skip first line that contains column names
		reader.readLine();
		return reader;
	}

	private Optional<BigDecimal> parseBigDecimal(final String input) {
		try {
			return Optional.of(BigDecimal.valueOf(Double.valueOf(input)));
		} catch (NumberFormatException e) {
			log.warn("Failed to parse " + input, e);
			return Optional.empty();
		}
	}

	private Optional<Long> parseLong(final String input) {
		try {
			return Optional.of(Long.parseLong(input));
		} catch (NumberFormatException e) {
			log.warn("Failed to parse " + input, e);
			return Optional.empty();
		}
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

		final String line = reader.readLine();
		if (line == null || line.length() == 0) {
			release();
			return false;
		}

		final int length = line.length();
		int start = 0;
		int comma = line.indexOf(',');
		int column = 0;
		while (start < length) {
			switch (column++) {
			case 0:
				date = parseDate(OUTPUT_FORMATTER, line.substring(start, comma));
				break;
			case 1:
				open = parseBigDecimal(line.substring(start, comma));
				break;
			case 2:
				high = parseBigDecimal(line.substring(start, comma));
				break;
			case 3:
				low = parseBigDecimal(line.substring(start, comma));
				break;
			case 4:
				close = parseBigDecimal(line.substring(start, comma));
				break;
			case 5:
				volume = parseLong(line.substring(start, comma));
				break;
			}
			start = comma + 1;
			comma = line.indexOf(',', start);
			if (comma == -1)
				comma = length;
		}
		return true;
	}

	public HistoricalQuote asHistoricalQuote() {
		return new HistoricalQuote(this.symbol, DateUtils.dateToCalendar(date), getOpen().orElse(null),
				getLow().orElse(null), getHigh().orElse(null), getClose().orElse(null), getClose().orElse(null),
				getVolume().get());
	}
	
}
