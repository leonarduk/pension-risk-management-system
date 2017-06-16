package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.leonarduk.finance.utils.DateUtils;

import com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class IntelligentStockFeedTest {

	private IntelligentStockFeed feed;

	@Before
	public void setUp() throws Exception {
		this.feed = new IntelligentStockFeed();
	}

	@Test
	public final void testAddLatestQuoteToTheSeries() throws IOException {
		final Instrument instrument = Mockito.mock(Instrument.class);
		final Stock stock = new Stock(instrument);
		final List<HistoricalQuote> history = Lists.newLinkedList();
		stock.setHistory(history);
		final QuoteFeed dataFeed = new QuoteFeed() {

			@Override
			public StockQuote getStockQuote(final Instrument instrument)
			        throws IOException {
				final Calendar lastTradeDateTime = DateUtils
				        .dateToCalendar(LocalDate.parse("2017-02-01"));
				return new StockQuote.StockQuoteBuilder(instrument)
				        .setLastTradeDateStr("2017-02-01")
				        .setLastTradeTime(lastTradeDateTime).build();
			}

			@Override
			public boolean isAvailable() {
				return true;
			}
		};
		this.feed.addLatestQuoteToTheSeries(stock, dataFeed);
		Assert.assertEquals(1, stock.getHistory().size());
	}

	@Test
	public final void testCleanUpSeries() {
	}

	@Test
	public final void testGetFlatCashSeriesInstrumentInt() {
	}

	@Test
	public final void testGetFlatCashSeriesInstrumentLocalDateLocalDate() {
	}

	@Test
	public final void testIsAvailable() throws IOException {
		final boolean useFeed = true;
		final StockFeed dataFeed = Mockito.mock(StockFeed.class);
		this.feed.getDataIfFeedAvailable(Instrument.CASH, LocalDate.now(),
		        LocalDate.now(), dataFeed, useFeed);
	}

}
