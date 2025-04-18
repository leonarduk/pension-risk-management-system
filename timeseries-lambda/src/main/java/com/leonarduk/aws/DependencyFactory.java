package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import software.amazon.awssdk.regions.Region;

public class DependencyFactory {

    private DependencyFactory() {
    }


    /**
     * Return an instance of a stock feed. This is a singleton, so subsequent calls will return the same instance.
     *
     * @return A stock feed
     */
    public static StockFeed stockFeed() {
        return new IntelligentStockFeed(new S3DataStore("timeseries-leonarduk",
                "timeseries", Region.EU_WEST_1.toString()));
    }
}
