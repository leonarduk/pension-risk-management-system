package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CachedStockFeedTest {

    @Test
    public void testLoadSeriesReturnsCachedData() throws Exception {
        DataStore store = Mockito.mock(DataStore.class);
        CachedStockFeed feed = new CachedStockFeed(store);
        Instrument instrument = new FxInstrument(Source.MANUAL, "USD", "GBP");
        List<Bar> history = List.of(
                new ExtendedHistoricalQuote(instrument, LocalDate.now().minusDays(1), 1d, 1d, 1d, 1d, 1d, 0L, "orig"));
        StockV1 cached = new StockV1(instrument, history);
        Mockito.when(store.get(instrument, 1000, false)).thenReturn(Optional.of(cached));

        List<Bar> result = feed.loadSeries(cached);

        Assertions.assertEquals(history, result);
    }

    @Test
    public void testLoadSeriesReturnsEmptyWhenCacheMiss() throws Exception {
        DataStore store = Mockito.mock(DataStore.class);
        CachedStockFeed feed = new CachedStockFeed(store);
        Instrument instrument = new FxInstrument(Source.MANUAL, "USD", "GBP");
        StockV1 stock = new StockV1(instrument, List.of());
        Mockito.when(store.get(instrument, 1000, false)).thenReturn(Optional.empty());

        List<Bar> result = feed.loadSeries(stock);

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testStoreSeriesMergesExistingData() throws Exception {
        DataStore store = Mockito.mock(DataStore.class);
        CachedStockFeed feed = new CachedStockFeed(store);
        Instrument instrument = new FxInstrument(Source.MANUAL, "USD", "GBP");

        List<Bar> originalHistory = List.of(
                new ExtendedHistoricalQuote(instrument, LocalDate.now().minusDays(2), 1d, 1d, 1d, 1d, 1d, 0L, "orig"));
        StockV1 originalStock = new StockV1(instrument, originalHistory);

        List<Bar> newHistory = List.of(
                new ExtendedHistoricalQuote(instrument, LocalDate.now().minusDays(1), 2d, 2d, 2d, 2d, 2d, 0L, "new"));
        StockV1 newStock = new StockV1(instrument, newHistory);

        Mockito.when(store.contains(newStock)).thenReturn(true);
        Mockito.when(store.get(instrument, 1000, false)).thenReturn(Optional.of(originalStock));

        feed.storeSeries(newStock);

        Mockito.verify(store).storeSeries(newStock);
        Assertions.assertEquals(2, newStock.getHistory().size());
    }

    @Test
    public void testStoreSeriesWithoutExistingData() throws Exception {
        DataStore store = Mockito.mock(DataStore.class);
        CachedStockFeed feed = new CachedStockFeed(store);
        Instrument instrument = new FxInstrument(Source.MANUAL, "USD", "GBP");
        StockV1 stock = new StockV1(instrument, List.of());

        Mockito.when(store.contains(stock)).thenReturn(false);

        feed.storeSeries(stock);

        Mockito.verify(store).storeSeries(stock);
        Mockito.verify(store, Mockito.never()).get(Mockito.<Instrument>any(), Mockito.anyInt(), Mockito.anyBoolean());
    }
}

