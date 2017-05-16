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
package com.leonarduk.finance.strategies;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.IntelligentStockFeed;
import com.leonarduk.finance.stockfeed.Stock;
import com.leonarduk.finance.stockfeed.StockFeed;
import com.leonarduk.finance.utils.TimeseriesUtils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;

/**
 * Strategy execution logging example.
 * <p>
 */
public class StrategyExecutionLogging {

	private static final URL LOGBACK_CONF_FILE = StrategyExecutionLogging.class.getClassLoader()
	        .getResource("logback-traces.xml");

	/**
	 * Loads the Logback configuration from a resource file. Only here to avoid polluting other
	 * examples with logs. Could be replaced by a simple logback.xml file in the resource folder.
	 */
	private static void loadLoggerConfiguration() {
		final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		context.reset();

		final JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		try {
			configurator.doConfigure(StrategyExecutionLogging.LOGBACK_CONF_FILE);
		}
		catch (final JoranException je) {
			Logger.getLogger(StrategyExecutionLogging.class.getName()).log(Level.SEVERE,
			        "Unable to load Logback configuration", je);
		}
	}

	public static void main(final String[] args) throws IOException {
		// Loading the Logback configuration
		StrategyExecutionLogging.loadLoggerConfiguration();

		// Getting the time series
		final StockFeed feed = new IntelligentStockFeed();
		final String ticker = "PHGP";
		final Stock stock = feed.get(Instrument.fromString(ticker), 20).get();
		final TimeSeries series = TimeseriesUtils.getTimeSeries(stock, 1);

		// Building the trading strategy
		final Strategy strategy = CCICorrectionStrategy.buildStrategy(series);

		// Running the strategy
		series.run(strategy);
	}
}
