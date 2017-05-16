package com.leonarduk.finance.stockfeed.yahoo;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.DateUtils;

import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.fx.FxQuotesRequest;
import yahoofinance.quotes.stock.StockQuote;

public class YahooFeed extends StockFeed {

	public static final int		CONNECTION_TIMEOUT		= Integer
	        .parseInt(System.getProperty("connection.timeout", "10000"));

	public static final String	HISTQUOTES_BASE_URL		= System.getProperty("baseurl.histquotes",
	        "http://ichart.yahoo.com/table.csv");

	public static final Logger	logger					= Logger
	        .getLogger(YahooFeed.class.getName());
	public static final String	QUOTES_BASE_URL			= System.getProperty("baseurl.quotes",
	        "http://finance.yahoo.com/d/quotes.csv");
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
	 * Sends a request for a single FX rate. Some common symbols can easily be found in the ENUM
	 * {@link quotes.fx.FxSymbols} Some examples of accepted symbols:
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

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
	        final LocalDate toDate) {
		try {
			final Stock stock = new Stock(instrument);

			stock.getHistory(DateUtils.dateToCalendar(fromDate),
			        DateUtils.dateToCalendar(toDate.toDate()), Interval.DAILY);
			final StockQuote quote = stock.getQuote();
			stock.getHistory()
			        .add(new HistoricalQuote(stock.getInstrument(),
			                LocalDate.fromCalendarFields(quote.getLastTradeTime()), quote.getOpen(),
			                quote.getDayLow(), quote.getDayHigh(), quote.getPrice(),
			                quote.getPrice(), quote.getVolume(), "Yahoo"));
			return Optional.of(stock);
		}
		catch (final Exception e) {
			return Optional.empty();
		}
	}

}
