package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class InstrumentTest {

    @Test
    public void testRemoveExchange() throws IOException {
        Assert.assertEquals("FCIT",
                Instrument.fromString("FCIT.L").code());

    }

    @Test
    public void testFromString() throws IOException {
        Assert.assertEquals(Instrument.CASH, Instrument.fromString("Cash"));
        Assert.assertEquals("IE00BH361H73",
                Instrument.fromString("IE00BH361H73").isin());
        Assert.assertEquals("XDND",
                Instrument.fromString("IE00BH361H73").code());


    }

    @Test
    public void testFromStringDetailedInstrument() throws IOException {

        final Instrument actual = Instrument.fromString("XDND");
        Assert.assertEquals("IE00BH361H73", actual.isin());
        Assert.assertEquals("db x-trackers MSCI NA Hi Div Yld (DR)1C GBP",
                actual.getName());
        Assert.assertEquals(AssetType.ETF, actual.assetType());
        Assert.assertEquals(AssetType.EQUITY, actual.underlyingType());
        Assert.assertEquals(Source.GOOGLE, actual.source());
        Assert.assertEquals(Exchange.LONDON, actual.getExchange());
        Assert.assertEquals("US Large-Cap Value Equity", actual.getCategory());
        Assert.assertEquals("MSCI NA High Div Yield", actual.getIndexCategory());
        Assert.assertEquals("GBX", actual.getCurrency());
        Assert.assertEquals("LON:XDND", actual.getGoogleCode());

    }

    @Test
    public void testInactiveTickerFilteredOut() throws IOException {
        Instrument.InstrumentLoader loader = Instrument.InstrumentLoader.getInstance();
        // Inactive instrument CC1 should not be present using any of its identifiers
        Assert.assertFalse(loader.getInstruments().containsKey("CC1"));
        Assert.assertFalse(loader.getInstruments().containsKey("FR0010713784"));
        Assert.assertFalse(loader.getInstruments().containsKey("LON:CC1"));

        // Active instrument still available
        Assert.assertTrue(loader.getInstruments().containsKey("XDND"));

        // Lookups for inactive identifiers should return a manually created placeholder
        Assert.assertEquals(Source.MANUAL, Instrument.fromString("CC1").getSource());
        Assert.assertEquals(Source.MANUAL, Instrument.fromString("FR0010713784").getSource());
    }

    @Test
    public void testLoaderSingletonReadsOnce() throws Exception {
        Instrument.InstrumentLoader first = Instrument.InstrumentLoader.getInstance();
        Instrument.InstrumentLoader second = Instrument.InstrumentLoader.getInstance();
        Assert.assertSame(first, second);

        Field field = Instrument.InstrumentLoader.class.getDeclaredField("instruments");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Instrument> map = (Map<String, Instrument>) field.get(first);
        Instrument removed = map.remove("XDND");

        Instrument.InstrumentLoader third = Instrument.InstrumentLoader.getInstance();
        Assert.assertSame(first, third);
        @SuppressWarnings("unchecked")
        Map<String, Instrument> mapAfter = (Map<String, Instrument>) field.get(third);
        Assert.assertFalse(mapAfter.containsKey("XDND"));

        // restore state for other tests
        mapAfter.put("XDND", removed);
    }

}
