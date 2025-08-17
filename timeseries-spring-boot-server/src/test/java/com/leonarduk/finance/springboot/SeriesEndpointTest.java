package com.leonarduk.finance.springboot;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SeriesEndpoint.class)
class SeriesEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockFeed stockFeed;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void mapSeriesReturnsMappedData() throws Exception {
        Instrument srcInstrument = Instrument.CASH;
        Instrument tgtInstrument = Instrument.UNKNOWN;

        ZonedDateTime d1 = LocalDate.of(2024,1,1).atStartOfDay(ZoneId.systemDefault());
        ZonedDateTime d2 = LocalDate.of(2024,1,2).atStartOfDay(ZoneId.systemDefault());
        Bar src1 = new BaseBar(Duration.ofDays(1), d1,
                10, 10, 10, 10, 1);
        Bar src2 = new BaseBar(Duration.ofDays(1), d2,
                20, 20, 20, 20, 1);
        StockV1 srcStock = Mockito.mock(StockV1.class);
        Mockito.when(srcStock.getHistory()).thenReturn(Arrays.asList(src1, src2));

        Bar tgt1 = new BaseBar(Duration.ofDays(1), d1,
                100, 100, 100, 100, 1);
        Bar tgt2 = new BaseBar(Duration.ofDays(1), d2,
                200, 200, 200, 200, 1);
        StockV1 tgtStock = Mockito.mock(StockV1.class);
        Mockito.when(tgtStock.getHistory()).thenReturn(Arrays.asList(tgt1, tgt2));

        Mockito.when(stockFeed.get(eq(srcInstrument), eq(1), eq(true), eq(true), eq(false)))
                .thenReturn(Optional.of(srcStock));
        Mockito.when(stockFeed.get(eq(tgtInstrument), eq(1), eq(true), eq(true), eq(false)))
                .thenReturn(Optional.of(tgtStock));

        mockMvc.perform(post("/series/map")
                        .param("source", "CASH")
                        .param("target", "UNKNOWN")
                        .param("years", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapped['2024-01-01']").value(100.0))
                .andExpect(jsonPath("$.mapped['2024-01-02']").value(200.0));
    }

    @Test
    void mapSeriesReturnsEmptyWhenDataMissing() throws Exception {
        Mockito.when(stockFeed.get(any(Instrument.class), anyInt(), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/series/map")
                        .param("source", "CASH")
                        .param("target", "UNKNOWN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapped").isEmpty());
    }
}

