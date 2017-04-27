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

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeriesCollection;

import com.leonarduk.finance.utils.TimeseriesUtils;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollinger.BollingerBandsLowerIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollinger.BollingerBandsMiddleIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollinger.BollingerBandsUpperIndicator;
import yahoofinance.Stock;

/**
 * This class builds a graphical chart showing values from indicators.
 */
public class BollingerBars {

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
			chartTimeSeries.add(new Day(tick.getEndTime().toDate()), indicator.getValue(i).toDouble());
		}
		return chartTimeSeries;
	}

	public static void displayBollingerBars(final Stock stock) throws IOException {
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock);

		/**
		 * Creating indicators
		 */
		// Close price
		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Bollinger bands
		final BollingerBandsMiddleIndicator middleBBand = new BollingerBandsMiddleIndicator(closePrice);
		final BollingerBandsLowerIndicator lowBBand = new BollingerBandsLowerIndicator(middleBBand, closePrice,
				Decimal.ONE);
		final BollingerBandsUpperIndicator upBBand = new BollingerBandsUpperIndicator(middleBBand, closePrice,
				Decimal.ONE);

		/**
		 * Building chart dataset
		 */
		final TimeSeriesCollection dataset = new TimeSeriesCollection();
		dataset.addSeries(buildChartTimeSeries(series, closePrice,
				stock.getName() + ". (" + stock.getSymbol() + ") - " + stock.getStockExchange()));
		dataset.addSeries(buildChartTimeSeries(series, lowBBand, "Low Bollinger Band"));
		dataset.addSeries(buildChartTimeSeries(series, upBBand, "High Bollinger Band"));

		/**
		 * Creating the chart
		 */
		final JFreeChart chart = ChartFactory.createTimeSeriesChart(stock.getName() + "Close Prices", // title
				"Date", // x-axis label
				"Price Per Unit", // y-axis label
				dataset, // data
				true, // create legend?
				true, // generate tooltips?
				false // generate URLs?
		);
		final XYPlot plot = (XYPlot) chart.getPlot();
		final DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

		/**
		 * Displaying the chart
		 */
		ChartDisplay.displayChartInFrame(chart, 500, 270, "Bollinger chart");
	}

}
