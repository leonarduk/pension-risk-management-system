package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.leonarduk.finance.stockfeed.yahoofinance.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.yahoofinance.StockQuoteBuilder;
import com.leonarduk.finance.stockfeed.yahoofinance.StockV1;
import com.leonarduk.finance.utils.TimeseriesUtils;

public abstract class AbstractStockFeed implements StockFeed {

	public static void addQuoteToSeries(final Instrument instrument, final List<ExtendedHistoricalQuote> quotes,
			final StockV1 stock) {
		final StockQuoteBuilder quoteBuilder = new StockQuoteBuilder(instrument);

		if ((quotes != null) && !quotes.isEmpty()) {
			final ExtendedHistoricalQuote historicalQuote = quotes.get(quotes.size() - 1);
			quoteBuilder.setDayHigh(historicalQuote.getHigh()).setDayLow(historicalQuote.getLow())
					.setOpen(historicalQuote.getOpen()).setAvgVolume(historicalQuote.getVolume())
					.setPrice(historicalQuote.getClose());
			stock.setQuote(quoteBuilder.build());
			stock.setHistory(quotes);
		}
	}

	public static StockV1 createStock(final Instrument instrument) {
		return AbstractStockFeed.createStock(instrument, null);
	}

	public static StockV1 createStock(final Instrument instrument, final List<ExtendedHistoricalQuote> quotes) {
		final StockV1 stock = new StockV1(instrument);
		stock.setHistory(quotes);
		AbstractStockFeed.addQuoteToSeries(instrument, quotes, stock);
		return stock;
	}

	@Override
	public abstract Optional<StockV1> get(final Instrument instrument, final int years) throws IOException;

	@Override
	public Optional<StockV1> get(final Instrument instrument, final int years, final boolean interpolate)
			throws IOException {
		return get(instrument, LocalDate.now().plusYears(-1 * years), LocalDate.now(), interpolate);
	}

	@Override
	public abstract Optional<StockV1> get(final Instrument instrument, final LocalDate fromDate, final LocalDate toDate)
			throws IOException;

	@Override
	public Optional<StockV1> get(final Instrument instrument, final LocalDate fromLocalDate,
			final LocalDate toLocalDate, final boolean interpolate) throws IOException {
		final Optional<StockV1> liveData = this.get(instrument, fromLocalDate, toLocalDate);
		TimeseriesUtils.cleanUpSeries(liveData);
		return TimeseriesUtils.interpolateAndSortSeries(fromLocalDate, toLocalDate, interpolate, liveData);
	}

	@Override
	public abstract Source getSource();

	@Override
	public abstract boolean isAvailable();

	public void mergeSeries(final StockV1 stock, final List<ExtendedHistoricalQuote> original) throws IOException {
		final List<ExtendedHistoricalQuote> newSeries = stock.getHistory();
		this.mergeSeries(stock, original, newSeries);
	}

	public void mergeSeries(final StockV1 stock, final List<ExtendedHistoricalQuote> original,
			final List<ExtendedHistoricalQuote> newSeries) {
		final Map<LocalDate, ExtendedHistoricalQuote> dates = original.stream()
				.collect(Collectors.toMap(quote -> quote.getLocaldate(), Function.identity()));
		newSeries.stream().forEach(historicalQuote -> {
			final LocalDate date = historicalQuote.getLocaldate();
			if ((date != null) && !dates.containsKey(date)
					&& !historicalQuote.getClose().equals(BigDecimal.valueOf(0))) {
				dates.putIfAbsent(date, historicalQuote);
			}
		});

		final List<ExtendedHistoricalQuote> sortedList = new LinkedList<>(dates.values());
		sortedList.sort((quote1, quote2) -> quote1.getDate().compareTo(quote2.getDate()));
		stock.setHistory(sortedList);

	}

}
