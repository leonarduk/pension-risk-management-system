package com.leonarduk.finance.stockfeed;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.leonarduk.finance.stockfeed.file.CsvStockFeed;
import com.leonarduk.finance.utils.FileUtils;
import com.leonarduk.finance.utils.TimeseriesUtils;

import jersey.repackaged.com.google.common.collect.Lists;
import yahoofinance.histquotes.HistoricalQuote;

public class CachedStockFeed extends CsvStockFeed {
	private final String		storeLocation;

	public static final Logger	log	= Logger
	        .getLogger(CachedStockFeed.class.getName());

	public CachedStockFeed(final String storeLocation) {
		this.storeLocation = storeLocation;
	}

	private void createSeries(final Stock stock) throws IOException {

		final File file = this.getFile(stock);
		CachedStockFeed.log.info("Save stock to " + file.getAbsolutePath());
		final List<HistoricalQuote> series = stock.getHistory();

		/**
		 * Building header
		 */
		final StringBuilder sb = TimeseriesUtils.seriesToCsv(series);
		FileUtils.writeFile(file.getAbsolutePath(), sb);

	}

	protected File getFile(final Instrument instrument) {
		return new File(this.storeLocation, this.getQueryName(instrument));
	}

	protected File getFile(final Stock stock) throws IOException {
		final File folder = new File(this.storeLocation);
		if (!folder.exists() && !folder.mkdir()) {
			throw new IOException("Failed to create " + this.storeLocation);
		}

		return this.getFile(stock.getInstrument());
	}

	@Override
	protected String getQueryName(final Instrument instrument) {
		return instrument.getExchange().name() + "_" + instrument.code()
		        + ".csv";
	}

	@Override
	public Source getSource() {
		return Source.MANUAL;
	}

	@Override
	public boolean isAvailable() {
		final File store = new File(this.storeLocation);
		return (store.exists() & store.isDirectory()) && store.canWrite()
		        && store.canRead();
	}

	private List<HistoricalQuote> loadSeries(final Stock stock)
	        throws IOException {
		final Optional<Stock> optional = this.get(stock.getInstrument(), 1000);
		if (optional.isPresent()) {
			return optional.get().getHistory();
		}
		return Lists.newArrayList();
	}

	private void mergeSeries(final Stock stock) throws IOException {
		final List<HistoricalQuote> original = this.loadSeries(stock);
		this.mergeSeries(stock, original);
		this.createSeries(stock);
	}

	@Override
	protected BufferedReader openReader() throws IOException {
		final File file = new File(this.storeLocation,
		        this.getQueryName(this.getInstrument()));
		CachedStockFeed.log.info("Read file from " + file.getAbsolutePath());

		if (!file.exists()) {
			throw new IOException(file.getAbsolutePath() + " not found");
		}

		final FileReader in = new FileReader(file);
		final BufferedReader br = new BufferedReader(in);

		// Skip first line that contains column names
		br.readLine();
		return br;
	}

	public void storeSeries(final Stock stock) throws IOException {
		final File seriesFile = this.getFile(stock);
		if (seriesFile.exists()) {
			this.mergeSeries(stock);
		}
		else {
			this.createSeries(stock);
		}
	}

}
