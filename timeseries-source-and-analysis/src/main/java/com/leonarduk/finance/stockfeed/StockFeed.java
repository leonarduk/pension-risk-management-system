package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

public interface StockFeed {
	enum Exchange {
		London(".L"), NA("");

		private final String yahooSuffix;

		public String getYahooSuffix() {
			return yahooSuffix;
		}

		Exchange(String yahooSuffix) {
			this.yahooSuffix = yahooSuffix;
		}
	}

	Optional<StockV1> get(Instrument fromString, int i) throws IOException;

	Optional<StockV1> get(Instrument instrument, int years, boolean interpolate, boolean cleanData) throws IOException;

	Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate) throws IOException;

	Optional<StockV1> get(Instrument instrument, LocalDate fromLocalDate, LocalDate toLocalDate, boolean interpolate,
			boolean cleanData) throws IOException;

	Object getSource();

	boolean isAvailable();

}
