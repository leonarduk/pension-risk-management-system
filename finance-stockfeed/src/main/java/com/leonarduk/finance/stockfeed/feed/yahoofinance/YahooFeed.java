package com.leonarduk.finance.stockfeed.feed.yahoofinance;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.QuoteFeed;
import com.leonarduk.finance.stockfeed.Source;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.csv.FxQuotesRequest;
import yahoofinance.quotes.csv.StockQuotesData;
import yahoofinance.quotes.csv.StockQuotesRequest;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.stock.StockQuote;

// see https://financequotes-api.com/
public class YahooFeed extends AbstractStockFeed implements QuoteFeed {

	public static final Logger logger = LoggerFactory.getLogger(YahooFeed.class.getName());
	public static final String QUOTES_CSV_DELIMITER = ",";
	public static final String TIMEZONE = "America/New_York";

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
	 * @param symbol symbol for the FX rate you want to request
	 * @return a quote for the requested FX rate
	 * @throws java.io.IOException when there's a connection problem
	 */
	public static FxQuote getFx(final Instrument symbol) throws IOException {
		final FxQuotesRequest request = new FxQuotesRequest(symbol.code());
		return request.getSingleResult();
	}

	public static String getQueryName(final Instrument instrument) {
		switch (instrument.getExchange()) {
		case London:
			return YahooFeed.getCode(instrument) + ".L";
		default:
			throw new IllegalArgumentException("Don't know how to handle " + instrument.getExchange());
		}
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now());
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate,
			final LocalDate toDate) {
		try {
			String symbol = instrument.code() + instrument.getExchange().getYahooSuffix();
			return Optional
					.of(new StockV1(YahooFinance.get(symbol, true)));
		} catch (final Exception e) {
			YahooFeed.logger.warn("Error when fetching from Yahoo: " + e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public Source getSource() {
		return Source.Yahoo;
	}

	@Override
	public ExtendedStockQuote getStockQuote(final Instrument instrument) throws IOException {
		Stock stock = YahooFinance.get(instrument.code() + instrument.getExchange().getYahooSuffix());
		StockQuote price = stock.getQuote();
		return new ExtendedStockQuote(price);
	}

	public StockQuotesData getStockQuotesData(final Instrument instrument) throws IOException {
		final StockQuotesRequest request = new StockQuotesRequest(instrument.code());
		return request.getSingleResult();
	}

	@Override
	public boolean isAvailable() {
		return true;
//		return SeleniumUtils.isInternetAvailable("https://uk.yahoo.com");
	}

}
