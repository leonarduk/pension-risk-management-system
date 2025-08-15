package com.leonarduk.finance.springboot;

import com.leonarduk.finance.springboot.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CorsConfigTest.TestController.class)
@Import(CorsConfig.class)
@TestPropertySource(properties = "cors.allowed-origins=http://example.com")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaMailSender mailSender;

    @RestController
    @RequestMapping("/stock")
    static class TestController {
        @GetMapping
        public String get() {
            return "ok";
        }
    }

    @Test
    void corsHeadersPresent() throws Exception {
        mockMvc.perform(get("/stock").header("Origin", "http://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://example.com"));
    }
}
