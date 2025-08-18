package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.utils.DataField;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.ta4j.core.Bar;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HistoricalDataServiceTest {

    @Test
    void missingTickerThrows() {
        HistoricalDataService service = new HistoricalDataService(Mockito.mock(StockFeed.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.getRecords(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> service.getRecords(Map.of()));
    }

    @Test
    void categoryMismatchReturnsEmpty() throws IOException {
        StockFeed feed = Mockito.mock(StockFeed.class);
        HistoricalDataService service = new HistoricalDataService(feed);
        List<List<DataField>> result = service.getRecords(Map.of("ticker", "TEST", "category", "EQUITY"));
        Assertions.assertTrue(result.isEmpty());
        Mockito.verifyNoInteractions(feed);
    }

    @Test
    void scalingIsApplied() throws Exception {
        StockFeed feed = Mockito.mock(StockFeed.class);
        HistoricalDataService service = new HistoricalDataService(feed);
        Instrument instrument = Instrument.fromString("TEST");
        Bar bar = new ExtendedHistoricalQuote(instrument, LocalDate.parse("2024-01-01"),
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.valueOf(2), BigDecimal.ONE, 0L, "");
        when(feed.get(eq(instrument), any(LocalDate.class), any(LocalDate.class), anyBoolean(), anyBoolean(), eq(false)))
                .thenReturn(Optional.of(new StockV1(instrument, List.of(bar))));

        Map<String,String> params = Map.of("ticker","TEST","scaling","2.0");
        List<List<DataField>> records = service.getRecords(params);
        Assertions.assertEquals(1, records.size());
        Assertions.assertEquals("4.0", records.get(0).get(4).getValue().toString());
    }

    @Test
    void fromAndToDatesParsedCorrectly() throws Exception {
        StockFeed feed = Mockito.mock(StockFeed.class);
        HistoricalDataService service = new HistoricalDataService(feed);
        Instrument instrument = Instrument.fromString("TEST");
        when(feed.get(eq(instrument), any(LocalDate.class), any(LocalDate.class), anyBoolean(), anyBoolean(), eq(false)))
                .thenReturn(Optional.of(new StockV1(instrument, List.of())));

        Map<String,String> params = Map.of(
                "ticker","TEST",
                "fromDate","2020-01-01",
                "toDate","2020-01-10"
        );
        service.getRecords(params);
        ArgumentCaptor<LocalDate> fromCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(feed).get(eq(instrument), fromCaptor.capture(), toCaptor.capture(), anyBoolean(), anyBoolean(), eq(false));
        Assertions.assertEquals(LocalDate.parse("2020-01-01"), fromCaptor.getValue());
        Assertions.assertEquals(LocalDate.parse("2020-01-10"), toCaptor.getValue());
    }
}

