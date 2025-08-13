package com.leonarduk.finance.springboot;

import com.leonarduk.finance.springboot.config.CorsConfig;
import com.leonarduk.finance.stockfeed.StockFeed;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockFeedEndpoint.class)
@Import(CorsConfig.class)
@TestPropertySource(properties = "cors.allowed-origins=http://example.com")
class StockFeedCorsTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockFeed stockFeed;

    @Test
    void corsHeadersPresentOnStockEndpoint() throws Exception {
        Mockito.doReturn(Optional.empty())
                .when(stockFeed).get(any(), anyInt(), anyBoolean());

        mockMvc.perform(get("/stock/price/CASH")
                        .header("Origin", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://example.com"));
    }
}
