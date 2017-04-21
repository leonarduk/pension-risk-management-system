/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.leonarduk.stockmarketview.chart;

import java.awt.Color;
import java.io.IOException;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;

import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import yahoofinance.Stock;

/**
 * This class builds a traditional candlestick chart.
 */
public class CandlestickChart {

	public static void displayCandlestickChart(Stock stock) throws IOException {
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);

		/**
		 * Creating the OHLC dataset
		 */
		OHLCDataset ohlcDataset = createOHLCDataset(series);

		/**
		 * Creating the additional dataset
		 */
		TimeSeriesCollection xyDataset = createAdditionalDataset(series);

		/**
		 * Creating the chart
		 */
		JFreeChart chart = ChartFactory.createCandlestickChart(stock.getName() + " price", "Time", stock.getCurrency(),
				ohlcDataset, true);
		// Candlestick rendering
		CandlestickRenderer renderer = new CandlestickRenderer();
		renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
		XYPlot plot = chart.getXYPlot();
		plot.setRenderer(renderer);
		// Additional dataset
		int index = 1;
		plot.setDataset(index, xyDataset);
		plot.mapDatasetToRangeAxis(index, 0);
		XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
		renderer2.setSeriesPaint(index, Color.blue);
		plot.setRenderer(index, renderer2);
		// Misc
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setBackgroundPaint(Color.white);
		NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
		numberAxis.setAutoRangeIncludesZero(false);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		/**
		 * Displaying the chart
		 */
		ChartDisplay.displayChartInFrame(chart, 740,300,"Candlestick Chart");
	}

	/**
	 * Builds an additional JFreeChart dataset from a ta4j time series.
	 * 
	 * @param series
	 *            a time series
	 * @return an additional dataset
	 */
	private static TimeSeriesCollection createAdditionalDataset(TimeSeries series) {
		ClosePriceIndicator indicator = new ClosePriceIndicator(series);
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(
				series.getName() + " price");
		for (int i = 0; i < series.getTickCount(); i++) {
			Tick tick = series.getTick(i);
			chartTimeSeries.add(new Second(tick.getEndTime().toDate()), indicator.getValue(i).toDouble());
		}
		dataset.addSeries(chartTimeSeries);
		return dataset;
	}

	/**
	 * Builds a JFreeChart OHLC dataset from a ta4j time series.
	 * 
	 * @param series
	 *            a time series
	 * @return an Open-High-Low-Close dataset
	 */
	private static OHLCDataset createOHLCDataset(TimeSeries series) {
		final int nbTicks = series.getTickCount();

		Date[] dates = new Date[nbTicks];
		double[] opens = new double[nbTicks];
		double[] highs = new double[nbTicks];
		double[] lows = new double[nbTicks];
		double[] closes = new double[nbTicks];
		double[] volumes = new double[nbTicks];

		for (int i = 0; i < nbTicks; i++) {
			Tick tick = series.getTick(i);
			dates[i] = tick.getEndTime().toDate();
			opens[i] = tick.getOpenPrice().toDouble();
			highs[i] = tick.getMaxPrice().toDouble();
			lows[i] = tick.getMinPrice().toDouble();
			closes[i] = tick.getClosePrice().toDouble();
			volumes[i] = tick.getVolume().toDouble();
		}

		OHLCDataset dataset = new DefaultHighLowDataset(series.getName(), dates, highs, lows, opens, closes, volumes);

		return dataset;
	}

}
