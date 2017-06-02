package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.Optional;

import org.joda.time.LocalDate;

public interface StockFeed {
	public enum Exchange {
		London
	}

	Optional<Stock> get(Instrument fromString, int i) throws IOException;

	Optional<Stock> get(Instrument instrument, int years, boolean interpolate)
	        throws IOException;

	Optional<Stock> get(Instrument instrument, LocalDate fromDate,
	        LocalDate toDate) throws IOException;

	Optional<Stock> get(Instrument instrument, LocalDate fromLocalDate,
	        LocalDate toLocalDate, boolean interpolate) throws IOException;

	Object getSource();

	boolean isAvailable();

}
