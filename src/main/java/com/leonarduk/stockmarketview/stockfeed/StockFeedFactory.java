package com.leonarduk.stockmarketview.stockfeed;

import com.leonarduk.stockmarketview.stockfeed.ft.FTFeed;
import com.leonarduk.stockmarketview.stockfeed.google.GoogleFeed;
import com.leonarduk.stockmarketview.stockfeed.yahoo.YahooFeed;

public class StockFeedFactory {

	private static final YahooFeed YAHOO_FEED = new YahooFeed();
	private static final FTFeed FT_FEED = new FTFeed();
	private static final GoogleFeed GOOGLE_FEED = new GoogleFeed();

	public static StockFeed getDataFeed(Source source) {
		switch (source) {
		case Google:
			return GOOGLE_FEED;
		case FT:
			return FT_FEED;
		default:
			return YAHOO_FEED;
		}
	}

}
