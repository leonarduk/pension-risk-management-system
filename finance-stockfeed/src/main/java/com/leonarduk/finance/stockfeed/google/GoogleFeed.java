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
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.file.CsvStockFeed;
import com.leonarduk.web.SeleniumUtils;

public class GoogleFeed extends CsvStockFeed {
	private static final String		BASE_URL			= "http://www.google.com/finance/historical";
	/** The logger */
	public static final Logger		log					= Logger
	        .getLogger(GoogleFeed.class.getName());

	private static final String		OUTPUT_CSV			= "csv";

	private static final String		PARAM_END_DATE		= "enddate";

	private static final DateFormat	PARAM_FORMATTER		= new SimpleDateFormat(
	        "MMM'+'d'%2c+'yyyy");

	private static final String		PARAM_OUTPUT		= "output";

	private static final String		PARAM_START_DATE	= "startdate";

	private static final String		PARAM_SYMBOL		= "q";
	private static final DateFormat	RESULT_FORMATTER	= new SimpleDateFormat(
	        "dd-MMM-yy");

	protected HttpRequest createRequest(final CharSequence uri)
	        throws IOException {
		try {
			GoogleFeed.log.info("Request: " + uri);
			return HttpRequest.get(uri);
		}
		catch (final HttpRequestException e) {
			throw e.getCause();
		}
	}

	@Override
	protected String getQueryName(final Instrument instrument) {
		return instrument.getGoogleCode();
	}

	@Override
	public Source getSource() {
		return Source.Google;
	}

	@Override
	public boolean isAvailable() {
		return SeleniumUtils.isInternetAvailable(GoogleFeed.BASE_URL);
	}

	@Override
	protected BufferedReader openReader() throws IOException {
		final Map<Object, Object> params = new HashMap<>(4, 1);
		params.put(GoogleFeed.PARAM_OUTPUT, GoogleFeed.OUTPUT_CSV);
		params.put(GoogleFeed.PARAM_SYMBOL,
		        Instrument.fromString(this.getSymbol()).getGoogleCode());
		if (this.getStartDate() != null) {
			params.put(GoogleFeed.PARAM_START_DATE, CsvStockFeed.formatDate(
			        GoogleFeed.PARAM_FORMATTER, this.getStartDate()));
		}
		if (this.getEndDate() != null) {
			params.put(GoogleFeed.PARAM_END_DATE, CsvStockFeed
			        .formatDate(GoogleFeed.PARAM_FORMATTER, this.getEndDate()));
		}

		final HttpRequest request = this
		        .createRequest(HttpRequest.append(GoogleFeed.BASE_URL, params));
		if (!request.ok()) {
			throw new IOException("Bad response " + request.code());
		}

		final BufferedReader reader;
		try {
			reader = request.bufferedReader();
		}
		catch (final HttpRequestException e) {
			throw e.getCause();
		}
		// Skip first line that contains column names
		reader.readLine();
		return reader;
	}

	@Override
	protected Date parseDate(final String fieldValue) throws ParseException {
		return GoogleFeed.RESULT_FORMATTER.parse(fieldValue);
	}
}
