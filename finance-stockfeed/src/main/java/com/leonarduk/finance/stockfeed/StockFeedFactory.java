package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.google.GoogleFeed;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

public class StockFeedFactory {

	public static StockFeed getDataFeed(final Source source) {
		switch (source) {
			case MANUAL:
				return new CachedStockFeed("db");
//			case Google:
//				return new GoogleFeed();
			case Yahoo:
			default:
				return new YahooFeed();
		}
	}

	public static QuoteFeed getQuoteFeed(final Source yahoo) {
		return new YahooFeed();
	}

}
