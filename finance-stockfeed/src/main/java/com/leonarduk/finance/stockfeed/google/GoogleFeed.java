package com.leonarduk.finance.stockfeed.google;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.file.CsvStockFeed;

public class GoogleFeed extends CsvStockFeed {
	/** The logger */
	public static final Logger log = Logger.getLogger(GoogleFeed.class.getName());
	private static final String BASE_URL = "http://www.google.com/finance/historical";

	private static final String PARAM_START_DATE = "startdate";

	private static final String PARAM_END_DATE = "enddate";

	private static final String PARAM_OUTPUT = "output";

	private static final String PARAM_SYMBOL = "q";

	private static final String OUTPUT_CSV = "csv";


	private static final DateFormat PARAM_FORMATTER = new SimpleDateFormat("MMM'+'d'%2c+'yyyy");
	private static final DateFormat RESULT_FORMATTER = new SimpleDateFormat("dd-MMM-yy");

	@Override
	protected Date parseDate(String fieldValue) throws ParseException {
		return RESULT_FORMATTER.parse(fieldValue);
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
			log.info("Request: " + uri);
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
	@Override
	protected BufferedReader openReader() throws IOException {
		Map<Object, Object> params = new HashMap<Object, Object>(4, 1);
		params.put(PARAM_OUTPUT, OUTPUT_CSV);
		params.put(PARAM_SYMBOL, Instrument.fromString(getSymbol()).getGoogleCode());
		if (getStartDate() != null)
			params.put(PARAM_START_DATE, formatDate(PARAM_FORMATTER, getStartDate()));
		if (getEndDate() != null)
			params.put(PARAM_END_DATE, formatDate(PARAM_FORMATTER, getEndDate()));

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

	@Override
	protected String getQueryName(StockFeed.Exchange exchange, String ticker) {
		switch (exchange) {
		case London:
			return "LON:" + ticker;
		}
		throw new IllegalArgumentException("Don't know how to handle " + exchange);
	}
}
