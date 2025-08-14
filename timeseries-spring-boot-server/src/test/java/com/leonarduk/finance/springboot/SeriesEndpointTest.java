package com.leonarduk.finance.springboot;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.ExtendedHistoricalQuote;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.ta4j.core.Bar;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeriesEndpoint.class)
class SeriesEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockFeed stockFeed;

    @Test
    void mapSeriesAlignsSourceToTarget() throws Exception {
        List<Bar> srcQuotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-01"), 100, 100, 100, 100, 100, 0, ""),
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-02"), 110, 110, 110, 110, 110, 0, ""),
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-04"), 120, 120, 120, 120, 120, 0, "")
        );
        List<Bar> tgtQuotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-01"), 50, 50, 50, 50, 50, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-02"), 55, 55, 55, 55, 55, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-03"), 53, 53, 53, 53, 53, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-04"), 58, 58, 58, 58, 58, 0, "")
        );
        StockV1 srcStock = AbstractStockFeed.createStock(Instrument.CASH, srcQuotes);
        StockV1 tgtStock = AbstractStockFeed.createStock(Instrument.UNKNOWN, tgtQuotes);

        Mockito.when(stockFeed.get(eq(Instrument.CASH), anyInt(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(srcStock));
        Mockito.when(stockFeed.get(eq(Instrument.UNKNOWN), anyInt(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(tgtStock));

        mockMvc.perform(post("/series/map")
                        .param("source", "CASH")
                        .param("target", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapped['2023-01-01']").value(50.0))
                .andExpect(jsonPath("$.mapped['2023-01-03']").value(55.0))
                .andExpect(jsonPath("$.mapped['2023-01-04']").value(60.0));
    }

    @Test
    void mapSeriesHandlesDuplicateDates() throws Exception {
        List<Bar> srcQuotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-01"), 100, 100, 100, 100, 100, 0, ""),
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-02"), 110, 110, 110, 110, 110, 0, ""),
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-02"), 115, 115, 115, 115, 115, 0, ""),
                new ExtendedHistoricalQuote(Instrument.CASH, LocalDate.parse("2023-01-04"), 120, 120, 120, 120, 120, 0, "")
        );
        List<Bar> tgtQuotes = Arrays.asList(
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-01"), 50, 50, 50, 50, 50, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-02"), 55, 55, 55, 55, 55, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-02"), 57, 57, 57, 57, 57, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-03"), 53, 53, 53, 53, 53, 0, ""),
                new ExtendedHistoricalQuote(Instrument.UNKNOWN, LocalDate.parse("2023-01-04"), 58, 58, 58, 58, 58, 0, "")
        );
        StockV1 srcStock = AbstractStockFeed.createStock(Instrument.CASH, srcQuotes);
        StockV1 tgtStock = AbstractStockFeed.createStock(Instrument.UNKNOWN, tgtQuotes);

        Mockito.when(stockFeed.get(eq(Instrument.CASH), anyInt(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(srcStock));
        Mockito.when(stockFeed.get(eq(Instrument.UNKNOWN), anyInt(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(tgtStock));

        mockMvc.perform(post("/series/map")
                        .param("source", "CASH")
                        .param("target", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapped['2023-01-01']").value(50.0))
                .andExpect(jsonPath("$.mapped['2023-01-02']").value(55.0))
                .andExpect(jsonPath("$.mapped['2023-01-03']").value(55.0))
                .andExpect(jsonPath("$.mapped['2023-01-04']").value(60.0));
    }
}
