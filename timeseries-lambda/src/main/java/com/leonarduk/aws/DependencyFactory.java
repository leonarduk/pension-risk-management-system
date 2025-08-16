package com.leonarduk.aws;

import com.amazonaws.regions.Regions;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;

public class DependencyFactory {

    private DependencyFactory() {
    }

    private static volatile StockFeed instance;

    /**
     * Return an instance of a stock feed. This is a singleton, so subsequent calls will return the same instance.
     *
     * @return A stock feed
     */
    public static StockFeed stockFeed() {
        if (instance == null) {
            synchronized (DependencyFactory.class) {
                if (instance == null) {
                    instance =
                            new IntelligentStockFeed(
                                    new S3DataStore(
                                            "timeseries-leonarduk",
                                            "timeseries",
                                            Regions.EU_WEST_1.getName()));
                }
            }
        }
        return instance;
    }
}
