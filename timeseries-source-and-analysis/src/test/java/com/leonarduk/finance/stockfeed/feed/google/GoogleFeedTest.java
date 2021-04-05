package com.leonarduk.finance.stockfeed.feed.google;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;

public class GoogleFeedTest {

	private GoogleFeed feed;

	@Before
	public void setUp() throws Exception {
		this.feed = new GoogleFeed();
	}

	@Test
	public final void testCreateRequest() throws IOException {
		Assert.assertEquals(GoogleFeed.BASE_URL,
		        this.feed.createRequest(GoogleFeed.BASE_URL).url().toString());
	}

	@Test
	public final void testGetQueryName() {
		Assert.assertEquals("UNKNOWN",
		        this.feed.getQueryName(Instrument.UNKNOWN));
	}

	@Test
	public final void testGetSource() {
		Assert.assertEquals(Source.GOOGLE, this.feed.getSource());
	}

	@Test
	public final void testParseDate() throws ParseException {
		Assert.assertEquals("1975-03-22", new SimpleDateFormat("yyyy-MM-dd")
		        .format(this.feed.parseDate("22-Mar-1975")));
	}

}
