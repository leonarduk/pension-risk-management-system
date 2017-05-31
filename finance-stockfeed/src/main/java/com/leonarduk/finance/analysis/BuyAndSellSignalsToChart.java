/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.leonarduk.finance.analysis;

import java.awt.Color;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Marker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.strategies.AbstractStrategy;
import com.leonarduk.finance.strategies.GlobalExtremaStrategy;
import com.leonarduk.finance.strategies.MovingMomentumStrategy;
import com.leonarduk.finance.strategies.SimpleMovingAverageStrategy;
import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;

/**
 * This class builds a graphical chart showing the buy/sell signals of a strategy.
 */
public class BuyAndSellSignalsToChart {
	/**
	 * Runs a strategy over a time series and adds the value markers corresponding to buy/sell
	 * signals to the plot.
	 *
	 * @param series
	 *            a time series
	 * @param strategy2
	 *            a trading strategy
	 * @param plot
	 *            the plot
	 */
	private static void addBuySellSignals(final TimeSeries series,
	        final AbstractStrategy strategy2, final XYPlot plot) {
		// Running the strategy
		final List<Trade> trades = series.run(strategy2.getStrategy())
		        .getTrades();
		// Adding markers to plot
		for (final Trade trade : trades) {
			// Buy signal
			final double buySignalTickTime = new Minute(series
			        .getTick(trade.getEntry().getIndex()).getEndTime().toDate())
			                .getFirstMillisecond();
			final Marker buyMarker = new ValueMarker(buySignalTickTime);
			buyMarker.setPaint(Color.GREEN);
			buyMarker.setLabel("B");
			plot.addDomainMarker(buyMarker);
			// Sell signal
			final double sellSignalTickTime = new Minute(series
			        .getTick(trade.getExit().getIndex()).getEndTime().toDate())
			                .getFirstMillisecond();
			final Marker sellMarker = new ValueMarker(sellSignalTickTime);
			sellMarker.setPaint(Color.RED);
			sellMarker.setLabel("S");
			plot.addDomainMarker(sellMarker);
		}
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
	private static org.jfree.data.time.TimeSeries buildChartTimeSeries(
	        final TimeSeries tickSeries, final Indicator<Decimal> indicator,
	        final String name) {
		final org.jfree.data.time.TimeSeries chartTimeSeries = new org.jfree.data.time.TimeSeries(
		        name);
		for (int i = 0; i < tickSeries.getTickCount(); i++) {
			final Tick tick = tickSeries.getTick(i);
			chartTimeSeries.add(new Minute(tick.getEndTime().toDate()),
			        indicator.getValue(i).toDouble());
		}
		return chartTimeSeries;
	}

	public static void displayBuyAndSellChart(final TimeSeries series,
	        final List<AbstractStrategy> strategies, final String name) {
		/**
		 * Building chart datasets
		 */
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(BuyAndSellSignalsToChart.buildChartTimeSeries(series,
		        new ClosePriceIndicator(series), name));

		/**
		 * Creating the chart
		 */
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(name, // title
		        "Date", // x-axis label
		        "Price", // y-axis label
		        dataset, // data
		        true, // create legend?
		        true, // generate tooltips?
		        false // generate URLs?
		);
		final XYPlot plot = (XYPlot) chart.getPlot();
		// final DateAxis axis = (DateAxis) plot.getDomainAxis();
		// axis.setDateFormatOverride(new SimpleDateFormat("MM-dd HH:mm"));

		/**
		 * Running the strategy and adding the buy and sell signals to plot
		 */
		for (final AbstractStrategy strategy2 : strategies) {
			BuyAndSellSignalsToChart.addBuySellSignals(series, strategy2, plot);
		}

		/**
		 * Displaying the chart
		 */
		BuyAndSellSignalsToChart.displayChart(chart);
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
		final ApplicationFrame frame = new ApplicationFrame(
		        "Ta4j example - Buy and sell signals to chart");
		frame.setContentPane(panel);
		frame.pack();
		RefineryUtilities.centerFrameOnScreen(frame);
		frame.setVisible(true);
	}

	public static void main(final String[] args) throws IOException {

		// Getting the time series
		final StockFeed feed = new IntelligentStockFeed();
		final String ticker = "ISXF";
		final Stock stock = feed.get(Instrument.fromString(ticker), 1).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);

		// Building the trading strategy
		final List<AbstractStrategy> strategies = new ArrayList<>();
		strategies.add(GlobalExtremaStrategy.buildStrategy(series));
		strategies.add(MovingMomentumStrategy.buildStrategy(series, 12, 26, 9));

		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 12));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 20));
		strategies.add(SimpleMovingAverageStrategy.buildStrategy(series, 50));

		BuyAndSellSignalsToChart.displayBuyAndSellChart(series, strategies,
		        stock.getName());
	}

}
