package com.leonarduk.finance.stockfeed.yahoo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;

import yahoofinance.histquotes.HistoricalQuote;

public class YahooFeedIT {

	private YahooFeed feed;
	private Instrument gold;
	private Instrument bonds;

	private void getInstrument(final Instrument instrument) throws IOException {
		final Optional<Stock> stock = this.feed.get(instrument, 1);
		assertTrue(stock.isPresent());
		final List<HistoricalQuote> history = stock.get().getHistory();
		assertTrue(history.size() > 0);
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
		YahooFeed.getFx(Instrument.fromString("USDGBP"));
	}

	@Test
	public final void testGetInstrumentBonds() throws IOException {
		final Instrument instrument = this.bonds;
		this.getInstrument(instrument);
	}

	@Test
	public final void testGetInstrumentGold() throws IOException {
		final Instrument instrument = this.gold;
		this.getInstrument(instrument);
	}

	@Test
	public final void testGetQueryName() {
		assertEquals("PHGP.L", YahooFeed.getQueryName(this.gold));
		assertEquals("GB00B1H05601.L", YahooFeed.getQueryName(this.bonds));
	}

}
