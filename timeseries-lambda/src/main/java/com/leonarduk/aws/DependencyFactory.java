package com.leonarduk.aws;

import com.amazonaws.regions.Regions;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;

public enum DependencyFactory {
    ;

    private static volatile StockFeed instance;

    /**
     * Return an instance of a stock feed. This is a singleton, so subsequent calls will return the same instance.
     *
     * @return A stock feed
     */
    public static StockFeed stockFeed() {
        if (null == instance) {
            synchronized (DependencyFactory.class) {
                if (null == instance) {
                    DependencyFactory.instance =
                            new IntelligentStockFeed(
                                    new S3DataStore(
                                            "timeseries-leonarduk",
                                            "timeseries",
                                            Regions.EU_WEST_1.getName()));
                }
            }
        }
        return DependencyFactory.instance;
    }
}
