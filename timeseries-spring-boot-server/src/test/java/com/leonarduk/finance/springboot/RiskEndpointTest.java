package com.leonarduk.finance.springboot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RiskEndpoint.class)
class RiskEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private JavaMailSender mailSender;

    @Test
    void historicVarReturnsValue() throws Exception {
        String body = "{\"returns\":[-0.2,-0.1,0.1,0.2]}";

        mockMvc.perform(post("/risk/historic-var")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.var").value(-0.2));
    }

    @Test
    void historicVarReturnsBadRequestWhenMissing() throws Exception {
        Mockito.when(messageSource.getMessage(eq("returns.empty"), any(), any(Locale.class)))
                .thenReturn("empty");

        mockMvc.perform(post("/risk/historic-var")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}

