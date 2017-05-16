package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

import yahoofinance.histquotes.HistoricalQuote;

public class CachedStockFeedTest {

	@Test
	public void testStoreSeries() throws IOException {
		final String storeLocation = Files.createTempDir().getAbsolutePath();
		final CachedStockFeed feed = new CachedStockFeed(storeLocation);
		final Instrument symbol = Instrument.CASH;
		final Stock stock = new Stock(symbol);
		final List<HistoricalQuote> history = Lists.newArrayList();
		history.add(new HistoricalQuote(symbol, LocalDate.parse("2017-01-01"),
		        BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11),
		        BigDecimal.valueOf(9), BigDecimal.valueOf(9), Long.valueOf(23), "TestCache"));
		history.add(new HistoricalQuote(symbol, LocalDate.parse("2017-01-02"),
		        BigDecimal.valueOf(10), BigDecimal.valueOf(9), BigDecimal.valueOf(12),
		        BigDecimal.valueOf(10), BigDecimal.valueOf(10), Long.valueOf(3), "TestCache"));
		stock.setHistory(history);
		feed.storeSeries(stock);

		final Optional<Stock> fetchedFeed = feed.get(symbol, 1);
		Assert.assertTrue(fetchedFeed.isPresent());

		Assert.assertTrue(fetchedFeed.get().getHistory().containsAll(history));
		Assert.assertTrue(history.containsAll(fetchedFeed.get().getHistory()));

		final List<HistoricalQuote> newhistory = Lists.newArrayList();
		newhistory.add(new HistoricalQuote(symbol, LocalDate.parse("2017-01-01"),
		        BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11),
		        BigDecimal.valueOf(9), BigDecimal.valueOf(9), Long.valueOf(23), "TestCache"));
		newhistory.add(new HistoricalQuote(symbol, LocalDate.parse("2017-01-03"),
		        BigDecimal.valueOf(10), BigDecimal.valueOf(8), BigDecimal.valueOf(11),
		        BigDecimal.valueOf(9), BigDecimal.valueOf(9), Long.valueOf(23), "TestCache"));

		stock.setHistory(newhistory);
		feed.storeSeries(stock);

		final Optional<Stock> newfetchedFeed = feed.get(symbol, 1);
		Assert.assertTrue(newfetchedFeed.isPresent());
		Assert.assertTrue(newfetchedFeed.get().getHistory().containsAll(newhistory));
		Assert.assertEquals(new HashSet<>(newfetchedFeed.get().getHistory()).size(),
		        newfetchedFeed.get().getHistory().size());

	}

}
