/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2016 Marc de Verdelhan & respective authors (see AUTHORS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.leonarduk.finance.stockfeed.file;

import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.PPOIndicator;
import org.ta4j.core.indicators.ROCIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.WilliamsRIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.PriceVariationIndicator;
import org.ta4j.core.indicators.helpers.TypicalPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import com.leonarduk.finance.utils.FileUtils;
import com.leonarduk.finance.utils.StringUtils;

/**
 * This class builds a CSV file containing values from indicators.
 */
public class IndicatorsToCsv {

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
//		final AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 20);
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
		final int nbTicks = series.getBarCount();
		for (int i = 0; i < nbTicks; i++) {
			sb.append(series.getBar(i).getEndTime().toLocalDate()); //
			StringUtils.addValue(sb, (closePrice.getValue(i)));
			StringUtils.addValue(sb, (typicalPrice.getValue(i)));
			StringUtils.addValue(sb, (priceVariation.getValue(i)));
			StringUtils.addValue(sb, (shortSma.getValue(i)));
			StringUtils.addValue(sb, (longSma.getValue(i)));
			StringUtils.addValue(sb, (shortEma.getValue(i)));
			StringUtils.addValue(sb, (longEma.getValue(i)));
			StringUtils.addValue(sb, (ppo.getValue(i)));
			StringUtils.addValue(sb, (roc.getValue(i)));
			StringUtils.addValue(sb, (rsi.getValue(i)));
			StringUtils.addValue(sb, (williamsR.getValue(i)));
//			StringUtils.addValue(sb, (atr.getValue(i)));
			StringUtils.addValue(sb, sd.getValue(i)); //
			sb.append('\n');
		}

		FileUtils.writeFile(fileName, sb);
	}

}
