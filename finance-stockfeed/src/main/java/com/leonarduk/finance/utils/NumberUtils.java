package com.leonarduk.finance.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.verdelhan.ta4j.Decimal;

public class NumberUtils {

	public static final BigDecimal HUNDRED = getBigDecimal("100");
	public static final BigDecimal THOUSAND = getBigDecimal("1000");
	public static final BigDecimal MILLION = getBigDecimal("1000000");
	public static final BigDecimal BILLION = getBigDecimal("1000000000");

	public static final Logger logger = Logger.getLogger(NumberUtils.class.getName());

	private static DecimalFormat format;

	public static boolean areSame(final BigDecimal thisOne, final BigDecimal thatOne) {
		if (((thisOne == null) && (thatOne != null)) || ((thatOne == null) && (thisOne != null))) {
			return false;
		}

		return roundDecimal(thisOne).equals(roundDecimal(thatOne));
	}

	private static BigDecimal calculateBigDecimal(String data) {
		if (!StringUtils.isParseable(data)) {
			return null;
		}
		try {
			data = NumberUtils.cleanNumberString(data);
			final char lastChar = data.charAt(data.length() - 1);
			BigDecimal multiplier = BigDecimal.ONE;
			switch (lastChar) {
			case 'B':
				data = data.substring(0, data.length() - 1);
				multiplier = BILLION;
				break;
			case 'M':
				data = data.substring(0, data.length() - 1);
				multiplier = MILLION;
				break;
			case 'K':
				data = data.substring(0, data.length() - 1);
				multiplier = THOUSAND;
				break;
			}
			return new BigDecimal(data).multiply(multiplier);
		} catch (final NumberFormatException e) {
			logger.log(Level.WARNING, "Failed to parse: " + data);
			logger.log(Level.FINEST, "Failed to parse: " + data, e);
		}
		return null;
	}

	private static String cleanNumberString(final String data) {
		return StringUtils.join(data.trim().split(","), "");
	}

	public static BigDecimal getBigDecimal(final String data) {
		return calculateBigDecimal(data);
	}

	public static BigDecimal getBigDecimal(final String dataMain, final String dataSub) {
		final BigDecimal main = getBigDecimal(dataMain);
		final BigDecimal sub = getBigDecimal(dataSub);
		if ((main == null) || (main.compareTo(BigDecimal.ZERO) == 0)) {
			return sub;
		}
		return main;
	}

	public static double getDouble(String data) {
		double result = Double.NaN;
		if (!StringUtils.isParseable(data)) {
			return result;
		}
		try {
			data = NumberUtils.cleanNumberString(data);
			final char lastChar = data.charAt(data.length() - 1);
			int multiplier = 1;
			switch (lastChar) {
			case 'B':
				data = data.substring(0, data.length() - 1);
				multiplier = 1000000000;
				break;
			case 'M':
				data = data.substring(0, data.length() - 1);
				multiplier = 1000000;
				break;
			case 'K':
				data = data.substring(0, data.length() - 1);
				multiplier = 1000;
				break;
			}
			result = Double.parseDouble(data) * multiplier;
		} catch (final NumberFormatException e) {
			logger.log(Level.WARNING, "Failed to parse: " + data);
			logger.log(Level.FINEST, "Failed to parse: " + data, e);
		}
		return result;
	}

	public static Integer getInt(String data) {
		Integer result = null;
		if (!StringUtils.isParseable(data)) {
			return result;
		}
		try {
			data = NumberUtils.cleanNumberString(data);
			result = Integer.parseInt(data);
		} catch (final NumberFormatException e) {
			logger.log(Level.WARNING, "Failed to parse: " + data);
			logger.log(Level.FINEST, "Failed to parse: " + data, e);
		}
		return result;
	}

	public static Long getLong(String data) {
		Long result = null;
		if (!StringUtils.isParseable(data)) {
			return result;
		}
		try {
			data = NumberUtils.cleanNumberString(data);
			result = Long.parseLong(data);
		} catch (final NumberFormatException e) {
			logger.log(Level.WARNING, "Failed to parse: " + data);
			logger.log(Level.FINEST, "Failed to parse: " + data, e);
		}
		return result;
	}

	public static BigDecimal getPercent(final BigDecimal numerator, final BigDecimal denominator) {
		if ((denominator == null) || (numerator == null) || (denominator.compareTo(BigDecimal.ZERO) == 0)) {
			return BigDecimal.ZERO;
		}
		return numerator.divide(denominator, 4, BigDecimal.ROUND_HALF_EVEN).multiply(HUNDRED).setScale(2,
				BigDecimal.ROUND_HALF_EVEN);
	}

	public static double getPercent(final double numerator, final double denominator) {
		if (denominator == 0) {
			return 0;
		}
		return (numerator / denominator) * 100;
	}

	public static BigDecimal roundDecimal(final BigDecimal value) {
		return value.setScale(2, RoundingMode.HALF_EVEN);
	}

	public static Decimal roundDecimal(final Decimal decimal) {
		if (Decimal.NaN.equals(decimal)) {
			return decimal;
		}
		format = new DecimalFormat("#.##");
		return Decimal.valueOf(format.format(decimal.toDouble()));
	}

}
