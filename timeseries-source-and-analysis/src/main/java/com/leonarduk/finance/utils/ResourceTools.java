package com.leonarduk.finance.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResourceTools {

	public static InputStream getResourceAsInputStream(final String filePath) {
		ClassLoader classLoader = ResourceTools.class.getClassLoader();
		return classLoader.getResourceAsStream(filePath);
	}

	public static List<String> getResourceAsLines(final String filePath) throws IOException {
		try (BufferedReader buffer = new BufferedReader(
				new InputStreamReader(ResourceTools.getResourceAsInputStream(filePath)))) {
			return buffer.lines().collect(Collectors.toList());
		}
	}

	public static List<List<String>> readCsvRecords(final String fileName) throws IOException {
		try (Stream<String> lines = ResourceTools.getResourceAsLines(fileName).stream()) {
			return lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());
		}
	}
}
