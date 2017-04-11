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
package com.leonarduk.stockmarketview.strategies;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

import com.leonarduk.stockmarketview.stockfeed.DailyTimeseries;
import com.leonarduk.stockmarketview.stockfeed.IntelligentStockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed;
import com.leonarduk.stockmarketview.stockfeed.StockFeed.EXCHANGE;
import com.leonarduk.stockmarketview.stockfeed.google.GoogleFeed;

import yahoofinance.Stock;

/**
 * Strategy execution logging example.
 * <p>
 */
public class StrategyExecutionLogging {

    private static final URL LOGBACK_CONF_FILE = StrategyExecutionLogging.class.getClassLoader().getResource("logback-traces.xml");
    
    /**
     * Loads the Logback configuration from a resource file.
     * Only here to avoid polluting other examples with logs. Could be replaced by a simple logback.xml file in the resource folder.
     */
    private static void loadLoggerConfiguration() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        try {
            configurator.doConfigure(LOGBACK_CONF_FILE);
        } catch (JoranException je) {
            Logger.getLogger(StrategyExecutionLogging.class.getName()).log(Level.SEVERE, "Unable to load Logback configuration", je);
        }
    }

    public static void main(String[] args) throws IOException {
        // Loading the Logback configuration
        loadLoggerConfiguration();

        // Getting the time series
		StockFeed feed = new IntelligentStockFeed();
		String ticker = "PHGP";
		Stock stock = feed.get(EXCHANGE.London, ticker,20).get();
		TimeSeries series = DailyTimeseries.getTimeSeries(stock);

        // Building the trading strategy
        Strategy strategy = CCICorrectionStrategy.buildStrategy(series);

        // Running the strategy
        series.run(strategy);
    }
}
