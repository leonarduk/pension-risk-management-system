package com.leonarduk.stockmarketview;

import java.io.IOException;

import com.leonarduk.stockmarketview.chart.BollingerBars;
import com.leonarduk.stockmarketview.chart.CandlestickChart;
import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.IndicatorsToCsv;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.stockfeed.google.GoogleFeed;

import eu.verdelhan.ta4j.TimeSeries;
import yahoofinance.Stock;

public class Demo {
	public static void main(String[] args) throws IOException {
		StockFeed feed = new GoogleFeed();
		String ticker = "PHGP";
		Stock stock = feed.get(EXCHANGE.London, ticker);
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);

		System.out.println("Series: " + series.getName() + " (" + series.getSeriesPeriodDescription() + ")");
		System.out.println("Number of ticks: " + series.getTickCount());
		System.out.println("First tick: \n" + "\tVolume: " + series.getTick(0).getVolume() + "\n" + "\tOpen price: "
				+ series.getTick(0).getOpenPrice() + "\n" + "\tClose price: " + series.getTick(0).getClosePrice());

		CandlestickChart.displayCandlestickChart(stock);
		BollingerBars.displayBollingerBars(stock);
		IndicatorsToCsv.exportToCsv(series);
	}
}
