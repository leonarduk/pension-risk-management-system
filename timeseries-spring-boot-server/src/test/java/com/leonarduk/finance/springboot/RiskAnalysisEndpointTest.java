package com.leonarduk.finance.springboot;

import com.leonarduk.finance.springboot.analysis.RiskAnalysisService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.anyList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskAnalysisEndpoint.class)
class RiskAnalysisEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RiskAnalysisService riskAnalysisService;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void maxDrawdownReturnsValue() throws Exception {
        Mockito.when(riskAnalysisService.calculateMaxDrawdown(anyList())).thenReturn(0.25);

        mockMvc.perform(post("/risk/maxdrawdown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[100,120,80]"))
                .andExpect(status().isOk())
                .andExpect(content().string("0.25"));
    }

    @Test
    void maxDrawdownReturnsServerErrorOnFailure() throws Exception {
        Mockito.when(riskAnalysisService.calculateMaxDrawdown(anyList()))
                .thenThrow(new RuntimeException("failure"));

        mockMvc.perform(post("/risk/maxdrawdown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[100,120,80]"))
                .andExpect(status().isInternalServerError());
    }
}

