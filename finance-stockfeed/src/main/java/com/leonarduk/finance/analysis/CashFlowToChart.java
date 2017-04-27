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
package com.leonarduk.finance.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.CashFlow;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import yahoofinance.Stock;

/**
 * This class builds a graphical chart showing the cash flow of a strategy.
 */
public class CashFlowToChart {

	/**
	 * Adds the cash flow axis to the plot.
	 * 
	 * @param plot
	 *            the plot
	 * @param dataset
	 *            the cash flow dataset
	 */
	private static void addCashFlowAxis(final XYPlot plot, final TimeSeriesCollection dataset) {
		final NumberAxis cashAxis = new NumberAxis("Cash Flow Ratio");
		cashAxis.setAutoRangeIncludesZero(false);
		plot.setRangeAxis(1, cashAxis);
		plot.setDataset(1, dataset);
		plot.mapDatasetToRangeAxis(1, 1);
		final StandardXYItemRenderer cashFlowRenderer = new StandardXYItemRenderer();
		cashFlowRenderer.setSeriesPaint(0, Color.blue);
		plot.setRenderer(1, cashFlowRenderer);
	}

	/**
	 * Builds a JFreeChart time series from a Ta4j time series and an indicator.
	 * 
	 * @param tickSeries
	 *            the ta4j time series
	 * @param indicator
	 *            the indicator
	 * @param name
	 *            the name of the chart time series
	 * @return the JFreeChart time series
	 */
	private static org.jfree.data.time.TimeSeries buildChartTimeSeries(final TimeSeries tickSeries,
			final Indicator<Decimal> indicator, final String name) {
		final org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(name);
		for (int i = 0; i < tickSeries.getTickCount(); i++) {
			final Tick tick = tickSeries.getTick(i);
			chartTimeSeries.add(new Minute(tick.getEndTime().toDate()), indicator.getValue(i).toDouble());
		}
		return chartTimeSeries;
	}

	/**
	 * Displays a chart in a frame.
	 * 
	 * @param chart
	 *            the chart to be displayed
	 */
	private static void displayChart(final JFreeChart chart) {
		// Chart panel
		final ChartPanel panel = new ChartPanel(chart);
		panel.setFillZoomRectangle(true);
		panel.setMouseWheelEnabled(true);
		panel.setPreferredSize(new Dimension(1024, 400));
		// Application frame
		final ApplicationFrame frame = new ApplicationFrame("Ta4j example - Cash flow to chart");
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	public static void main(final String[] args) throws IOException {

		// Getting the time series
		final StockFeed feed = new IntelligentStockFeed();
		final String ticker = "IUKD";
		final Stock stock = feed.get(Exchange.London, ticker, 2).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock);

		// Building the trading strategy
		final AbstractStrategy strategy = MovingMomentumStrategy.buildStrategy(series, 12, 26, 9);

		// Running the strategy
		final TradingRecord tradingRecord = series.run(strategy.getStrategy());
		// Getting the cash flow of the resulting trades
		final CashFlow cashFlow = new CashFlow(series, tradingRecord);

		/**
		 * Building chart datasets
		 */
		final TimeSeriesCollection datasetAxis1 = new TimeSeriesCollection();
		datasetAxis1.addSeries(buildChartTimeSeries(series, new ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"));
		final TimeSeriesCollection datasetAxis2 = new TimeSeriesCollection();
		datasetAxis2.addSeries(buildChartTimeSeries(series, cashFlow, "Cash Flow"));

		/**
		 * Creating the chart
		 */
		final JFreeChart chart = ChartFactory.createTimeSeriesChart("Bitstamp BTC", // title
				"Date", // x-axis label
				"Price", // y-axis label
				datasetAxis1, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
		final XYPlot plot = (XYPlot) chart.getPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

		/**
		 * Adding the cash flow axis (on the right)
		 */
		addCashFlowAxis(plot, datasetAxis2);

		/**
		 * Displaying the chart
		 */
		displayChart(chart);
	}
}
