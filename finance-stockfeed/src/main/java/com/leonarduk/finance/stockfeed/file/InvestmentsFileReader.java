package com.leonarduk.finance.stockfeed.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.ResourceTools;

public class InvestmentsFileReader {
	private final static Logger logger = Logger.getLogger(InvestmentsFileReader.class.getName());

	private static Position createPosition(final List<String> fields) {
		if (fields.size() < 2) {
			InvestmentsFileReader.logger.warning("not enough detsails: " + fields);
			return null;
		}
		final int portfolioIdx = 0;
		final int isinIdx = 1;
		final int amountIndex = 2;
		final String symbol = fields.get(isinIdx);
		Instrument instrument;
		try {
			instrument = Instrument.fromString(symbol);
		}
		catch (final IOException e) {
			e.printStackTrace();
			instrument = Instrument.UNKNOWN;
		}
		final Optional<Stock> stock = Optional.of(new Stock(instrument));
		return new Position(fields.get(portfolioIdx), instrument,
		        new BigDecimal(fields.get(amountIndex)), stock, symbol);
	}

	private static Stock createStock(final List<String> fields) {
		if (fields.size() < 2) {
			InvestmentsFileReader.logger.warning("not enough details: " + fields);
			return null;
		}
		final int isinIdx = 1;
		Instrument instrument;
		try {
			instrument = Instrument.fromString(fields.get(isinIdx));
		}
		catch (final IOException e) {
			e.printStackTrace();
			instrument = Instrument.UNKNOWN;
		}

		return StockFeed.createStock(instrument);
	}

	public static List<Position> getPositionsFromCSVFile(final String filePath) throws IOException {
		return ResourceTools.readCsvRecords(filePath).stream().skip(1)
		        .map(InvestmentsFileReader::createPosition).collect(Collectors.toList());
	}

}
