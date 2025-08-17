package com.leonarduk.finance.stockfeed.feed.alphavantage;

import com.leonarduk.finance.stockfeed.*;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.ExtendedStockQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.ContinualListIterator;
import org.patriques.AlphaVantageConnector;
import org.patriques.ForeignExchange;
import org.patriques.TimeSeries;
import org.patriques.input.timeseries.OutputSize;
import org.patriques.output.exchange.Daily;
import org.patriques.output.exchange.data.ForexData;
import org.patriques.output.timeseries.DailyAdjusted;
import org.patriques.output.timeseries.data.StockData;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class AlphavantageFeed extends AbstractStockFeed implements QuoteFeed, FxFeed {

    private static final String API_KEYS_ENV_VAR = "ALPHAVANTAGE_API_KEYS";
    private static final List<String> API_KEYS = Arrays.stream(
                    Optional.ofNullable(System.getenv(API_KEYS_ENV_VAR)).orElse("").split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    private static final ContinualListIterator<String> apiKeyIterator = new ContinualListIterator<>(API_KEYS);

    public AlphavantageFeed() {
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) {
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate, boolean addLatestQuoteToTheSeries) {
        log.info(String.format("Get %s for %s to %s", instrument.getName(), fromDate.toString(), toDate.toString()));
        if (instrument instanceof FxInstrument fXInstrument) {
            return getFxSeriesInternal(fXInstrument.getCurrencyOne(), fXInstrument.getCurrencyTwo(), fromDate, toDate)
                    .map(series -> {
                        try {
                            return new StockV1(instrument, series);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        return withApiKey(connector -> {
            TimeSeries stockTimeSeries = new TimeSeries(connector);

            String symbol = instrument.code() + instrument.getExchange().getYahooSuffix();

            DailyAdjusted response = stockTimeSeries.dailyAdjusted(symbol, OutputSize.FULL);

            List<Bar> series = convertSeries(instrument, response.getStockData());
              log.info("Returning series of size {}", series.size());
            try {
                return new StockV1(instrument, series);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static final int TIMEOUT = 3000;

    private <T> Optional<T> withApiKey(Function<AlphaVantageConnector, T> action) {
        if (API_KEYS.isEmpty()) {
            log.error("No Alphavantage API keys configured. Set {} environment variable.", API_KEYS_ENV_VAR);
            return Optional.empty();
        }
        for (int i = 0; i < API_KEYS.size(); i++) {
            String apiKey = apiKeyIterator.next();
            try {
            log.debug("Using key {}", apiKey);
                AlphaVantageConnector apiConnector = new AlphaVantageConnector(apiKey, TIMEOUT);
                return Optional.ofNullable(action.apply(apiConnector));
            } catch (Exception e) {
                log.warn("Alphavantage request failed with key {}: {}", apiKey, e.getMessage());
            }
        }
        log.error("All Alphavantage API keys failed.");
        return Optional.empty();
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
        return Source.ALPHAVANTAGE;
    }

    @Override
    public ExtendedStockQuote getStockQuote(final Instrument instrument) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAvailable() {
        return !API_KEYS.isEmpty();
//		return SeleniumUtils.isInternetAvailable("https://uk.yahoo.com");
    }

    @Override
    public List<Bar> getFxSeries(String currencyOne, String currencyTwo, LocalDate fromDate, LocalDate toDate) {
        return getFxSeriesInternal(currencyOne, currencyTwo, fromDate, toDate).orElse(List.of());
    }

    private Optional<List<Bar>> getFxSeriesInternal(String currencyOne, String currencyTwo, LocalDate fromDate,
            LocalDate toDate) {
        return withApiKey(connector -> {
            ForeignExchange foreignExchange = new ForeignExchange(connector);

            Daily fxResults = foreignExchange.daily(currencyOne.toUpperCase(), currencyTwo.toUpperCase(), OutputSize.FULL);
            List<ForexData> fxData = fxResults.getForexData();

            return convertFxSeries(new FxInstrument(Source.ALPHAVANTAGE, currencyOne, currencyTwo), fxData);
        });
    }

    private List<Bar> convertFxSeries(Instrument instrument, List<ForexData> fxData) {
        return fxData.stream()
                .map(quote -> new ExtendedHistoricalQuote(instrument, quote.getDateTime().toLocalDate(),
                        quote.getOpen(), quote.getLow(), quote.getHigh(), quote.getClose(), quote.getClose(), 0,
                        "Alphavantage"))
                .collect(Collectors.toList());
    }
}
