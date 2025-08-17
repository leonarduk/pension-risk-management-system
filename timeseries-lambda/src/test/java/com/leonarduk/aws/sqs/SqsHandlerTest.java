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
        this.handler = new SqsHandler();
    }

    @Test
    void getStringStringMap() {
        final String message = """
                {
                    "ticker": "PHGP"
                }
                """;
        final Map<String, String> actualMap = handler.getParameterMap(message);
        final Map<String,String> expectedMap = ImmutableMap.of("ticker","PHGP", "interpolate","false", "cleanData","false", "years","0");
        Assertions.assertEquals(expectedMap, actualMap);
    }
}