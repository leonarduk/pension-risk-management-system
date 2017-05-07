package com.leonarduk.finance.stockfeed;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;

import yahoofinance.histquotes.HistoricalQuote;

public class IntelligentStockFeedIT {

	private IntelligentStockFeed feed;

	private void getInstrument(final Instrument instrument) throws IOException {
		final Optional<Stock> stock = this.feed.get(instrument, 1);
		assertTrue(stock.isPresent());
		final List<HistoricalQuote> history = stock.get().getHistory();
		assertTrue(history.size() > 0);
	}

	@Before
	public void setUp() throws Exception {
		this.feed = new IntelligentStockFeed();
		final Instrument cash = Instrument.CASH;
	}

	@Test
	public final void testGetFlatCashSeries() throws IOException {
		assertEquals(261, IntelligentStockFeed.getFlatCashSeries(Instrument.CASH, 1).get().getHistory().size());
	}

	@Test
	public final void testGetGoogle() throws IOException {
		final Optional<Instrument> instrument = Instrument.values().stream()
				.filter(i -> i.getSource().equals(Source.Google) && !i.assetType().equals(AssetType.FX)).findFirst();
		this.getInstrument(instrument.get());
	}

	@Test
	public final void testGetYahoo() throws IOException {
		final Optional<Instrument> instrument = Instrument.values().stream()
				.filter(i -> i.getSource().equals(Source.Yahoo) && !i.assetType().equals(AssetType.FX)).findFirst();
		this.getInstrument(instrument.get());
	}

	@Test
	public final void testSetRefresh() {
		// TODO
	}

}
