package com.leonarduk.finance.stockfeed;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class InstrumentTest {

	@Test
	public void testFromString() throws IOException {
		Assert.assertEquals(Instrument.CASH, Instrument.fromString("Cash"));
		Assert.assertEquals("IE00BH361H73", Instrument.fromString("IE00BH361H73").isin());
		Assert.assertEquals("XDND", Instrument.fromString("IE00BH361H73").code());

		final Instrument actual = Instrument.fromString("XDND");
		Assert.assertEquals("IE00BH361H73", actual.isin());
		Assert.assertEquals("db x-trackers MSCI NA Hi Div Yld (DR)1C GBP", actual.getName());
		Assert.assertEquals(AssetType.ETF, actual.assetType());
		Assert.assertEquals(AssetType.EQUITY, actual.underlyingType());
		Assert.assertEquals(Source.Google, actual.source());
		Assert.assertEquals(Exchange.London, actual.getExchange());
		Assert.assertEquals("US Large-Cap Value Equity", actual.getCategory());
		Assert.assertEquals("GBX", actual.getCurrency());
		Assert.assertEquals("LON:XDND", actual.getGoogleCode());

	}

}
