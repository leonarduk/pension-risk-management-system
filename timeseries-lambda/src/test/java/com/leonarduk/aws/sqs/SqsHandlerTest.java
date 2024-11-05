package com.leonarduk.aws.sqs;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

class SqsHandlerTest {
    SqsHandler handler;
    @BeforeEach
    void setUp() {
        handler = new SqsHandler();
    }

    @Test
    void getStringStringMap() {
        String message = """
                {
                    "ticker": "PHGP"
                }
                """;
        Map<String, String> actualMap = this.handler.getParameterMap(message);
        Map<String,String> expectedMap = ImmutableMap.of("ticker","PHGP", "interpolate","false", "cleanData","false", "years","0");
        Assertions.assertEquals(expectedMap, actualMap);
    }
}