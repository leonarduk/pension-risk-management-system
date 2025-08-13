package com.leonarduk.aws;

import com.leonarduk.finance.stockfeed.StockFeed;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DependencyFactoryTest {
    @Test
    void stockFeedReturnsSingleton() {
        StockFeed first = DependencyFactory.stockFeed();
        StockFeed second = DependencyFactory.stockFeed();
        Assertions.assertSame(first, second);
    }
}
