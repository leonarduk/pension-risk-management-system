package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.YahooFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;

public class StockFeedFactory {

    public static StockFeed getDataFeed(final Source source) {
        switch (source) {
            case MANUAL:
                //TODO add details for non local DB
                String bucket = "portfolio";
                String org = "leonarduk";
                String token = "fX6n4UJqXg7Aq2OY7MerSxPB-624Sqwua4LVyRadKHlT91q3Wf-RopTm7YHZroT0actf46RrfXs9lR4i08sA2w==";
                String serverUrl = "http://localhost:8086";

                final InfluxDBDataStore dataStore = new InfluxDBDataStore(bucket, org, token, serverUrl);
                if (dataStore.isAvailable())
                    return new CachedStockFeed(dataStore);

                // If influx db not available,  use local files
                //TODO log
                return new CachedStockFeed(new FileBasedDataStore("db"));
//			case Google:
//				return new GoogleFeed();
            case YAHOO:
                return new YahooFeed();
            case ALPHAVANTAGE:
            default:
                return new AlphavantageFeed();
        }
    }

    public static QuoteFeed getQuoteFeed(final Source yahoo) {
        return new YahooFeed();
    }

}
