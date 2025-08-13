package com.leonarduk.aws;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryRunnerTest {

    @Test
    void getResultsThrowsWhenTickerMissing() {
        QueryRunner runner = new QueryRunner();
        Map<String, String> params = new HashMap<>();
        assertThrows(IllegalArgumentException.class, () -> runner.getResults(params));
    }
}
