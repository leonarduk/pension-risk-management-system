package com.leonarduk.finance.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.core.io.ClassPathResource;

public class ResourceTools {

	public static InputStream getResourceAsInputStream(final String filePath) throws IOException {
		return new ClassPathResource(filePath).getInputStream();
	}

	public static List<String> getResourceAsLines(final String filePath) throws IOException {
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(getResourceAsInputStream(filePath)))) {
			return buffer.lines().collect(Collectors.toList());
		}
	}

	public static List<List<String>> readCsvRecords(final String fileName) throws IOException {
		try (Stream<String> lines = getResourceAsLines(fileName).stream()) {
			return lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());
		}
	}
}
