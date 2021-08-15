package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Bar;

import com.google.common.collect.Lists;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.StockV1;

public class CachedStockFeed extends AbstractStockFeed {

	public static final Logger	log	= LoggerFactory
	        .getLogger(CachedStockFeed.class.getName());
	private final DataStore dataStore;

	public CachedStockFeed(final DataStore dataStore) {
		this.dataStore = dataStore;
	}

	public List<Bar> loadSeries(final StockV1 stock)
	        throws IOException {
		boolean addLatestQuoteToTheSeries  =false;
		final Optional<StockV1> optional = this.get(stock.getInstrument(), 1000, false);
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
	public Optional<StockV1> get(Instrument instrument, int years, boolean addLatestQuoteToTheSeries) throws IOException {
		return this.dataStore.get(instrument, years, addLatestQuoteToTheSeries);
	}

	@Override
	public Optional<StockV1> get(Instrument instrument, LocalDate fromDate, LocalDate toDate, boolean addLatestQuoteToTheSeries) throws IOException {
		return this.dataStore.get(instrument, fromDate,toDate, addLatestQuoteToTheSeries);
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
