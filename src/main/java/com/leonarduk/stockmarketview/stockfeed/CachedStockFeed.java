package com.leonarduk.stockmarketview.stockfeed;

import static com.leonarduk.stockmarketview.stockfeed.file.IndicatorsToCsv.addValue;
import static com.leonarduk.stockmarketview.stockfeed.file.IndicatorsToCsv.writeFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.leonarduk.stockmarketview.stockfeed.file.CsvStockFeed;

import yahoofinance.Stock;
import yahoofinance.histquotes.HistoricalQuote;

public class CachedStockFeed extends CsvStockFeed {
	public static final Logger log = Logger.getLogger(CachedStockFeed.class.getName());

	private String storeLocation;

	public CachedStockFeed(String storeLocation) {
		this.storeLocation = storeLocation;
	}

	@Override
	protected BufferedReader openReader() throws IOException {
		File file = new File(this.storeLocation, getQueryName(getExchange(), getSymbol()));
		log.info("Read file from " + file.getAbsolutePath());

		if (!file.exists()) {
			throw new IOException(file.getAbsolutePath() + " not found");
		}

		FileReader in = new FileReader(file);
		BufferedReader br = new BufferedReader(in);

		// Skip first line that contains column names
		br.readLine();
		return br;
	}


	public void storeSeries(Stock stock) throws IOException {
		File seriesFile = getFile(stock);
		if (seriesFile.exists()) {
			mergeSeries(stock);
		} else {
			createSeries(stock);
		}
	}

	private void createSeries(Stock stock) throws IOException {

		File file = getFile(stock);
		log.info("Save stock to " + file.getAbsolutePath());

		/**
		 * Building header
		 */
		StringBuilder sb = new StringBuilder("date,open,high,low,close,volume\n");
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		List<HistoricalQuote> series = stock.getHistory();
		for (HistoricalQuote historicalQuote : series) {
			sb.append(format1.format(historicalQuote.getDate().getTime()));
			addValue(sb, historicalQuote.getOpen());
			addValue(sb, historicalQuote.getHigh());
			addValue(sb, historicalQuote.getLow());
			addValue(sb, historicalQuote.getClose());
			sb.append(",").append(historicalQuote.getVolume()).append("\n");
		}
		writeFile(file.getAbsolutePath(), sb);

	}

	private List<HistoricalQuote> loadSeries(Stock stock) throws IOException {
		Optional<Stock> optional = get(stock, Integer.MAX_VALUE);
		if (optional.isPresent()) {
			return optional.get().getHistory();
		}
		return Lists.newArrayList();
	}

	private void mergeSeries(Stock stock) throws IOException {
		List<HistoricalQuote> original = loadSeries(stock);
		mergeSeries(stock, original);
		createSeries(stock);
	}

	protected File getFile(String stockExchange, String symbol) {
		return new File(this.storeLocation, getQueryName(EXCHANGE.valueOf(stockExchange), symbol));
	}

	protected File getFile(Stock stock) throws IOException {
		File folder = new File(this.storeLocation);
		if (!folder.exists()) {
			if (!folder.mkdir()) {
				throw new IOException("Failed to create " + this.storeLocation);
			}
		}

		return getFile(stock.getStockExchange(), stock.getSymbol());
	}

	@Override
	protected String getQueryName(EXCHANGE exchange, String ticker) {
		return exchange.name() + "_" + ticker + ".csv";
	}

}
