package com.leonarduk.finance.stockfeed.yahoo;

import java.util.Calendar;
import java.util.Optional;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class YahooFeed extends StockFeed {

	public static String getQueryName(final Instrument instrument) {
		switch (instrument.getExchange()) {
		case London:
			return instrument.getIsin() + ".L";
		}
		throw new IllegalArgumentException("Don't know how to handle " + instrument.getExchange());
	}

	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) {
		try {
			final Calendar from = Calendar.getInstance();

			from.add(Calendar.YEAR, -1 * years);
			final Stock stock = new Stock(instrument);

			stock.getHistory(from, HistQuotesRequest.DEFAULT_TO, Interval.DAILY);
			final StockQuote quote = stock.getQuote();
			stock.getHistory().add(new HistoricalQuote(stock.getInstrument(), quote.getLastTradeTime(), quote.getOpen(),
					quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(), quote.getVolume()));
			return Optional.of(stock);
		} catch (final Exception e) {
			return Optional.empty();
		}
	}

}
