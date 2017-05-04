package com.leonarduk.finance.stockfeed.yahoo;

import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Logger;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.fx.FxQuotesRequest;
import yahoofinance.quotes.stock.StockQuote;

public class YahooFeed extends StockFeed {

	public static final String QUOTES_BASE_URL = System.getProperty("baseurl.quotes",
			"http://finance.yahoo.com/d/quotes.csv");

	public static final String HISTQUOTES_BASE_URL = System.getProperty("baseurl.histquotes",
			"http://ichart.yahoo.com/table.csv");

	public static final String QUOTES_CSV_DELIMITER = ",";
	public static final String TIMEZONE = "America/New_York";
	public static final int CONNECTION_TIMEOUT = Integer.parseInt(System.getProperty("connection.timeout", "10000"));
	public static final Logger logger = Logger.getLogger(YahooFeed.class.getName());

	private static String getCode(final Instrument instrument) {
		switch (instrument.getAssetType()) {
		case FUND:
			return instrument.getIsin();
		default:
			return instrument.code();
		}
	}

	/**
	 * Sends a request for a single FX rate. Some common symbols can easily be
	 * found in the ENUM {@link quotes.fx.FxSymbols} Some examples of accepted
	 * symbols:
	 * <ul>
	 * <li>EURUSD=X
	 * <li>USDEUR=X
	 * <li>USDGBP=X
	 * <li>AUDGBP=X
	 * <li>CADUSD=X
	 * </ul>
	 *
	 * @param symbol
	 *            symbol for the FX rate you want to request
	 * @return a quote for the requested FX rate
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static FxQuote getFx(final Instrument symbol) throws IOException {
		final FxQuotesRequest request = new FxQuotesRequest(symbol);
		return request.getSingleResult();
	}

	public static String getQueryName(final Instrument instrument) {
		switch (instrument.getExchange()) {
		case London:
			return getCode(instrument) + ".L";
		}
		throw new IllegalArgumentException("Don't know how to handle " + instrument.getExchange());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		try {
			final Calendar from = Calendar.getInstance();

			from.add(Calendar.YEAR, -1 * years);
			final Stock stock = new Stock(instrument);

			stock.getHistory(from, HistQuotesRequest.DEFAULT_TO, Interval.DAILY);
			final StockQuote quote = stock.getQuote();
			stock.getHistory().add(new HistoricalQuote(stock.getInstrument(), quote.getLastTradeTime(), quote.getOpen(),
					quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(), quote.getVolume()));
			return Optional.of(stock);
		} catch (final Exception e) {
			return Optional.empty();
		}
	}

}
