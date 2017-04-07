package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class DailyTimeseries {
	public static TimeSeries getTimeSeries(Stock stock) throws IOException {
		Iterator<HistoricalQuote> series = stock.getHistory().iterator();

		List<Tick> ticks = new LinkedList<>();
		while (series.hasNext()) {
			try {
				HistoricalQuote quote = series.next();

				double open = ensureIsDouble(quote.getOpen());
				double high = ensureIsDouble(quote.getHigh());
				double low = ensureIsDouble(quote.getLow());
				double close = ensureIsDouble(quote.getClose());
				double volume = ensureIsDouble(quote.getVolume());

				ticks.add(new Tick(new DateTime(quote.getDate().getTime()), open, high, low, close, volume));
			} catch (NullPointerException e) {
			}
		}
		return new TimeSeries(stock.getName(), ticks);
	}

	private static Double ensureIsDouble(Number bigDecimal) {
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.doubleValue();
	}
}
