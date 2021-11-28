package com.leonarduk.finance.stockfeed;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.datatransformation.interpolation.FlatLineInterpolator;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.ExtendedStockQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockQuoteBuilder;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.DateUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;
import org.ta4j.core.num.DoubleNum;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

public class IntelligentStockFeed extends AbstractStockFeed implements StockFeed {
    public static final Logger log = LoggerFactory.getLogger(IntelligentStockFeed.class.getName());

    private final DataStore dataStore;
    private final StockFeedFactory stockFeedFactory;
    public boolean refresh = true;
    private final Set<String> previousQueries;

    public IntelligentStockFeed(final DataStore dataStore) {
        this.dataStore = dataStore;
        stockFeedFactory = new StockFeedFactory(dataStore);
        previousQueries = new HashSet<>();
    }

    public Optional<StockV1> getFlatCashSeries(final Instrument instrument, final int years) throws IOException {
        return getFlatCashSeries(instrument, LocalDate.now().minusYears(years), LocalDate.now());
    }

    public Optional<StockV1> getFlatCashSeries(final Instrument instrument, final LocalDate fromDate,
                                               final LocalDate toDate) throws IOException {
        final StockV1 cash = new StockV1(instrument);
        final List<Bar> history = Lists.newArrayList();
        history.add(new ExtendedHistoricalQuote(instrument.getCode(), toDate, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, DoubleNum.valueOf(0l), "Manually created"));

        final FlatLineInterpolator flatLineInterpolator = new FlatLineInterpolator();
        cash.setHistory(flatLineInterpolator.extendToFromDate(history, fromDate));
        cash.setQuote(new StockQuoteBuilder(instrument).setPrice(DoubleNum.valueOf(1)).build());
        return Optional.of(cash);
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public void addLatestQuoteToTheSeries(final StockV1 stock, final QuoteFeed dataFeed) throws IOException {
        // Add latest price to the series
        if ((dataFeed != null) && dataFeed.isAvailable()) {
            final ExtendedStockQuote quote = dataFeed.getStockQuote(stock.getInstrument());
            if ((quote != null) && quote.isPopulated()) {
                LocalDate calendarToLocalDate = DateUtils.calendarToLocalDate(quote.getLastTradeTime());
                if (stock.getHistory().stream()
                        .filter(dataPoint -> dataPoint.getEndTime().toLocalDate().equals(calendarToLocalDate)).findAny()
                        .isPresent()) {
                    return;
                }
                List<Bar> history = stock.getHistory();
                if (!history.isEmpty()) {
                    Bar mostRecentQuote = TimeseriesUtils.getMostRecentQuote(history);
                    if (mostRecentQuote.getEndTime().toLocalDate().isEqual(calendarToLocalDate)) {
                        history.remove(mostRecentQuote);
                    }
                    history.add(new ExtendedHistoricalQuote(stock.getInstrument().code(), calendarToLocalDate,
                            quote.getOpen(), quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(),
                            DoubleNum.valueOf(quote.getVolume()), Source.YAHOO.name()));
                }
            }
        } else {
            IntelligentStockFeed.log.warn(String.format("Failed to populate quote for %s", stock.getInstrument()));
        }

    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) {
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), false, false, addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, final boolean interpolate,
                                 final boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
        return this.get(instrument, LocalDate.now().minusYears(years), LocalDate.now(), interpolate, cleanData, addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate, boolean addLatestQuoteToTheSeries)
            throws IOException {
        return this.get(instrument, fromDate, toDate, false, false, addLatestQuoteToTheSeries);
    }

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromDateRaw, final LocalDate toDateRaw,
                                 final boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) {
        try {
            return getUsingCache(instrument, fromDateRaw, toDateRaw, interpolate, cleanData, addLatestQuoteToTheSeries);
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println(e.getStackTrace());
            IntelligentStockFeed.log.warn("Failed to get data", e.getMessage());
            return Optional.empty();
        }

    }

