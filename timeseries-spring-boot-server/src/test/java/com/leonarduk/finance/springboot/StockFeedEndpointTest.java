package com.leonarduk.finance.springboot;

import com.leonarduk.finance.stockfeed.AbstractStockFeed;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.StockFeedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StockFeedEndpoint.class)
class StockFeedEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockFeed stockFeed;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void displayHistoryReturnsHtmlTable() throws Exception {
        Instrument instrument = Instrument.CASH;
        StockV1 stock = AbstractStockFeed.createStock(instrument, Collections.emptyList());
        Mockito.when(stockFeed.get(argThat(i -> i.getCode().equals("CASH")), eq(LocalDate.parse("2024-01-01")),
                eq(LocalDate.parse("2024-01-02")), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(stock));

        mockMvc.perform(get("/stock/ticker/{ticker}", "CASH")
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-01-02")
                        .param("interpolate", "true")
                        .param("cleanDate", "true")
                        .param("category", "Cash"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("<html")));
    }

    @Test
    void displayHistoryAsJsonFiltersOutNonMatchingCategory() throws Exception {
        Instrument instrument = Instrument.CASH;
        StockV1 stock = AbstractStockFeed.createStock(instrument, Collections.emptyList());
        Mockito.when(stockFeed.get(argThat(i -> i.getCode().equals("CASH")), eq(LocalDate.parse("2024-01-01")),
                eq(LocalDate.parse("2024-01-02")), anyBoolean(), anyBoolean(), anyBoolean()))
                .thenReturn(Optional.of(stock));

        mockMvc.perform(post("/stock/ticker")
                        .param("ticker", "CASH")
                        .param("fromDate", "2024-01-01")
                        .param("toDate", "2024-01-02")
                        .param("interpolate", "true")
                        .param("cleanDate", "true")
                        .param("category", "EQUITY"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
    }

    @Test
    void displayHistoryReturnsServiceUnavailableWhenFeedFails() throws Exception {
        Mockito.when(stockFeed.get(any(Instrument.class), any(LocalDate.class), any(LocalDate.class),
                anyBoolean(), anyBoolean(), anyBoolean()))
                .thenThrow(new StockFeedException("all feeds failed"));

        mockMvc.perform(get("/stock/ticker/{ticker}", "CASH"))
                .andExpect(status().isServiceUnavailable());
    }
}

