package com.leonarduk.finance;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.leonarduk.finance.stockfeed.DailyTimeseries;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import com.leonarduk.finance.stockfeed.file.InvestmentsFileReader;
import com.leonarduk.finance.stockfeed.google.GoogleFeed;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

import eu.verdelhan.ta4j.TimeSeries;
import yahoofinance.Stock;

public class Demo {
	public static void main(String[] args) throws IOException {

		String filePath = new File(Demo.class.getClassLoader().getResource("Book1.csv").getFile()).getAbsolutePath();
		StockFeed yahoo = new YahooFeed();
		StockFeed google = new GoogleFeed();
		Map<String, String> sourceMap = new ConcurrentHashMap<>();

		/*
		 * {MINV=failed, ALAT=failed, CU2=failed, XFVT=failed, CW8=failed,
		 * SAAA=failed, ANX=failed, GBDV=failed, XSTR=failed, CC1=failed,
		 * AEEM=failed, UB39=failed, CKR1=failed, ASDX=failed, GHYS=failed,
		 * MVUS=failed, IBGY=failed, EMV=failed, VGOV=failed, IMV=failed,
		 * CRU1=failed, XMEA=failed, BRDX=failed, SCHP=failed, GOLB=failed}
		 * 
		 */
		InvestmentsFileReader.getStocksFromCSVFile(filePath).parallelStream().forEach(stock -> {
			try {

				if (showForOneSeries(yahoo, Exchange.valueOf(stock.getStockExchange()), stock.getSymbol())) {
					// sourceMap.put(stock.getSymbol(), "yahoo");
				} else {
					if (!showForOneSeries(google, Exchange.valueOf(stock.getStockExchange()), stock.getSymbol())) {
						sourceMap.put(stock.getSymbol(), "google");
					} else {
						sourceMap.put(stock.getSymbol(), "failed");
					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		System.out.println(sourceMap);
	}

	public static boolean showForOneSeries(StockFeed feed, Exchange exchange, String ticker) {
		try {
			Optional<Stock> stock = feed.get(exchange, ticker,1);

			if (stock.isPresent()) {
				TimeSeries series;

				series = DailyTimeseries.getTimeSeries(stock.get());

				// System.out.println("Series: " + series.getName() + " (" +
				// System.out.println("Number of ticks: " +
				// series.getTickCount());
				// System.out.println("First tick: \n" + "\tVolume: " +
				// series.getTick(0).getVolume() + "\n" + "\tOpen price: "
				// + series.getTick(0).getOpenPrice() + "\n" + "\tClose price: "
				// +
				// series.getTick(0).getClosePrice());

				// CandlestickChart.displayCandlestickChart(stock.get());
				// BollingerBars.displayBollingerBars(stock.get());
				// IndicatorsToCsv.exportToCsv(series);
				return true;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
