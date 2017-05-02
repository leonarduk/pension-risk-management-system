package com.leonarduk.finance.stockfeed.yahoo;

import java.io.IOException;
import java.util.Calendar;
import java.util.Optional;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistQuotesRequest;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public class YahooFeed extends StockFeed {
	@Override
	public Optional<Stock> get(final Instrument instrument, final int years) throws IOException {
		return this.get(instrument.getExchange(), instrument.getIsin(), years);
	}

	@Override
	public Optional<Stock> get(final StockFeed.Exchange exchange, final String ticker, final int years) {
		try {
			final Stock stock = YahooFinance.get(this.getQueryName(exchange, ticker));
			final Calendar from = Calendar.getInstance();

			from.add(Calendar.YEAR, -1 * years);

			stock.getHistory(from, HistQuotesRequest.DEFAULT_TO, Interval.DAILY);
			final StockQuote quote = stock.getQuote();
			stock.getHistory().add(new HistoricalQuote(stock.getSymbol(), quote.getLastTradeTime(), quote.getOpen(),
					quote.getDayLow(), quote.getDayHigh(), quote.getPrice(), quote.getPrice(), quote.getVolume()));
			stock.setStockExchange(exchange.name());
			return Optional.of(stock);
		} catch (final Exception e) {
			return Optional.empty();
		}
	}

	private String getQueryName(final StockFeed.Exchange exchange, final String ticker) {
		switch (exchange) {
		case London:
			return ticker + ".L";
		}
		throw new IllegalArgumentException("Don't know how to handle " + exchange);
	}

}
