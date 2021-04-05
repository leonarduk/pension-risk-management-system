package com.leonarduk.finance.stockfeed.feed.google;
//package com.leonarduk.finance.stockfeed.google;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Optional;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.leonarduk.finance.stockfeed.Instrument;
//import com.leonarduk.finance.stockfeed.Stock;
//
//import yahoofinance.histquotes.HistoricalQuote;
//
//public class GoogleFeedIT {
//
//	private Instrument	bonds;
//	private GoogleFeed	feed;
//	private Instrument	gold;
//
//	private boolean getInstrument(final Instrument instrument)
//	        throws IOException {
//		final Optional<Stock> stock = this.feed.get(instrument, 1);
//		Assert.assertTrue(stock.isPresent());
//		final List<HistoricalQuote> history = stock.get().getHistory();
//		Assert.assertTrue(history.size() > 0);
//		return true;
//	}
//
//	@Before
//	public void setUp() throws Exception {
//		this.feed = new GoogleFeed();
//		this.gold = Instrument.fromString("PHGP");
//		this.bonds = Instrument.fromString("XGSG");
//
//	}
//
//	@Test
//	public final void testGetInstrumentBonds() throws IOException {
//		final Instrument instrument = this.bonds;
//		Assert.assertTrue(this.getInstrument(instrument));
//	}
//
//	@Test
//	public final void testGetInstrumentGold() throws IOException {
//		final Instrument instrument = this.gold;
//		Assert.assertTrue(this.getInstrument(instrument));
//	}
//
//	@Test
//	public final void testGetQueryName() {
//		Assert.assertEquals("LON:PHGP", this.feed.getQueryName(this.gold));
//		Assert.assertEquals("LON:XGSG", this.feed.getQueryName(this.bonds));
//	}
//
//	@Test
//	public void testIsAvailable() throws Exception {
//		Assert.assertTrue(this.feed.isAvailable());
//	}
//
//}
