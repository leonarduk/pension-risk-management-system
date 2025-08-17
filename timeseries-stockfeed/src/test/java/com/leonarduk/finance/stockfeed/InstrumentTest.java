package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.Instrument.AssetType;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public class InstrumentTest {

    @Test
    public void testRemoveExchange() throws IOException {
        Assertions.assertEquals("FCIT",
                Instrument.fromString("FCIT.L").code());

    }

    @Test
    public void testFromString() throws IOException {
        Assertions.assertEquals(Instrument.CASH, Instrument.fromString("Cash"));
        Assertions.assertEquals("IE00BH361H73",
                Instrument.fromString("IE00BH361H73").isin());
        Assertions.assertEquals("XDND",
                Instrument.fromString("IE00BH361H73").code());


    }

    @Test
    public void testFromStringDetailedInstrument() throws IOException {

        final Instrument actual = Instrument.fromString("XDND");
        Assertions.assertEquals("IE00BH361H73", actual.isin());
        Assertions.assertEquals("db x-trackers MSCI NA Hi Div Yld (DR)1C GBP",
                actual.getName());
        Assertions.assertEquals(AssetType.ETF, actual.assetType());
        Assertions.assertEquals(AssetType.EQUITY, actual.underlyingType());
        Assertions.assertEquals(Exchange.LONDON, actual.getExchange());
        Assertions.assertEquals("US Large-Cap Value Equity", actual.getCategory());
        Assertions.assertEquals("MSCI NA High Div Yield", actual.getIndexCategory());
        Assertions.assertEquals("GBX", actual.getCurrency());

    }

    @Test
    public void testInactiveTickerFilteredOut() throws IOException {
        Instrument.InstrumentLoader loader = Instrument.InstrumentLoader.getInstance();
        // Inactive instrument CC1 should not be present using any of its identifiers
        Assertions.assertFalse(loader.getInstruments().containsKey("CC1"));
        Assertions.assertFalse(loader.getInstruments().containsKey("FR0010713784"));
        Assertions.assertFalse(loader.getInstruments().containsKey("LON:CC1"));

        // Active instrument still available
        Assertions.assertTrue(loader.getInstruments().containsKey("XDND"));

        // Lookups for inactive identifiers should return a manually created placeholder
        Assertions.assertEquals(Source.MANUAL, Instrument.fromString("CC1").getSource());
        Assertions.assertEquals(Source.MANUAL, Instrument.fromString("FR0010713784").getSource());
    }

    @Test
    public void testLoaderSingletonReadsOnce() throws Exception {
        Instrument.InstrumentLoader first = Instrument.InstrumentLoader.getInstance();
        Instrument.InstrumentLoader second = Instrument.InstrumentLoader.getInstance();
        Assertions.assertSame(first, second);

        Field field = Instrument.InstrumentLoader.class.getDeclaredField("instruments");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Instrument> map = (Map<String, Instrument>) field.get(first);
        Instrument removed = map.remove("XDND");

        Instrument.InstrumentLoader third = Instrument.InstrumentLoader.getInstance();
        Assertions.assertSame(first, third);
        @SuppressWarnings("unchecked")
        Map<String, Instrument> mapAfter = (Map<String, Instrument>) field.get(third);
        Assertions.assertFalse(mapAfter.containsKey("XDND"));

        // restore state for other tests
        mapAfter.put("XDND", removed);
    }

    @Test
    public void testPopulateCurrencyResolvesFromFeed() {
        Instrument unresolved = Instrument.fromString("BBAI.N");
        Assertions.assertEquals("UNKNOWN", unresolved.getCurrency());
        Instrument resolved = Instrument.populateCurrency(unresolved);
        Assertions.assertEquals("USD", resolved.getCurrency());
    }

}
