package com.leonarduk.stockmarketview.stockfeed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;

import yahoofinance.Stock;

public class SymbolFileReader {
	private final static Logger logger = Logger.getLogger(SymbolFileReader.class.getName());

	static List<List<String>> readCsvRecords(String fileName) throws IOException {
		logger.info("Parse file:" + fileName);
		try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
			return lines.map(line -> Arrays.asList(line.split(","))).collect(Collectors.toList());
		}
	}

	public static List<Stock> getStocksFromCSVFile(String filePath) throws IOException {
		List<List<String>> stream = readCsvRecords(filePath);
		// List<String> inputList = stream.get(0);
		// Map<String, Integer> outputMap = new HashMap<>();
		// for (int j = 0; j < inputList.size(); j++) {
		// outputMap.put(inputList.get(j), 1 + j);
		// }
		return stream.stream().skip(1).map(SymbolFileReader::createStock).collect(Collectors.toList());
	}

	private static Stock createStock(List<String> list) {
		if (list.size() < 2) {
			logger.warning("not enough details: " + list);
			return null;
		}
		int isinIdx = 1;
		int nameIndex = 0;
		return StockFeed.createStock(EXCHANGE.London, list.get(isinIdx), list.get(nameIndex));
	}
}
