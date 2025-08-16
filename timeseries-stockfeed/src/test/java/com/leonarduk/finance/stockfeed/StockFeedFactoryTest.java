package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.feed.ft.FTFeed;
import com.leonarduk.finance.stockfeed.feed.stooq.StooqFeed;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

public class StockFeedFactoryTest {

    @Test
    public void testManualSourceUsesProvidedDataStore() {
        DataStore dataStore = Mockito.mock(DataStore.class);
        Mockito.when(dataStore.isAvailable()).thenReturn(true);
        StockFeedFactory factory = new StockFeedFactory(dataStore);

        StockFeed feed = factory.getDataFeed(Source.MANUAL);

        Assert.assertTrue(feed instanceof CachedStockFeed);
        Mockito.verify(dataStore).isAvailable();
    }

    @Test
    public void testManualSourceFallsBackWhenDataStoreUnavailable() throws Exception {
        DataStore dataStore = Mockito.mock(DataStore.class);
        Mockito.when(dataStore.isAvailable()).thenReturn(false);
        StockFeedFactory factory = new StockFeedFactory(dataStore);

        StockFeed feed = factory.getDataFeed(Source.MANUAL);

        Assert.assertTrue(feed instanceof CachedStockFeed);

        Field field = CachedStockFeed.class.getDeclaredField("dataStore");
        field.setAccessible(true);
        DataStore used = (DataStore) field.get(feed);
        Assert.assertNotSame(dataStore, used);
    }

    @Test
    public void testReturnsSpecificFeedsForSources() {
        StockFeedFactory factory = new StockFeedFactory(Mockito.mock(DataStore.class));

        Assert.assertTrue(factory.getDataFeed(Source.FT) instanceof FTFeed);
        Assert.assertTrue(factory.getDataFeed(Source.ALPHAVANTAGE) instanceof AlphavantageFeed);
        Assert.assertTrue(factory.getDataFeed(Source.STOOQ) instanceof StooqFeed);
    }

    @Test
    public void testReturnsStooqFeedForUnknownSource() {
        StockFeedFactory factory = new StockFeedFactory(Mockito.mock(DataStore.class));
        StockFeed feed = factory.getDataFeed(Source.GOOGLE);
        Assert.assertTrue(feed instanceof StooqFeed);
    }
}

