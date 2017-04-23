package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.ft.FTFeed;
import com.leonarduk.finance.stockfeed.google.GoogleFeed;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

public class StockFeedFactory {

	private static final YahooFeed YAHOO_FEED = new YahooFeed();
	private static final FTFeed FT_FEED = new FTFeed();
	private static final GoogleFeed GOOGLE_FEED = new GoogleFeed();
	private static final CachedStockFeed CACHED_FEED = new CachedStockFeed("db");

	public static StockFeed getDataFeed(Source source) {
		switch (source) {
		case FT:
			return FT_FEED;
		case MANUAL:
			return CACHED_FEED;
		case Yahoo:
			return YAHOO_FEED;
		case Google:
		default:
			return GOOGLE_FEED;
		}
	}

}
