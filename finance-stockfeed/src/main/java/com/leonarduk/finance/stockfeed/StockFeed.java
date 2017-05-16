package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.file.IndicatorsToCsv;

import eu.verdelhan.ta4j.Decimal;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public abstract class StockFeed {
	public enum Exchange {
		London
	}

	public static Stock createStock(final Instrument instrument) {
		return StockFeed.createStock(instrument, null);
	}

	public static Stock createStock(final Instrument instrument,
	        final List<HistoricalQuote> quotes) {
		final Stock stock = new Stock(instrument);

		// stock.setCurrency();
		final StockQuote quote = new StockQuote(instrument);

		if ((quotes != null) && !quotes.isEmpty()) {
			final HistoricalQuote historicalQuote = quotes.get(quotes.size() - 1);

			quote.setDayHigh(historicalQuote.getHigh());
			quote.setDayLow(historicalQuote.getLow());
			quote.setOpen(historicalQuote.getOpen());
			quote.setAvgVolume(historicalQuote.getVolume());
			quote.setPrice(historicalQuote.getClose());
			stock.setQuote(quote);
			stock.setHistory(quotes);
		}
		return stock;
	}

	public static StringBuilder seriesToCsv(final List<HistoricalQuote> series) {
		final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume\n");
		for (final HistoricalQuote historicalQuote : series) {
			sb.append(historicalQuote.getDate().toString());
			IndicatorsToCsv.addValue(sb, historicalQuote.getOpen());
			IndicatorsToCsv.addValue(sb, historicalQuote.getHigh());
			IndicatorsToCsv.addValue(sb, historicalQuote.getLow());
			IndicatorsToCsv.addValue(sb, historicalQuote.getClose());
			IndicatorsToCsv.addValue(sb, historicalQuote.getVolume());
			sb.append(",").append(historicalQuote.getComment()).append("\n");
		}
		return sb;
	}

	public abstract Optional<Stock> get(final Instrument instrument, final int years)
	        throws IOException;

	public abstract Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
	        final LocalDate toDate) throws IOException;

	public void mergeSeries(final Stock stock, final List<HistoricalQuote> original)
	        throws IOException {
		final List<HistoricalQuote> newSeries = stock.getHistory();
		this.mergeSeries(stock, original, newSeries);
	}

	public void mergeSeries(final Stock stock, final List<HistoricalQuote> original,
	        final List<HistoricalQuote> newSeries) {
		final Map<LocalDate, HistoricalQuote> dates = original.stream()
		        .collect(Collectors.toMap(quote -> quote.getDate(), Function.identity()));
		newSeries.stream().forEach(historicalQuote -> {
			final LocalDate date = historicalQuote.getDate();
			if ((date != null) && !dates.containsKey(date)
			        && !historicalQuote.getClose().equals(Decimal.ZERO)) {
				dates.put(date, historicalQuote);
			}
		});

		final List<HistoricalQuote> sortedList = new LinkedList<>(dates.values());
		sortedList.sort((quote1, quote2) -> quote1.getDate().compareTo(quote2.getDate()));
		stock.setHistory(sortedList);

	}

}
