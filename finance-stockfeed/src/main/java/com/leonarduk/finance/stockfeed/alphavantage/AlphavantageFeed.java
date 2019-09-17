package com.leonarduk.finance.stockfeed.alphavantage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.Interval;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.IntraDay;
import org.patriques.output.timeseries.data.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.QuoteFeed;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedStockQuote;
import com.leonarduk.finance.stockfeed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.yahoofinance.YahooFeed;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.csv.FxQuotesRequest;
import yahoofinance.quotes.csv.StockQuotesData;
import yahoofinance.quotes.csv.StockQuotesRequest;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.stock.StockQuote;

// see https://financequotes-api.com/
public class AlphavantageFeed extends AbstractStockFeed implements QuoteFeed {

	public static final Logger logger = LoggerFactory.getLogger(AlphavantageFeed.class.getName());
	public static final String QUOTES_CSV_DELIMITER = ",";
	public static final String TIMEZONE = "America/New_York";

	public static FxQuote getFx(final Instrument symbol) throws IOException {
		final FxQuotesRequest request = new FxQuotesRequest(symbol.code());
		return request.getSingleResult();
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final int years) {
		return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now());
	}

	@Override
	public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate) {
		try {
			String apiKey = "KKYL9UZSTHIFAMS8";
			int timeout = 3000;
			AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, timeout);
			TimeSeries stockTimeSeries = new TimeSeries(apiConnector);

			String code = instrument.code() + instrument.getExchange().getYahooSuffix();
			DailyAdjusted response = stockTimeSeries.dailyAdjusted(code, OutputSize.FULL);

			List<ExtendedHistoricalQuote> series = convertSeries(instrument, response.getStockData());
			return Optional.of(new StockV1(instrument, series));
		} catch (final Exception e) {
			YahooFeed.logger.warn("Error when fetching from Alphavantage: " + e.getMessage());
			return Optional.empty();
		}
	}

	private List<ExtendedHistoricalQuote> convertSeries(Instrument instrument, List<StockData> stockData) {
		return stockData.stream()
				.map(quote -> new ExtendedHistoricalQuote(instrument, quote.getDateTime().toLocalDate(),
						quote.getOpen(), quote.getLow(), quote.getHigh(), quote.getClose(), quote.getAdjustedClose(),
						quote.getVolume(), "Alphavantage"))
				.collect(Collectors.toList());
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
