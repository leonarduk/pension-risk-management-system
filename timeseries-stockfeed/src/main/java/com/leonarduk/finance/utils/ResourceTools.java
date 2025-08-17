package com.leonarduk.finance.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResourceTools {
    ;

    public static InputStream getResourceAsInputStream(String filePath) {
        final ClassLoader classLoader = ResourceTools.class.getClassLoader();
        return classLoader.getResourceAsStream(filePath);
    }

    public static List<String> getResourceAsLines(String filePath) throws IOException {
        try (final BufferedReader buffer = new BufferedReader(
                new InputStreamReader(getResourceAsInputStream(filePath), StandardCharsets.UTF_8))) {
            return buffer.lines().collect(Collectors.toList());
        }
    }

    public static List<List<String>> readCsvRecords(String fileName) throws IOException {
        try (final Stream<String> lines = getResourceAsLines(fileName).stream()) {
            return lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());
        }
    }
}
