package com.leonarduk.finance.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ResourceToolsTest {

    @Test
    public void testReadCsvRecords() throws IOException {
        List<List<String>> records = ResourceTools.readCsvRecords("sample.csv");

        assertEquals(Arrays.asList("A", "B", "C"), records.get(0));
        assertEquals(Arrays.asList("1", "2", "3"), records.get(1));
        assertEquals(Arrays.asList("4", "5", "6"), records.get(2));
    }
}

