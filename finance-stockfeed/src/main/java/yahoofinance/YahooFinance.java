
package yahoofinance;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.leonarduk.finance.stockfeed.Instrument;

import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.fx.FxQuotesRequest;
import yahoofinance.quotes.stock.StockQuotesData;
import yahoofinance.quotes.stock.StockQuotesRequest;

/**
 * YahooFinance can be used to retrieve quotes and some extra information on
 * stocks. There is also the possibility to include historical quotes on the
 * requested stocks.
 * <p>
 * When trying to get information on multiple stocks at once, please use the
 * provided methods that accept a <code>String[]</code> of symbols to get the
 * best performance. To retrieve the basic quote, statistics and dividend data,
 * a single request can be sent to Yahoo Finance for multiple stocks at once.
 * For the historical data however, a separate request has to be sent to Yahoo
 * Finance for each of the requested stocks. The provided methods will retrieve
 * all of the required information in the least amount of requests possible
 * towards Yahoo Finance.
 * <p>
 * You can change the default timeout of 10s for requests to Yahoo Finance by
 * setting the yahoofinance.connection.timeout system property.
 * <p>
 * Please be aware that the data received from Yahoo Finance is not always
 * complete for every single stock. Stocks on the American stock exchanges
 * usually have a lot more data available than stocks on other exchanges.
 * <p>
 * This API can also be used to send requests for retrieving FX rates.
 * <p>
 * Since the data is provided by Yahoo, please check their Terms of Service at
 * https://info.yahoo.com/legal/us/yahoo/
 *
 * @author Stijn Strickx
 * @version %I%, %G%
 */
public class YahooFinance {

	public static final String QUOTES_BASE_URL = System.getProperty("yahoofinance.baseurl.quotes",
			"http://finance.yahoo.com/d/quotes.csv");
	public static final String HISTQUOTES_BASE_URL = System.getProperty("yahoofinance.baseurl.histquotes",
			"http://ichart.yahoo.com/table.csv");
	public static final String QUOTES_CSV_DELIMITER = ",";
	public static final String TIMEZONE = "America/New_York";

	public static final int CONNECTION_TIMEOUT = Integer
			.parseInt(System.getProperty("yahoofinance.connection.timeout", "10000"));

	public static final Logger logger = Logger.getLogger(YahooFinance.class.getName());

	private static Map<String, Stock> fetchHistoricalQuotes(final Map<String, Stock> stocks, final Calendar from,
			final Calendar to, final Interval interval) throws IOException {
		for (final Stock s : stocks.values()) {
			s.getHistory(from, to, interval);
		}
		return stocks;
	}

	public static Stock get(final Instrument instrument) throws IOException {
		return get(instrument, HistQuotesRequest.DEFAULT_FROM);
	}

	/**
	 * Sends a request with the historical quotes included starting from the
	 * specified {@link Calendar} date at the default interval (monthly).
	 * Returns null if the data can't be retrieved from Yahoo Finance.
	 *
	 * @param symbol
	 *            the symbol of the stock for which you want to retrieve
	 *            information
	 * @param from
	 *            start date of the historical data
	 * @return a {@link Stock} object containing the requested information
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static Stock get(final Instrument instrument, final Calendar from) throws IOException {
		return YahooFinance.get(instrument, from, HistQuotesRequest.DEFAULT_TO, HistQuotesRequest.DEFAULT_INTERVAL);
	}

	/**
	 * Sends a request with the historical quotes included starting from the
	 * specified {@link Calendar} date until the specified Calendar date (to) at
	 * the default interval (monthly). Returns null if the data can't be
	 * retrieved from Yahoo Finance.
	 *
	 * @param symbol
	 *            the symbol of the stock for which you want to retrieve
	 *            information
	 * @param from
	 *            start date of the historical data
	 * @param to
	 *            end date of the historical data
	 * @return a {@link Stock} object containing the requested information
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static Stock get(final Instrument instrument, final Calendar from, final Calendar to) throws IOException {
		return YahooFinance.get(instrument, from, to, HistQuotesRequest.DEFAULT_INTERVAL);
	}

	/**
	 * Sends a request with the historical quotes included starting from the
	 * specified {@link Calendar} date until the specified Calendar date (to) at
	 * the specified interval. Returns null if the data can't be retrieved from
	 * Yahoo Finance.
	 *
	 * @param symbol
	 *            the symbol of the stock for which you want to retrieve
	 *            information
	 * @param from
	 *            start date of the historical data
	 * @param to
	 *            end date of the historical data
	 * @param interval
	 *            the interval of the included historical data
	 * @return a {@link Stock} object containing the requested information
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static Stock get(final Instrument instrument, final Calendar from, final Calendar to,
			final Interval interval) throws IOException {
		final Map<String, Stock> result = YahooFinance.getQuotes(instrument, from, to, interval);
		return result.get(instrument);
	}

	/**
	 * Sends a request with the historical quotes included starting from the
	 * specified {@link Calendar} date at the specified interval. Returns null
	 * if the data can't be retrieved from Yahoo Finance.
	 *
	 * @param symbol
	 *            the symbol of the stock for which you want to retrieve
	 *            information
	 * @param from
	 *            start date of the historical data
	 * @param interval
	 *            the interval of the included historical data
	 * @return a {@link Stock} object containing the requested information
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static Stock get(final Instrument instrument, final Calendar from, final Interval interval)
			throws IOException {
		return YahooFinance.get(instrument, from, HistQuotesRequest.DEFAULT_TO, interval);
	}

	/**
	 * Sends a request with the historical quotes included at the specified
	 * interval (DAILY, WEEKLY, MONTHLY). Returns null if the data can't be
	 * retrieved from Yahoo Finance.
	 *
	 * @param symbol
	 *            the symbol of the stock for which you want to retrieve
	 *            information
	 * @param interval
	 *            the interval of the included historical data
	 * @return a {@link Stock} object containing the requested information
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public static Stock get(final Instrument instrument, final Interval interval) throws IOException {
		return YahooFinance.get(instrument, HistQuotesRequest.DEFAULT_FROM, HistQuotesRequest.DEFAULT_TO, interval);
	}

	/**
	 * Sends a request for a single FX rate. Some common symbols can easily be
	 * found in the ENUM {@link yahoofinance.quotes.fx.FxSymbols} Some examples
	 * of accepted symbols:
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

	private static Map<String, Stock> getQuotes(final Instrument instrument, final boolean includeHistorical)
			throws IOException {
		final StockQuotesRequest request = new StockQuotesRequest(instrument);
		final List<StockQuotesData> quotes = request.getResult();
		final Map<String, Stock> result = new HashMap<>();

		for (final StockQuotesData data : quotes) {
			final Stock s = data.getStock();
			result.put(s.getSymbol(), s);
		}

		if (includeHistorical) {
			for (final Stock s : result.values()) {
				s.getHistory();
			}
		}

		return result;
	}

	private static Map<String, Stock> getQuotes(final Instrument instrument, final Calendar from, final Calendar to,
			final Interval interval) throws IOException {
		Map<String, Stock> stocks = YahooFinance.getQuotes(instrument, false);
		stocks = YahooFinance.fetchHistoricalQuotes(stocks, from, to, interval);
		return stocks;
	}

}
