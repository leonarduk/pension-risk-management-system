package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class FTFeedTest {

    FTFeed feed;
    @Before
    public void setUp() throws Exception {
        feed = new FTFeed();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() throws IOException {
        feed.get(Instrument.fromString("PHGP"), 1,false);
    }

    @Test
    public void testGet() {
    }

    @Test
    public void getSource() {
    }

    @Test
    public void isAvailable() {
    }
}