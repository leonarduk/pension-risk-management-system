package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
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
		List<HistoricalQuote> history = stock.getHistory();
		Collections.sort(history, (o1, o2) -> {
			return o2.getDate().compareTo(o1.getDate());
		});
		Iterator<HistoricalQuote> series = history.iterator();

		List<Tick> ticks = new LinkedList<>();
		while (series.hasNext()) {
			try {
				HistoricalQuote quote = series.next();

				BigDecimal closeBd = quote.getClose();
				double open = ensureIsDouble(ifNull(quote.getOpen(), closeBd));
				double high = ensureIsDouble(ifNull(quote.getHigh(), closeBd));
				double low = ensureIsDouble(ifNull(quote.getLow(), closeBd));
				double close = ensureIsDouble(closeBd);
				double volume = ensureIsDouble(ifNull(quote.getVolume(), 0L));

				ticks.add(new Tick(new DateTime(quote.getDate().getTime()), open, high, low, close, volume));
			} catch (NullPointerException e) {
			}
		}
		return new TimeSeries(stock.getName(), ticks);
	}

	private static Number ifNull(Number open, Number close) {
		if (open == null) {
			return close;
		}
		return open;
	}

	private static Double ensureIsDouble(Number bigDecimal) {
		if (bigDecimal == null) {
			return null;
		}
		return bigDecimal.doubleValue();
	}
}
