package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.yahoofinance.YahooFeed;

public class StockFeedFactory {

	public static StockFeed getDataFeed(final Source source) {
		switch (source) {
		case MANUAL:
			return new CachedStockFeed("db");
//			case Google:
//				return new GoogleFeed();
		case Yahoo:
			return new YahooFeed();
		default:
			return new AlphavantageFeed();
		}
	}

	public static QuoteFeed getQuoteFeed(final Source yahoo) {
		return new YahooFeed();
	}

}
