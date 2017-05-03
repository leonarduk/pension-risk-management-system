package com.leonarduk.finance.stockfeed;

import static com.leonarduk.finance.stockfeed.file.IndicatorsToCsv.addValue;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.joda.time.LocalDate;

import eu.verdelhan.ta4j.Decimal;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public abstract class StockFeed {
	public enum Exchange {
		London
	}

	public static Stock createStock(final Instrument instrument) {
		return createStock(instrument, null);
	}

	public static Stock createStock(final Instrument instrument, final List<HistoricalQuote> quotes) {
		final Stock stock = new Stock(instrument);

		// stock.setCurrency();
		final StockQuote quote = new StockQuote(instrument);

		if (!quotes.isEmpty()) {
			final HistoricalQuote historicalQuote = quotes.get(quotes.size() - 1);

			quote.setDayHigh(historicalQuote.getHigh());
			quote.setDayLow(historicalQuote.getLow());
			quote.setOpen(historicalQuote.getOpen());
			quote.setAvgVolume(historicalQuote.getVolume());
			quote.setPrice(historicalQuote.getClose());
			stock.setQuote(quote);
			// stock.setQuote(this.getQuote());
			// stock.setStats(this.getStats());
			// stock.setDividend(this.getDividend())
			stock.setHistory(quotes);
		}
		return stock;
	}

	public static StringBuilder seriesToCsv(final List<HistoricalQuote> series) {
		final StringBuilder sb = new StringBuilder("date,open,high,low,close,volume\n");
		final SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		for (final HistoricalQuote historicalQuote : series) {
			sb.append(format1.format(historicalQuote.getDate().getTime()));
			addValue(sb, historicalQuote.getOpen());
			addValue(sb, historicalQuote.getHigh());
			addValue(sb, historicalQuote.getLow());
			addValue(sb, historicalQuote.getClose());
			sb.append(",").append(historicalQuote.getVolume()).append("\n");
		}
		return sb;
	}

	public abstract Optional<Stock> get(final Instrument instrument, final int years) throws IOException;

	public void mergeSeries(final Stock stock, final List<HistoricalQuote> original) throws IOException {
		final List<HistoricalQuote> newSeries = stock.getHistory();
		this.mergeSeries(stock, original, newSeries);
	}

	public void mergeSeries(final Stock stock, final List<HistoricalQuote> original,
			final List<HistoricalQuote> newSeries) {
		final Map<LocalDate, HistoricalQuote> dates = original.stream()
				.collect(Collectors.toMap(quote -> LocalDate.fromCalendarFields(quote.getDate()), Function.identity()));
		newSeries.stream().forEach(historicalQuote -> {
			if (!dates.containsKey(LocalDate.fromCalendarFields(historicalQuote.getDate()))
					&& !historicalQuote.getClose().equals(Decimal.ZERO)) {
				dates.put(LocalDate.fromCalendarFields(historicalQuote.getDate()), historicalQuote);
			}
		});

		final List<HistoricalQuote> sortedList = new LinkedList<>(dates.values());
		sortedList.sort((quote1, quote2) -> quote1.getDate().compareTo(quote2.getDate()));
		stock.setHistory(sortedList);

	}

}
