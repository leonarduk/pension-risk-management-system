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
package com.leonarduk.finance.stockfeed.file;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
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

	private static final int	FIVE_YEAR	= 5 * IndicatorsToCsv.ONE_YEAR;
	private static NumberFormat	formatter	= new DecimalFormat("#0.00");
	private static final int	HALF_YEAR	= IndicatorsToCsv.ONE_YEAR / 2;
	private static final Logger	LOGGER		= Logger.getLogger(IndicatorsToCsv.class.getName());
	private static final int	ONE_YEAR	= 251;
	private static final int	TEN_YEAR	= 10 * IndicatorsToCsv.ONE_YEAR;

	private static final int	THREE_YEAR	= 3 * IndicatorsToCsv.ONE_YEAR;

	public static void addValue(final StringBuilder buf, final BigDecimal value) {
		final String format = IndicatorsToCsv.formatter
		        .format(value == null ? BigDecimal.ZERO : value);
		IndicatorsToCsv.addValue(buf, format);
	}

	private static void addValue(final StringBuilder buf, final Decimal value) {
		IndicatorsToCsv.addValue(buf, (BigDecimal.valueOf(value.toDouble())));
	}

	public static void addValue(final StringBuilder buf, final long value) {
		buf.append(',').append(value);
	}

	public static void addValue(final StringBuilder buf, final String value) {
		buf.append(',').append(value);
	}

	private static Decimal calculateReturn(final TimeSeries series, final int timePeriod,
	        final int ticker) {
		final int index = ticker - timePeriod;
		if ((index < 0) || (index > series.getEnd())) {
			return Decimal.NaN;
		}
		final Decimal initialValue = series.getTick(index).getClosePrice();
		final Decimal diff = series.getTick(ticker).getClosePrice().minus(initialValue);
		return diff.dividedBy(initialValue);
	}

	public static void exportIndicatorsToCsv(final TimeSeries series) {
		final String fileName = "target/" + series.getName() + "_indicators.csv";
		/**
		 * Creating indicators
		 */
		// Close price
		final ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		// Typical price
		final TypicalPriceIndicator typicalPrice = new TypicalPriceIndicator(series);
		// Price variation
		final PriceVariationIndicator priceVariation = new PriceVariationIndicator(series);
		// Simple moving averages
		final SMAIndicator shortSma = new SMAIndicator(closePrice, 8);
		final SMAIndicator longSma = new SMAIndicator(closePrice, 20);
		// Exponential moving averages
		final EMAIndicator shortEma = new EMAIndicator(closePrice, 8);
		final EMAIndicator longEma = new EMAIndicator(closePrice, 20);
		// Percentage price oscillator
		final PPOIndicator ppo = new PPOIndicator(closePrice, 12, 26);
		// Rate of change
		final ROCIndicator roc = new ROCIndicator(closePrice, 100);
		// Relative strength index
		final RSIIndicator rsi = new RSIIndicator(closePrice, 14);
		// Williams %R
		final WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 20);
		// Average true range
		final AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 20);
		// Standard deviation
		final StandardDeviationIndicator sd = new StandardDeviationIndicator(closePrice, 14);

		/**
		 * Building header
		 */
		final StringBuilder sb = new StringBuilder(
		        "timestamp,close,typical,variation,sma8,sma20,ema8,ema20,ppo,roc,rsi,williamsr,atr,sd,1D,1W,1M,3M,6M1YR,3YR,5YR,10YR\n");

		/**
		 * Adding indicators values
		 */
		final int nbTicks = series.getTickCount();
		for (int i = 0; i < nbTicks; i++) {
			sb.append(series.getTick(i).getEndTime().toLocalDate()); //
			IndicatorsToCsv.addValue(sb, (closePrice.getValue(i)));
			IndicatorsToCsv.addValue(sb, (typicalPrice.getValue(i)));
			IndicatorsToCsv.addValue(sb, (priceVariation.getValue(i)));
			IndicatorsToCsv.addValue(sb, (shortSma.getValue(i)));
			IndicatorsToCsv.addValue(sb, (longSma.getValue(i)));
			IndicatorsToCsv.addValue(sb, (shortEma.getValue(i)));
			IndicatorsToCsv.addValue(sb, (longEma.getValue(i)));
			IndicatorsToCsv.addValue(sb, (ppo.getValue(i)));
			IndicatorsToCsv.addValue(sb, (roc.getValue(i)));
			IndicatorsToCsv.addValue(sb, (rsi.getValue(i)));
			IndicatorsToCsv.addValue(sb, (williamsR.getValue(i)));
			IndicatorsToCsv.addValue(sb, (atr.getValue(i)));
			IndicatorsToCsv.addValue(sb, sd.getValue(i)); //
			IndicatorsToCsv.addValue(sb, IndicatorsToCsv.calculateReturn(series, 1, i));
			IndicatorsToCsv.addValue(sb, IndicatorsToCsv.calculateReturn(series, 5, i));
			IndicatorsToCsv.addValue(sb, IndicatorsToCsv.calculateReturn(series, 21, i));
			IndicatorsToCsv.addValue(sb, IndicatorsToCsv.calculateReturn(series, 63, i));
			IndicatorsToCsv.addValue(sb,
			        IndicatorsToCsv.calculateReturn(series, IndicatorsToCsv.HALF_YEAR, i));

			IndicatorsToCsv.addValue(sb,
			        IndicatorsToCsv.calculateReturn(series, IndicatorsToCsv.ONE_YEAR, i));
			IndicatorsToCsv.addValue(sb,
			        IndicatorsToCsv.calculateReturn(series, IndicatorsToCsv.THREE_YEAR, i));
			IndicatorsToCsv.addValue(sb,
			        IndicatorsToCsv.calculateReturn(series, IndicatorsToCsv.FIVE_YEAR, i));
			IndicatorsToCsv.addValue(sb,
			        IndicatorsToCsv.calculateReturn(series, IndicatorsToCsv.TEN_YEAR, i));
			sb.append('\n');
		}

		IndicatorsToCsv.writeFile(fileName, sb);
	}

	public static void writeFile(final String fileName, final StringBuilder sb) {
		/**
		 * Writing CSV file
		 */
		BufferedWriter writer = null;
		try {

			writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(sb.toString());
			IndicatorsToCsv.LOGGER.info("Saved to " + fileName);
		}
		catch (final IOException ioe) {
			IndicatorsToCsv.LOGGER.log(Level.SEVERE, "Unable to write CSV file", ioe);
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}
			catch (final IOException ioe) {
			}
		}
	}

}
