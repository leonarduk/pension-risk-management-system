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
package com.leonarduk.finance.stockfeed.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.indicators.helpers.AverageTrueRangeIndicator;
import eu.verdelhan.ta4j.indicators.oscillators.PPOIndicator;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.simple.PriceVariationIndicator;
import eu.verdelhan.ta4j.indicators.simple.TypicalPriceIndicator;
import eu.verdelhan.ta4j.indicators.statistics.StandardDeviationIndicator;
import eu.verdelhan.ta4j.indicators.trackers.EMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.ROCIndicator;
import eu.verdelhan.ta4j.indicators.trackers.RSIIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.WilliamsRIndicator;

/**
 * This class builds a CSV file containing values from indicators.
 */
public class IndicatorsToCsv {

	private static final Logger LOGGER = Logger.getLogger(IndicatorsToCsv.class.getName());
	private static final int ONE_YEAR = 251;
	private static final int TEN_YEAR = 10 * ONE_YEAR;
	private static final int FIVE_YEAR = 5 * ONE_YEAR;
	private static final int THREE_YEAR = 3 * ONE_YEAR;
	private static final int HALF_YEAR = ONE_YEAR / 2;

	public static void exportIndicatorsToCsv(TimeSeries series) {
		String fileName = "target/" + series.getName() + "_indicators.csv";
		/**
		 * Creating indicators
		 */
		// Close price
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Typical price
		TypicalPriceIndicator typicalPrice = new TypicalPriceIndicator(series);
		// Price variation
		PriceVariationIndicator priceVariation = new PriceVariationIndicator(series);
		// Simple moving averages
		SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
		SMAIndicator longSma = new SMAIndicator(closePrice, 20);
		// Exponential moving averages
		EMAIndicator shortEma = new EMAIndicator(closePrice, 8);
		EMAIndicator longEma = new EMAIndicator(closePrice, 20);
		// Percentage price oscillator
		PPOIndicator ppo = new PPOIndicator(closePrice, 12, 26);
		// Rate of change
		ROCIndicator roc = new ROCIndicator(closePrice, 100);
		// Relative strength index
		RSIIndicator rsi = new RSIIndicator(closePrice, 14);
		// Williams %R
		WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 20);
		// Average true range
		AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 20);
		// Standard deviation
		StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);

		/**
		 * Building header
		 */
		StringBuilder sb = new StringBuilder(
				"timestamp,close,typical,variation,sma8,sma20,ema8,ema20,ppo,roc,rsi,williamsr,atr,sd,1D,1W,1M,3M,6M1YR,3YR,5YR,10YR\n");

		/**
		 * Adding indicators values
		 */
		final int nbTicks = series.getTickCount();
		for (int i = 0; i < nbTicks; i++) {
			sb.append(series.getTick(i).getEndTime().toLocalDate()); //
			addValue(sb, (closePrice.getValue(i)));
			addValue(sb, (typicalPrice.getValue(i)));
			addValue(sb, (priceVariation.getValue(i)));
			addValue(sb, (shortSma.getValue(i)));
			addValue(sb, (longSma.getValue(i)));
			addValue(sb, (shortEma.getValue(i)));
			addValue(sb, (longEma.getValue(i)));
			addValue(sb, (ppo.getValue(i)));
			addValue(sb, (roc.getValue(i)));
			addValue(sb, (rsi.getValue(i)));
			addValue(sb, (williamsR.getValue(i)));
			addValue(sb, (atr.getValue(i)));
			addValue(sb, sd.getValue(i)); //
			addValue(sb, calculateReturn(series, 1, i));
			addValue(sb, calculateReturn(series, 5, i));
			addValue(sb, calculateReturn(series, 21, i));
			addValue(sb, calculateReturn(series, 63, i));
			addValue(sb, calculateReturn(series, HALF_YEAR, i));

			addValue(sb, calculateReturn(series, ONE_YEAR, i));
			addValue(sb, calculateReturn(series, THREE_YEAR, i));
			addValue(sb, calculateReturn(series, FIVE_YEAR, i));
			addValue(sb, calculateReturn(series, TEN_YEAR, i));
			sb.append('\n');
		}

		writeFile(fileName, sb);
	}

	public static void writeFile(String fileName, StringBuilder sb) {
		/**
		 * Writing CSV file
		 */
		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(sb.toString());
			LOGGER.info("Saved to " + fileName);
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Unable to write CSV file", ioe);
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException ioe) {
			}
		}
	}

	static NumberFormat formatter = new DecimalFormat("#0.00");

	public static void addValue(StringBuilder buf, Number value) {
		if (value == null) {
			value = 0;
		}
		String format = formatter.format(value);
		addValue(buf, format);
	}

	public static void addValue(StringBuilder buf, String value) {
		buf.append(',').append(value);
	}

	private static void addValue(StringBuilder buf, Decimal value) {
		addValue(buf, (value.toDouble()));
	}

	private static Decimal calculateReturn(TimeSeries series, int timePeriod, int ticker) {
		int index = ticker - timePeriod;
		if (index < 0 || index > series.getEnd()) {
			return Decimal.NaN;
		}
		Decimal initialValue = series.getTick(index).getClosePrice();
		Decimal diff = series.getTick(ticker).getClosePrice().minus(initialValue);
		return diff.dividedBy(initialValue);
	}

}
