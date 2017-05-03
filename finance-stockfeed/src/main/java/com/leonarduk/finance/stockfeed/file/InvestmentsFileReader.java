package com.leonarduk.finance.stockfeed.file;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.ResourceTools;

import eu.verdelhan.ta4j.Decimal;
import yahoofinance.Stock;

public class InvestmentsFileReader {
	private final static Logger logger = Logger.getLogger(InvestmentsFileReader.class.getName());

	private static Position createPosition(final List<String> list) {
		if (list.size() < 2) {
			logger.warning("not enough details: " + list);
			return null;
		}
		final int portfolioIdx = 0;
		final int isinIdx = 1;
		final int amountIndex = 2;
		final String symbol = list.get(isinIdx);
		final Instrument instrument = Instrument.fromString(symbol);
		final Optional<Stock> stock = Optional.of(new Stock(instrument));
		return new Position(list.get(portfolioIdx), instrument, Decimal.valueOf(list.get(amountIndex)), stock, symbol);
	}

	private static Stock createStock(final List<String> list) {
		if (list.size() < 2) {
			logger.warning("not enough details: " + list);
			return null;
		}
		final int isinIdx = 1;
		return StockFeed.createStock(Instrument.fromString(list.get(isinIdx)));
	}

	public static List<Position> getPositionsFromCSVFile(final String filePath) throws IOException {
		final List<List<String>> stream = ResourceTools.readCsvRecords(filePath);
		return stream.stream().skip(1).map(InvestmentsFileReader::createPosition).collect(Collectors.toList());
	}

	public static List<Stock> getStocksFromCSVFile(final String filePath) throws IOException {
		final List<List<String>> stream = ResourceTools.readCsvRecords(filePath);
		// List<String> inputList = stream.get(0);
		// Map<String, Integer> outputMap = new HashMap<>();
		// for (int j = 0; j < inputList.size(); j++) {
		// outputMap.put(inputList.get(j), 1 + j);
		// }
		return stream.stream().skip(1).map(InvestmentsFileReader::createStock).collect(Collectors.toList());
	}
}
