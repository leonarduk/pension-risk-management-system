package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.quotes.stock.StockQuote;

public class StockFeedTest {

	private StockFeed stockFeed;

	private List<HistoricalQuote> getAlternateQuotes() {
		final List<HistoricalQuote> series = Lists.newArrayList();
		series.add(new HistoricalQuote(Instrument.CASH, LocalDate.parse("2017-01-02"),
		        BigDecimal.valueOf(11.3), BigDecimal.TEN, BigDecimal.valueOf(8.3),
		        BigDecimal.valueOf(11.2), BigDecimal.valueOf(11.2), 13L, "TestCache"));
		return series;
	}

	private List<HistoricalQuote> getQuotes() {
		final List<HistoricalQuote> series = Lists.newArrayList();
		series.add(new HistoricalQuote(Instrument.CASH, LocalDate.parse("2017-01-01"),
		        BigDecimal.valueOf(12.3), BigDecimal.TEN, BigDecimal.valueOf(9.3),
		        BigDecimal.valueOf(12.2), BigDecimal.valueOf(12.2), 23L, "TestCache"));
		return series;
	}

	@Before
	public void setUp() throws Exception {
		this.stockFeed = new StockFeed() {

			@Override
			public Optional<Stock> get(final Instrument instrument, final int years)
			        throws IOException {
				return Optional
				        .of(StockFeed.createStock(instrument, StockFeedTest.this.getQuotes()));
			}

			@Override
			public Optional<Stock> get(final Instrument instrument, final LocalDate fromDate,
			        final LocalDate toDate) throws IOException {
				return Optional
				        .of(StockFeed.createStock(instrument, StockFeedTest.this.getQuotes()));
			}

			@Override
			public boolean isAvailable() {
				return true;
			}
		};

	}

	@Test
	public final void testCreateStockInstrument() {
		final Stock actual = StockFeed.createStock(Instrument.CASH);
		Assert.assertEquals(new Stock(Instrument.CASH), actual);
	}

	@Test
	public final void testCreateStockInstrumentListOfHistoricalQuote() {
		final Instrument cash = Instrument.CASH;
		final List<HistoricalQuote> quotes = this.getQuotes();
		final Stock actual = StockFeed.createStock(cash, quotes);
		final Stock expected = new Stock(cash);
		expected.setHistory(quotes);
		final HistoricalQuote historicalQuote = quotes.get(quotes.size() - 1);

		// TODO should really populate this a different way to the code we are
		// testing
		final StockQuote quote = new StockQuote(cash);
		quote.setDayHigh(historicalQuote.getHigh());
		quote.setDayLow(historicalQuote.getLow());
		quote.setOpen(historicalQuote.getOpen());
		quote.setAvgVolume(historicalQuote.getVolume());
		quote.setPrice(historicalQuote.getClose());
		expected.setQuote(quote);
		Assert.assertEquals(expected, actual);
	}

	@Test
	public final void testGet() throws IOException {
		final Optional<Stock> actual = this.stockFeed.get(Instrument.CASH, 1);
		Assert.assertTrue(actual.isPresent());
	}

	@Test
	public final void testMergeSeriesStockListOfHistoricalQuote() throws IOException {
		final Stock stock = StockFeed.createStock(Instrument.CASH, this.getQuotes());
		this.stockFeed.mergeSeries(stock, this.getAlternateQuotes());
		Assert.assertEquals(2, stock.getHistory().size());
	}

	@Test
	public final void testMergeSeriesStockListOfHistoricalQuoteListOfHistoricalQuote()
	        throws IOException {
		final Stock stock = StockFeed.createStock(Instrument.CASH, this.getQuotes());
		this.stockFeed.mergeSeries(stock, this.getQuotes(), this.getAlternateQuotes());
		Assert.assertEquals(2, stock.getHistory().size());
	}

	@Test
	public final void testSeriesToCsv() {
		final StringBuilder actual = StockFeed.seriesToCsv(this.getQuotes());
		Assert.assertEquals("date,open,high,low,close,volume\n"
		        + "2017-01-01,12.30,9.30,10.00,12.20,23,TestCache\n", actual.toString());
	}

}
