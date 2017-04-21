package com.leonarduk.stockmarketview.stockfeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.stockfeed.google.DateUtils;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class CachedStockFeedTest {

	@Test
	public void testStoreSeries() throws IOException {
		String storeLocation = Files.createTempDir().getAbsolutePath();
		CachedStockFeed feed = new CachedStockFeed(storeLocation);
		String symbol = "test";
		Stock stock = new Stock(symbol);
		stock.setStockExchange(EXCHANGE.London.name());
		String fullName = EXCHANGE.London.name() + "_" + symbol + ".csv";
		List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new ComparableHistoricalQuote(fullName, DateUtils.dateToCalendar(LocalDate.parse("2017-01-01").toDate()),
				BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11), BigDecimal.valueOf(9),
				BigDecimal.valueOf(9), Long.valueOf(23)));
		history.add(new ComparableHistoricalQuote(fullName, DateUtils.dateToCalendar(LocalDate.parse("2017-01-02").toDate()),
				BigDecimal.valueOf(10), BigDecimal.valueOf(9), BigDecimal.valueOf(12), BigDecimal.valueOf(10),
				BigDecimal.valueOf(10), Long.valueOf(3)));
		stock.setHistory(history);
		feed.storeSeries(stock);

		Optional<Stock> fetchedFeed = feed.get(stock, 1);
		assertTrue(fetchedFeed.isPresent());

		assertTrue(fetchedFeed.get().getHistory().containsAll(history));
		assertTrue(history.containsAll(fetchedFeed.get().getHistory()));

		List<HistoricalQuote> newhistory = Lists.newArrayList();
		newhistory.add(new ComparableHistoricalQuote(fullName, DateUtils.dateToCalendar(LocalDate.parse("2017-01-01").toDate()),
				BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11), BigDecimal.valueOf(9),
				BigDecimal.valueOf(9), Long.valueOf(23)));
		newhistory.add(new ComparableHistoricalQuote(fullName, DateUtils.dateToCalendar(LocalDate.parse("2017-01-03").toDate()),
				BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11), BigDecimal.valueOf(9),
				BigDecimal.valueOf(9), Long.valueOf(23)));

		stock.setHistory(newhistory);
		feed.storeSeries(stock);

		Optional<Stock> newfetchedFeed = feed.get(stock, 1);
		assertTrue(newfetchedFeed.isPresent());
		assertTrue(newfetchedFeed.get().getHistory().containsAll(newhistory));
		assertEquals(new HashSet<>(newfetchedFeed.get().getHistory()).size() , newfetchedFeed.get().getHistory().size());

	}

}
