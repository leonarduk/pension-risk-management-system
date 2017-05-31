package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;

import yahoofinance.histquotes.HistoricalQuote;

public class IntelligentStockFeedIT {

	private IntelligentStockFeed feed;

	private boolean getInstrument(final Instrument instrument)
	        throws IOException {
		final Optional<Stock> stock = this.feed.get(instrument, 1);
		Assert.assertTrue(stock.isPresent());
		final List<HistoricalQuote> history = stock.get().getHistory();
		return (history.size() > 0);
	}

	@Before
	public void setUp() throws Exception {
		this.feed = new IntelligentStockFeed();
	}

	@Test
	public final void testGetFlatCashSeries() throws IOException {
		// TODO in AM this gives 261; in PM gives 262 - need to work out why
		Assert.assertTrue(
		        IntelligentStockFeed.getFlatCashSeries(Instrument.CASH, 1).get()
		                .getHistory().size() > 260);
	}

	@Test
	public final void testGetGoogle() throws IOException {
		final Optional<Instrument> instrument = Instrument.values().stream()
		        .filter(i -> i.getSource().equals(Source.Google)
		                && !i.assetType().equals(AssetType.FX))
		        .findFirst();
		Assert.assertTrue(this.getInstrument(instrument.get()));
	}

	@Test
	public final void testGetYahoo() throws IOException {
		final Optional<Instrument> instrument = Instrument.values().stream()
		        .filter(i -> i.getSource().equals(Source.Yahoo)
		                && !i.assetType().equals(AssetType.FX))
		        .findFirst();
		Assert.assertTrue(this.getInstrument(instrument.get()));
	}

	@Test
	public void testIsAvailable() throws Exception {
		Assert.assertTrue(this.feed.isAvailable());
	}

}
