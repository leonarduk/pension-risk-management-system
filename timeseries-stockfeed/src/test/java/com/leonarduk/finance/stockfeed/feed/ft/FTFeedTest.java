package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class FTFeedTest {

    FTFeed feed;

    @BeforeEach
    public void setUp() throws Exception {
        feed = new FTFeed();
    }

    @Test
    @Disabled
    public void get() throws IOException {
        Optional<StockV1> result = feed.get(Instrument.fromString("PHGP"), 1, false);
        if (result.isPresent()) {
            log.info("{}", result.get().getHistory());
        }
    }
}
