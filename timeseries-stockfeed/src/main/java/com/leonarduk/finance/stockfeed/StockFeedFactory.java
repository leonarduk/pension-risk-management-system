package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.feed.ft.FTFeed;
import com.leonarduk.finance.stockfeed.feed.stooq.StooqFeed;
import com.leonarduk.finance.stockfeed.file.FileBasedDataStore;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                log.warn("Primary data store unavailable. Falling back to FileBasedDataStore");
                return new CachedStockFeed(new FileBasedDataStore("db"));
            case FT:
                return new FTFeed();
            case ALPHAVANTAGE:
                return new AlphavantageFeed();
            case STOOQ:
            default:
                return new StooqFeed();
        }
    }

}
