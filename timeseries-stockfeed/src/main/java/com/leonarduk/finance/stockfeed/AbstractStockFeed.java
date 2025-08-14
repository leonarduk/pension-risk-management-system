package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockQuoteBuilder;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.TimeseriesUtils;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractStockFeed implements StockFeed {

    public static void addQuoteToSeries(final Instrument instrument, final List<Bar> quotes, final StockV1 stock) {
        final StockQuoteBuilder quoteBuilder = new StockQuoteBuilder(instrument);

        if ((quotes != null) && !quotes.isEmpty()) {
            final Bar historicalQuote = quotes.get(quotes.size() - 1);
            quoteBuilder.setDayHigh(historicalQuote.getHighPrice()).setDayLow(historicalQuote.getLowPrice())
                    .setOpen(historicalQuote.getOpenPrice()).setAvgVolume(historicalQuote.getVolume().longValue())
                    .setPrice(historicalQuote.getClosePrice());
            stock.setQuote(quoteBuilder.build());
            stock.setHistory(quotes);
        }
    }

    public static StockV1 createStock(final Instrument instrument, final List<Bar> quotes) {
        final StockV1 stock = new StockV1(instrument);
        stock.setHistory(quotes);
        AbstractStockFeed.addQuoteToSeries(instrument, quotes, stock);
        return stock;
    }

    @Override
    public abstract Optional<StockV1> get(final Instrument instrument, final int years, boolean addLatestQuoteToTheSeries) throws IOException;

    @Override
    public Optional<StockV1> get(final Instrument instrument, final int years, final boolean interpolate, boolean cleanData, boolean addLatestQuoteToTheSeries)
            throws IOException {
        return get(instrument, LocalDate.now().plusYears(-1 * years), LocalDate.now(), interpolate, cleanData, addLatestQuoteToTheSeries);
    }

    @Override
    public abstract Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate, boolean addLatestQuoteToTheSeries)
            throws IOException;

    @Override
    public Optional<StockV1> get(final Instrument instrument, final LocalDate fromLocalDate,
                                 final LocalDate toLocalDate, final boolean interpolate, final boolean cleanData, boolean addLatestQuoteToTheSeries) throws IOException {
        final Optional<StockV1> liveData = this.get(instrument, fromLocalDate, toLocalDate, addLatestQuoteToTheSeries);
        if (cleanData) {
            TimeseriesUtils.cleanUpSeries(liveData);
        }
        return TimeseriesUtils.interpolateAndSortSeries(fromLocalDate, toLocalDate, interpolate, liveData);
    }

    @Override
    public abstract Source getSource();

    @Override
    public abstract boolean isAvailable();

    public void mergeSeries(final StockV1 stock, final List<Bar> newSeries) throws IOException {
        this.mergeSeries(stock, stock.getHistory(), newSeries);
    }

    public void mergeSeries(final StockV1 stock, final List<Bar> original, final List<Bar> newSeries) {
        final Map<LocalDate, Bar> dates =
                original.stream()
                        .collect(
                                Collectors.toMap(
                                        quote -> quote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate(),
                                        Function.identity(),
                                        (existing, replacement) -> existing));
        newSeries.stream().forEach(historicalQuote -> {
            final LocalDate date = historicalQuote.getEndTime().atZone(ZoneId.systemDefault()).toLocalDate();
            if ((date != null) && !dates.containsKey(date)
                    && !historicalQuote.getClosePrice().equals(BigDecimal.valueOf(0))) {
                dates.putIfAbsent(date, historicalQuote);
            }
        });

        final List<Bar> sortedList = new LinkedList<>(dates.values());
        sortedList.sort(Comparator.comparing(Bar::getEndTime));
        stock.setHistory(sortedList);

    }

}
