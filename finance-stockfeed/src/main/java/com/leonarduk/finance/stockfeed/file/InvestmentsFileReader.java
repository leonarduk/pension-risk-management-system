package com.leonarduk.finance.stockfeed.file;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

import com.leonarduk.finance.portfolio.Position;
import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.StockV1;
import com.leonarduk.finance.utils.ResourceTools;

public class InvestmentsFileReader {
	private final static Logger logger = LoggerFactory
	        .getLogger(InvestmentsFileReader.class.getName());

	private static Position createPosition(final List<String> fields) {
		if (fields.size() < 2) {
			InvestmentsFileReader.logger
			        .warn("not enough detsails: " + fields);
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
		final Optional<StockV1> stock = Optional.of(new StockV1(instrument));
		return new Position(fields.get(portfolioIdx), instrument,
		        new BigDecimal(fields.get(amountIndex)), stock, symbol);
	}

	public static List<Position> getPositionsFromCSVFile(final String filePath)
	        throws IOException {
		return ResourceTools.readCsvRecords(filePath).stream().skip(1)
		        .map(InvestmentsFileReader::createPosition)
		        .collect(Collectors.toList());
	}

}
