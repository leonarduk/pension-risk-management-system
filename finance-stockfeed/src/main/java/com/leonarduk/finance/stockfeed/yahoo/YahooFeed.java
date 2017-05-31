package com.leonarduk.finance.stockfeed.yahoo;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.QuoteFeed;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.web.SeleniumUtils;

import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.fx.FxQuotesRequest;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockQuotesData;
import yahoofinance.quotes.stock.StockQuotesRequest;

public class YahooFeed extends StockFeed implements QuoteFeed {

	public static final int		CONNECTION_TIMEOUT		= Integer
	        .parseInt(System.getProperty("connection.timeout", "10000"));

	public static final String	HISTQUOTES_BASE_URL		= System.getProperty(
	        "baseurl.histquotes", "http://ichart.yahoo.com/table.csv");

	public static final Logger	logger					= Logger
	        .getLogger(YahooFeed.class.getName());
	public static final String	QUOTES_BASE_URL			= System.getProperty(
	        "baseurl.quotes", "http://finance.yahoo.com/d/quotes.csv");
	public static final String	QUOTES_CSV_DELIMITER	= ",";
	public static final String	TIMEZONE				= "America/New_York";

	private static String getCode(final Instrument instrument) {
		switch (instrument.getAssetType()) {
			case FUND:
				return instrument.getIsin();
			default:
				return instrument.code();
		}
	}

	/**
	 * Some examples of accepted symbols:
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
				return YahooFeed.getCode(instrument) + ".L";
			default:
				throw new IllegalArgumentException(
				        "Don't know how to handle " + instrument.getExchange());
		}
	}

	// if (data != null) {
	// this.setQuote(data.getQuote());
	// this.setStats(data.getStats());
	// this.setDividend(data.getDividend());
	// Stock.logger.log(Level.INFO, "Updated Stock with symbol: {0}", this.instrument.isin());
	// }
	// else {
	// Stock.logger.log(Level.SEVERE, "Failed to update Stock with symbol: {0}",
	// this.instrument.isin());
	// }
	// }

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years),
		        LocalDate.now());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate) {
		try {
			final Stock stock = new Stock(instrument);

			if (!this.isAvailable()) {
				YahooFeed.logger.warning("Cannot connect to Yahoo");
				return Optional.empty();
			}
			stock.getHistory(DateUtils.dateToCalendar(fromDate),
			        DateUtils.dateToCalendar(toDate.toDate()), Interval.DAILY);
			return Optional.of(stock);
		}
		catch (final Exception e) {
			YahooFeed.logger.warning(
			        "Error when fetching from Yahoo: " + e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public Source getSource() {
		return Source.Yahoo;
	}

	@Override
	public StockQuote getStockQuote(final Instrument instrument)
	        throws IOException {
		return this.getStockQuotesData(instrument).getQuote();
	}

	public StockQuotesData getStockQuotesData(final Instrument instrument)
	        throws IOException {
		final StockQuotesRequest request = new StockQuotesRequest(instrument);
		return request.getSingleResult();
	}

	@Override
	public boolean isAvailable() {
		return SeleniumUtils.isInternetAvailable(YahooFeed.QUOTES_BASE_URL);
	}

}
