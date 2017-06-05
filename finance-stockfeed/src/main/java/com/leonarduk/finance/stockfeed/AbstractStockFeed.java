package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;
import yahoofinance.quotes.stock.StockQuote.StockQuoteBuilder;

public abstract class AbstractStockFeed implements StockFeed {

	public static void addQuoteToSeries(final Instrument instrument,
	        final List<HistoricalQuote> quotes, final Stock stock) {
		final StockQuoteBuilder quoteBuilder = new StockQuote.StockQuoteBuilder(
		        instrument);

		if ((quotes != null) && !quotes.isEmpty()) {
			final HistoricalQuote historicalQuote = quotes
			        .get(quotes.size() - 1);
			quoteBuilder.setDayHigh(historicalQuote.getHigh())
			        .setDayLow(historicalQuote.getLow())
			        .setOpen(historicalQuote.getOpen())
			        .setAvgVolume(historicalQuote.getVolume())
			        .setPrice(historicalQuote.getClose());
			stock.setQuote(quoteBuilder.build());
			stock.setHistory(quotes);
		}
	}

	public static Stock createStock(final Instrument instrument) {
		return AbstractStockFeed.createStock(instrument, null);
	}

	public static Stock createStock(final Instrument instrument,
	        final List<HistoricalQuote> quotes) {
		final Stock stock = new Stock(instrument);
		stock.setHistory(quotes);
		AbstractStockFeed.addQuoteToSeries(instrument, quotes, stock);
		return stock;
	}

	@Override
	public abstract Optional<Stock> get(final Instrument instrument,
	        final int years) throws IOException;

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years,
	        final boolean interpolate) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public abstract Optional<Stock> get(final Instrument instrument,
	        final LocalDate fromDate, final LocalDate toDate)
	        throws IOException;

	@Override
	public Optional<Stock> get(final Instrument instrument,
	        final LocalDate fromLocalDate, final LocalDate toLocalDate,
	        final boolean interpolate) throws IOException {
		final Optional<Stock> liveData = this.get(instrument, fromLocalDate,
		        toLocalDate);
		TimeseriesUtils.cleanUpSeries(liveData);
		return TimeseriesUtils.interpolateAndSortSeries(fromLocalDate,
		        toLocalDate, interpolate, liveData);
	}

	@Override
	public abstract Source getSource();

	@Override
	public abstract boolean isAvailable();

	public void mergeSeries(final Stock stock,
	        final List<HistoricalQuote> original) throws IOException {
		final List<HistoricalQuote> newSeries = stock.getHistory();
		this.mergeSeries(stock, original, newSeries);
	}

	public void mergeSeries(final Stock stock,
	        final List<HistoricalQuote> original,
	        final List<HistoricalQuote> newSeries) {
		final Map<LocalDate, HistoricalQuote> dates = original.stream()
		        .collect(Collectors.toMap(quote -> quote.getDate(),
		                Function.identity()));
		newSeries.stream().forEach(historicalQuote -> {
			final LocalDate date = historicalQuote.getDate();
			if ((date != null) && !dates.containsKey(date)
			        && !historicalQuote.getClose().equals(Decimal.ZERO)) {
				dates.putIfAbsent(date, historicalQuote);
			}
		});

		final List<HistoricalQuote> sortedList = new LinkedList<>(
		        dates.values());
		sortedList.sort((quote1, quote2) -> quote1.getDate()
		        .compareTo(quote2.getDate()));
		stock.setHistory(sortedList);

	}

}
