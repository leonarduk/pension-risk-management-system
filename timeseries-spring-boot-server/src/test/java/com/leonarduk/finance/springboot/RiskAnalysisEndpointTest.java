package com.leonarduk.finance.springboot;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leonarduk.finance.springboot.analysis.RiskAnalysisService;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RiskAnalysisEndpoint.class)
class RiskAnalysisEndpointTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private RiskAnalysisService riskAnalysisService;

    @Test
    void returnsMaxDrawdown() throws Exception {
        List<Double> prices = Arrays.asList(100.0, 120.0, 80.0);
        when(riskAnalysisService.calculateMaxDrawdown(prices)).thenReturn(0.3333);
        ObjectMapper mapper = new ObjectMapper();

        mockMvc
                .perform(
                        post("/risk/maxdrawdown")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(prices)))
                .andExpect(status().isOk())
                .andExpect(content().string("0.3333"));
    }
}