    private Optional<StockV1> getUsingCache(final Instrument instrument, final LocalDate fromDateRaw,
                                            final LocalDate toDateRaw, final boolean interpolate,
                                            boolean cleanData, boolean addLatestQuoteToTheSeries)
            throws IOException {

        // Ignore weekends
        LocalDate fromDate = DateUtils.getLastWeekday(fromDateRaw);
        LocalDate toDate = DateUtils.getLastWeekday(toDateRaw);

        if (instrument.equals(Instrument.CASH)) {
            return getFlatCashSeries(instrument, fromDate, toDate);
        }

        final CachedStockFeed cachedDataFeed = (CachedStockFeed) stockFeedFactory.getDataFeed(Source.MANUAL);

        // we try to get from file cache first, then these sources in turn, then we might interpolate gaps
        Optional<StockV1> cachedData = this.getDataIfFeedAvailable(instrument, fromDate, toDate, cachedDataFeed,
                true, addLatestQuoteToTheSeries);
        cachedData = getWebFeed(instrument, addLatestQuoteToTheSeries, fromDate, toDate, cachedData,
                stockFeedFactory.getDataFeed(Source.FT));
        cachedData = getWebFeed(instrument, addLatestQuoteToTheSeries, fromDate, toDate, cachedData,
                stockFeedFactory.getDataFeed(Source.STOOQ));
        cachedData = getWebFeed(instrument, addLatestQuoteToTheSeries, fromDate, toDate, cachedData,
                stockFeedFactory.getDataFeed(Source.ALPHAVANTAGE));
        cachedData = getWebFeed(instrument, addLatestQuoteToTheSeries, fromDate, toDate, cachedData,
                stockFeedFactory.getDataFeed(Source.YAHOO));

        if (addLatestQuoteToTheSeries) {
            // or Source.ALPHAVANTAGE
            this.addLatestQuoteToTheSeries(cachedData.get(), stockFeedFactory.getQuoteFeed(Source.YAHOO));
        }

        if (cachedData.isEmpty()) {
            IntelligentStockFeed.log.warn("No data for " + instrument);
            return Optional.empty();
        }

        if (cleanData) {
            TimeseriesUtils.cleanUpSeries(cachedData);
        }
        cachedDataFeed.storeSeries(cachedData.get());
        return TimeseriesUtils.interpolateAndSortSeries(fromDate, toDate, interpolate, cachedData);
    }

    private Optional<StockV1> getWebFeed(Instrument instrument, boolean addLatestQuoteToTheSeries, LocalDate fromDate,
                                         LocalDate toDate, Optional<StockV1> cachedData,
                                         StockFeed webDataFeed) throws IOException {
        String key = getKey(instrument.getCode(), fromDate, toDate, webDataFeed.getSource().toString());

        boolean getWebData = refresh && webDataFeed.isAvailable()
                && !this.previousQueries.contains(key);

        if (getWebData) {
            Optional<StockV1> webdata = Optional.empty();
            if (cachedData.isPresent()) {
                final List<Bar> cachedHistory = cachedData.get().getHistory();
                List<LocalDate> missingDates = TimeseriesUtils.getMissingDataPointsForDateRange(cachedHistory, fromDate,
                        DateUtils.getPreviousDate(toDate));

                if (!missingDates.isEmpty()) {
                    LocalDate fromDate1 = missingDates.get(0);
                    LocalDate toDate1 = missingDates.get(missingDates.size() - 1);
                    log.info("Going to get missing data from " + fromDate1 + " to " + toDate1);
                    try {
                        webdata = this.getDataIfFeedAvailable(instrument, fromDate1,
                                toDate1, webDataFeed, refresh, addLatestQuoteToTheSeries);

                    }catch(Exception e){
                        log.warn("Exception from " + webDataFeed.getSource(), e);
                    }
                }
            } else {
                try {
                    webdata = this.getDataIfFeedAvailable(instrument, fromDate, toDate, webDataFeed,
                        refresh, addLatestQuoteToTheSeries);
                }catch(Exception e){
                    log.warn("Exception from " + webDataFeed.getSource(), e);
                }
            }
            if(cachedData.isEmpty()){
                cachedData = webdata;
            }else if (webdata.isPresent()) {
                final StockV1 stock = webdata.get();
                this.mergeSeries(cachedData.get(), stock.getHistory());
                if(webdata.isPresent()){
                    this.previousQueries.add(key);
                }
            }

        }


        return cachedData;
    }

    private String getKey(String code, LocalDate fromDate, LocalDate toDate, String source) {
        return  code + "|" + fromDate.toString() + "|" + toDate.toString() + "|" + source;
    }

    public Optional<StockV1> get(final Instrument instrument, final String fromDate, final String toDate,
                                 final boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries) {
        return this.get(instrument, LocalDate.parse(fromDate), LocalDate.parse(toDate), interpolate, cleanData, addLatestQuoteToTheSeries);
    }

    public Optional<StockV1> getDataIfFeedAvailable(final Instrument instrument, final LocalDate fromDate,
                                                    final LocalDate toDate, final StockFeed dataFeed, final boolean useFeed, boolean addLatestQuoteToTheSeries) throws IOException {
        final Optional<StockV1> data;
        if (useFeed) {
            if (dataFeed.isAvailable()) {
                data = dataFeed.get(instrument, fromDate, toDate, addLatestQuoteToTheSeries);
            } else {
                IntelligentStockFeed.log.warn(dataFeed.getClass().getName() + " is not available");
                data = Optional.empty();
            }
        } else {
            data = Optional.empty();
        }
        return data;
    }

    @Override
    public Source getSource() {
        return Source.MANUAL;
    }

    @Override
    public boolean isAvailable() {
        return stockFeedFactory.getDataFeed(Source.MANUAL).isAvailable()
                || stockFeedFactory.getDataFeed(Source.ALPHAVANTAGE).isAvailable()
                || stockFeedFactory.getDataFeed(Source.YAHOO).isAvailable();
    }

}
