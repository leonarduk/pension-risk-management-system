package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

public class FTFeedTest {

    FTFeed feed;

    @Before
    public void setUp() throws Exception {
        feed = new FTFeed();
    }

    @Test
    @Ignore
    public void get() throws IOException {
        Optional<StockV1> result = feed.get(Instrument.fromString("PHGP"), 1, false);
        if (result.isPresent()) {
            System.out.println(result.get().getHistory());
        }
    }
}