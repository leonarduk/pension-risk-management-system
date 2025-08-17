package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class FxFeedTest {

    @Test
    public void testGetFxSeriesReturnsData() {
        FxFeed mockFeed = Mockito.mock(FxFeed.class);
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now();
        Instrument instrument = new FxInstrument(Source.MANUAL, "USD", "GBP");
        List<Bar> series = List.of(
                new ExtendedHistoricalQuote(instrument, from, 1d, 1d, 1d, 1d, 1d, 0L, "test"),
                new ExtendedHistoricalQuote(instrument, to, 1d, 1d, 1d, 1d, 1d, 0L, "test"));
        Mockito.when(mockFeed.getFxSeries("USD", "GBP", from, to)).thenReturn(series);

        List<Bar> result = mockFeed.getFxSeries("USD", "GBP", from, to);
        Assertions.assertEquals(series, result);
    }

    @Test
    public void testGetFxSeriesUnknownPairReturnsEmptyList() {
        FxFeed mockFeed = Mockito.mock(FxFeed.class);
        LocalDate from = LocalDate.now().minusDays(1);
        LocalDate to = LocalDate.now();
        Mockito.when(mockFeed.getFxSeries("AAA", "BBB", from, to)).thenReturn(Collections.emptyList());

        List<Bar> result = mockFeed.getFxSeries("AAA", "BBB", from, to);
        Assertions.assertTrue(result.isEmpty());
    }
}

