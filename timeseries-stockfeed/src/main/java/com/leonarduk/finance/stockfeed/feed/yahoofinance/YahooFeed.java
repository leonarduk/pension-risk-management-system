package com.leonarduk.finance.stockfeed.feed.yahoofinance;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.QuoteFeed;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.csv.FxQuotesRequest;
import yahoofinance.quotes.fx.FxQuote;
import yahoofinance.quotes.stock.StockQuote;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

// see https://financequotes-api.com/
public class YahooFeed extends AbstractStockFeed implements QuoteFeed {

    public static final Logger logger = LoggerFactory.getLogger(YahooFeed.class.getName());
    public static final String TIMEZONE = "America/New_York";

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

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) {
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate,
                                 final LocalDate toDate, boolean addLatestQuoteToTheSeries) {
        try {
            String symbol = instrument.code() + instrument.getExchange().getYahooSuffix();
            Stock stock = YahooFinance.get(symbol, true);
            return Optional
                    .of(new StockV1(stock.getCurrency(), stock.getDividend(),
                            stock.getHistory().stream().map(q -> new ExtendedHistoricalQuote(q, "Yahoo")).collect(Collectors.toList()),
                            instrument));
        } catch (final Exception e) {
            YahooFeed.logger.warn("Error when fetching from Yahoo: " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Source getSource() {
        return Source.YAHOO;
    }

    @Override
    public ExtendedStockQuote getStockQuote(final Instrument instrument) throws IOException {
        String symbol = instrument.code() + instrument.getExchange().getYahooSuffix();
        Stock stock = YahooFinance.get(symbol);
        StockQuote price = stock.getQuote();
        return new ExtendedStockQuote(price, "Yahoo");
    }

    @Override
    public boolean isAvailable() {
        return true;
//		return SeleniumUtils.isInternetAvailable("https://uk.yahoo.com");
    }

}
