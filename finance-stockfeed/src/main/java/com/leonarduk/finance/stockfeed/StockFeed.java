package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.util.Optional;

import java.time.LocalDate;

public interface StockFeed {
	public enum Exchange {
		London(".L");

		private String yahooSuffix;

		public String getYahooSuffix() {
			return yahooSuffix;
		}

		Exchange(String yahooSuffix) {
			this.yahooSuffix = yahooSuffix;
		}
	}

	Optional<StockV1> get(Instrument fromString, int i) throws IOException;

	Optional<StockV1> get(Instrument instrument, int years, boolean interpolate) throws IOException;

	Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate) throws IOException;

	Optional<StockV1> get(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate, boolean interpolate)
			throws IOException;

	Object getSource();

	boolean isAvailable();

}
