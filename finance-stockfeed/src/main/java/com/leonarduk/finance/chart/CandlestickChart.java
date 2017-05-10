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
package com.leonarduk.finance.chart;

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

import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;

/**
 * This class builds a traditional candlestick chart.
 */
public class CandlestickChart {

	/**
	 * Builds an additional JFreeChart dataset from a ta4j time series.
	 *
	 * @param series
	 *            a time series
	 * @return an additional dataset
	 */
	private static TimeSeriesCollection createAdditionalDataset(final TimeSeries series) {
		final ClosePriceIndicator indicator = new ClosePriceIndicator(series);
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		final org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(
				series.getName() + " price");
		for (int i = 0; i < series.getTickCount(); i++) {
			final Tick tick = series.getTick(i);
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
	private static OHLCDataset createOHLCDataset(final TimeSeries series) {
		final int nbTicks = series.getTickCount();

		final Date[] dates = new Date[nbTicks];
		final double[] opens = new double[nbTicks];
		final double[] highs = new double[nbTicks];
		final double[] lows = new double[nbTicks];
		final double[] closes = new double[nbTicks];
		final double[] volumes = new double[nbTicks];

		for (int i = 0; i < nbTicks; i++) {
			final Tick tick = series.getTick(i);
			dates[i] = tick.getEndTime().toDate();
			opens[i] = tick.getOpenPrice().toDouble();
			highs[i] = tick.getMaxPrice().toDouble();
			lows[i] = tick.getMinPrice().toDouble();
			closes[i] = tick.getClosePrice().toDouble();
			volumes[i] = tick.getVolume().toDouble();
		}

		final OHLCDataset dataset = new DefaultHighLowDataset(series.getName(), dates, highs, lows, opens, closes,
				volumes);

		return dataset;
	}

	public static void displayCandlestickChart(final Stock stock) throws IOException {
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);

		/**
		 * Creating the OHLC dataset
		 */
		final OHLCDataset ohlcDataset = createOHLCDataset(series);

		/**
		 * Creating the additional dataset
		 */
		final TimeSeriesCollection xyDataset = createAdditionalDataset(series);

		/**
		 * Creating the chart
		 */
		final JFreeChart chart = ChartFactory.createCandlestickChart(stock.getName() + " price", "Time",
				stock.getCurrency(), ohlcDataset, true);
		// Candlestick rendering
		final CandlestickRenderer renderer = new CandlestickRenderer();
		renderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_SMALLEST);
		final XYPlot plot = chart.getXYPlot();
		plot.setRenderer(renderer);
		// Additional dataset
		final int index = 1;
		plot.setDataset(index, xyDataset);
		plot.mapDatasetToRangeAxis(index, 0);
		final XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);
		renderer2.setSeriesPaint(index, Color.blue);
		plot.setRenderer(index, renderer2);
		// Misc
		plot.setRangeGridlinePaint(Color.lightGray);
		plot.setBackgroundPaint(Color.white);
		final NumberAxis numberAxis = (NumberAxis) plot.getRangeAxis();
		numberAxis.setAutoRangeIncludesZero(false);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		/**
		 * Displaying the chart
		 */
		ChartDisplay.displayChartInFrame(chart, 740, 300, "Candlestick Chart");
	}

}
