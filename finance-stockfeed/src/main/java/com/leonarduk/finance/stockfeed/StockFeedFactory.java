package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.google.GoogleFeed;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

public class StockFeedFactory {

	public static StockFeed getDataFeed(final Source source) {
		switch (source) {
			case MANUAL:
				return new CachedStockFeed("db");
			case Yahoo:
				return new YahooFeed();
			case Google:
			default:
				return new GoogleFeed();
		}
	}

}
