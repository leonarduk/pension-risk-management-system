package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.feed.ft.FTFeed;
import com.leonarduk.finance.stockfeed.feed.stooq.StooqFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.YahooFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;

public class StockFeedFactory {

    private final DataStore dataStore;

    public StockFeedFactory(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public StockFeed getDataFeed(final Source source) {
        switch (source) {
            case MANUAL:
                if (dataStore.isAvailable())
                    return new CachedStockFeed(dataStore);

                // If db not available,  use local files
                //TODO log
                return new CachedStockFeed(new FileBasedDataStore("db"));
            case YAHOO:
                return new YahooFeed();
            case FT:
                return new FTFeed();
            case STOOQ:
                return new StooqFeed();
            case ALPHAVANTAGE:
            default:
                return new AlphavantageFeed();
        }
    }

    public QuoteFeed getQuoteFeed(final Source yahoo) {
        return new YahooFeed();
    }

}
