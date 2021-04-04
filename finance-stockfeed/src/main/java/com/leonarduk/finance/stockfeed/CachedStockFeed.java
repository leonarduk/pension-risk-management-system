package com.leonarduk.finance.stockfeed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;
import com.leonarduk.finance.stockfeed.file.CsvStockFeed;
import com.leonarduk.finance.utils.FileUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

public class CachedStockFeed extends AbstractStockFeed {

	public static final Logger	log	= LoggerFactory
	        .getLogger(CachedStockFeed.class.getName());
	private final DataStore dataStore;

	public CachedStockFeed(final DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public List<Bar> loadSeries(final StockV1 stock)
	        throws IOException {
		final Optional<StockV1> optional = this.get(stock.getInstrument(), 1000);
		if (optional.isPresent()) {
			return optional.get().getHistory();
		}
		return Lists.newArrayList();
	}

	private void mergeSeries(final StockV1 stock) throws IOException {
		final List<Bar> original = this.loadSeries(stock);
		this.mergeSeries(stock, original);
	}

	public void storeSeries(final StockV1 stock) throws IOException{
		if (this.dataStore.contains(stock)) {
			this.mergeSeries(stock);
		}
		this.dataStore.storeSeries(stock);
	}

	@Override
	public Optional<StockV1> get(Instrument instrument, int years) throws IOException {
		return this.dataStore.get(instrument, years);
	}

	@Override
	public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate) throws IOException {
		return this.dataStore.get(instrument, fromDate,toDate);
	}

	@Override
	public Source getSource() {
		return Source.CACHE;
	}


	@Override
	public boolean isAvailable() {
		return this.dataStore.isAvailable();
	}
}
