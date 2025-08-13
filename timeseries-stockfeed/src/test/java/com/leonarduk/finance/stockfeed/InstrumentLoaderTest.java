package com.leonarduk.finance.stockfeed;

import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import static org.junit.Assert.*;

public class InstrumentLoaderTest {

    @Test
    public void testInactiveTickersFilteredOut() throws IOException, URISyntaxException {
        Instrument.InstrumentLoader loader = new Instrument.InstrumentLoader();
        loader.init("resources/data/instruments_active_test.csv");
        Map<String, Instrument> instruments = loader.getInstruments();

        assertTrue("Active instrument should be present", instruments.containsKey("AAA"));
        assertFalse("Inactive instrument should be filtered out", instruments.containsKey("BBB"));
    }
}

