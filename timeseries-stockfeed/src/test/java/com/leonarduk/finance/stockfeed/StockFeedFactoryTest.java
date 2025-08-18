package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.alphavantage.AlphavantageFeed;
import com.leonarduk.finance.stockfeed.feed.ft.FTFeed;
import com.leonarduk.finance.stockfeed.feed.stooq.StooqFeed;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class StockFeedFactoryTest {

    @Test
    public void testManualSourceUsesProvidedDataStore() {
        DataStore dataStore = Mockito.mock(DataStore.class);
        Mockito.when(dataStore.isAvailable()).thenReturn(true);
        StockFeedFactory factory = new StockFeedFactory(dataStore);

        StockFeed feed = factory.getDataFeed(Source.MANUAL);

        Assertions.assertTrue(feed instanceof CachedStockFeed);
        Mockito.verify(dataStore).isAvailable();
    }

    @Test
    public void testManualSourceFallsBackWhenDataStoreUnavailable() throws Exception {
        DataStore dataStore = Mockito.mock(DataStore.class);
        Mockito.when(dataStore.isAvailable()).thenReturn(false);
        StockFeedFactory factory = new StockFeedFactory(dataStore);

        StockFeed feed = factory.getDataFeed(Source.MANUAL);

        Assertions.assertTrue(feed instanceof CachedStockFeed);

        Field field = CachedStockFeed.class.getDeclaredField("dataStore");
        field.setAccessible(true);
        DataStore used = (DataStore) field.get(feed);
        Assertions.assertNotSame(dataStore, used);
    }

    @Test
    public void testReturnsSpecificFeedsForSources() {
        StockFeedFactory factory = new StockFeedFactory(Mockito.mock(DataStore.class));

        Assertions.assertTrue(factory.getDataFeed(Source.FT) instanceof FTFeed);
        Assertions.assertTrue(factory.getDataFeed(Source.ALPHAVANTAGE) instanceof AlphavantageFeed);
        Assertions.assertTrue(factory.getDataFeed(Source.STOOQ) instanceof StooqFeed);
    }

    @Test
    public void testReturnsStooqFeedForUnknownSource() {
        StockFeedFactory factory = new StockFeedFactory(Mockito.mock(DataStore.class));
        StockFeed feed = factory.getDataFeed(Source.GOOGLE);
        Assertions.assertTrue(feed instanceof StooqFeed);
    }

    @Test
    public void testLogsWarningWhenDataStoreUnavailable() {
        DataStore dataStore = Mockito.mock(DataStore.class);
        Mockito.when(dataStore.isAvailable()).thenReturn(false);
        StockFeedFactory factory = new StockFeedFactory(dataStore);

        Logger logger = (Logger) LoggerFactory.getLogger(StockFeedFactory.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        factory.getDataFeed(Source.MANUAL);

        logger.detachAppender(listAppender);

        boolean warningLogged = listAppender.list.stream()
                .anyMatch(event -> event.getLevel().equals(Level.WARN)
                        && event.getFormattedMessage().contains("Primary data store unavailable"));
        Assertions.assertTrue(warningLogged, "Warning should be logged when data store is unavailable");
    }
}

