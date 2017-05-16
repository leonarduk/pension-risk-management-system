package com.leonarduk.finance.stockfeed.yahoo;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;

import yahoofinance.histquotes.HistoricalQuote;

public class YahooFeedIT {

	private Instrument	bonds;
	private YahooFeed	feed;
	private Instrument	gold;

	private boolean getInstrument(final Instrument instrument) throws IOException {
		final Optional<Stock> stock = this.feed.get(instrument, 1);
		Assert.assertTrue(stock.isPresent());
		final List<HistoricalQuote> history = stock.get().getHistory();
		Assert.assertTrue(history.size() > 0);
		return true;
	}

	@Before
	public void setUp() throws Exception {
		this.feed = new YahooFeed();
		this.gold = Instrument.fromString("PHGP");
		this.bonds = Instrument.fromString("MGOII");

	}

	@Ignore
	@Test
	public final void testGetFx() throws IOException {
		Assert.assertNotNull(YahooFeed.getFx(Instrument.fromString("USDGBP")));
	}

	@Test
	public final void testGetInstrumentBonds() throws IOException {
		final Instrument instrument = this.bonds;
		Assert.assertTrue(this.getInstrument(instrument));
	}

	@Test
	public final void testGetInstrumentGold() throws IOException {
		final Instrument instrument = this.gold;
		Assert.assertTrue(this.getInstrument(instrument));
	}

	@Test
	public final void testGetQueryName() {
		Assert.assertEquals("PHGP.L", YahooFeed.getQueryName(this.gold));
		Assert.assertEquals("GB00B1H05601.L", YahooFeed.getQueryName(this.bonds));
	}

}
