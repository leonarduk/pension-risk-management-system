package com.leonarduk.finance.stockfeed;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

public class InstrumentTest {

	@Test
	public void testFromString() {
		assertEquals(Instrument.CASH, Instrument.fromString("Cash"));
		assertEquals("IE00BH361H73", Instrument.fromString("IE00BH361H73").isin());
		assertEquals("XDND", Instrument.fromString("IE00BH361H73").code());

		final Instrument actual = Instrument.fromString("XDND");
		assertEquals("IE00BH361H73", actual.isin());
		assertEquals("db x-trackers MSCI NA Hi Div Yld (DR)1C GBP", actual.getName());
		assertEquals(AssetType.ETF, actual.assetType());
		assertEquals(AssetType.EQUITY, actual.underlyingType());
		assertEquals(Source.Google, actual.source());
		assertEquals(Exchange.London, actual.getExchange());
		assertEquals("US Large-Cap Value Equity", actual.getCategory());
		assertEquals("GBX", actual.getCurrency());
		assertEquals("LON:XDND", actual.getGoogleCode());

	}

}
