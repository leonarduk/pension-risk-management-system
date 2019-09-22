package com.leonarduk.finance.stockfeed.feed.alphavantage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.patriques.AlphaVantageConnector;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.data.StockData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.QuoteFeed;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.ExtendedStockQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

public class AlphavantageFeed extends AbstractStockFeed implements QuoteFeed {

	public static final Logger logger = LoggerFactory.getLogger(AlphavantageFeed.class.getName());
	public static final String QUOTES_CSV_DELIMITER = ",";
	public static final String TIMEZONE = "America/New_York";

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

			List<Bar> series = convertSeries(instrument, response.getStockData());
			return Optional.of(new StockV1(instrument, series));
		} catch (final Exception e) {
			logger.warn("Error when fetching from Alphavantage: " + e.getMessage());
			return Optional.empty();
		}
	}

	private List<Bar> convertSeries(Instrument instrument, List<StockData> stockData) {
		return stockData.stream()
				.map(quote -> new ExtendedHistoricalQuote(instrument, quote.getDateTime().toLocalDate(),
						quote.getOpen(), quote.getLow(), quote.getHigh(), quote.getClose(), quote.getAdjustedClose(),
						quote.getVolume(), "Alphavantage"))
				.collect(Collectors.toList());
	}

	@Override
	public Source getSource() {
		return Source.Alphavantage;
	}

	@Override
	public ExtendedStockQuote getStockQuote(final Instrument instrument) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAvailable() {
		return true;
//		return SeleniumUtils.isInternetAvailable("https://uk.yahoo.com");
	}

}
