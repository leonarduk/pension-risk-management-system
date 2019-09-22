package com.leonarduk.finance.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.ta4j.core.num.Num;

public class StringUtils {
	private static NumberFormat formatter = new DecimalFormat("#0.00");

	public static void addValue(final StringBuilder buf, final Num num) {
		final String format = StringUtils.formatter.format(num == null ? BigDecimal.ZERO : num.doubleValue());
		StringUtils.addValue(buf, format);
	}

	public static void addValue(final StringBuilder buf, final BigDecimal value) {
		StringUtils.addValue(buf, value.doubleValue());
	}

	public static void addValue(final StringBuilder buf, final Double value) {
		StringUtils.addValue(buf, String.valueOf(value));
	}

	public static void addValue(final StringBuilder buf, final long value) {
		buf.append(',').append(value);
	}

	public static void addValue(final StringBuilder buf, final String value) {
		buf.append(',').append(value);
	}

	public static String getString(final String data) {
		if (!StringUtils.isParseable(data)) {
			return null;
		}
		return data;
	}

	public static boolean isParseable(final String data) {
		return !((data == null) || data.equals("N/A") || data.equals("-") || data.equals("") || data.equals("nan"));
	}

	public static String join(final String[] data, final String d) {
		if (data.length == 0) {
			return "";
		}
		final StringBuilder sb = new StringBuilder();
		int i;

		for (i = 0; i < (data.length - 1); i++) {
			sb.append(data[i]).append(d);
		}
		return sb.append(data[i]).toString();
	}

	/**
	 * Strips the unwanted chars from a line returned in the CSV Used for parsing
	 * the FX CSV lines
	 *
	 * @param line the original CSV line
	 * @return the stripped line
	 */
	public static String stripOverhead(final String line) {
		return line.replaceAll("\"", "");
	}

	public static String unescape(final String data) {
		final StringBuilder buffer = new StringBuilder(data.length());
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) > 256) {
				buffer.append("\\u").append(Integer.toHexString(data.charAt(i)));
			} else {
				if (data.charAt(i) == '\n') {
					buffer.append("\\n");
				} else if (data.charAt(i) == '\t') {
					buffer.append("\\t");
				} else if (data.charAt(i) == '\r') {
					buffer.append("\\r");
				} else if (data.charAt(i) == '\b') {
					buffer.append("\\b");
				} else if (data.charAt(i) == '\f') {
					buffer.append("\\f");
				} else if (data.charAt(i) == '\'') {
					buffer.append("\\'");
				} else if (data.charAt(i) == '\"') {
					buffer.append("\\\"");
				} else if (data.charAt(i) == '\\') {
					buffer.append("\\\\");
				} else {
					buffer.append(data.charAt(i));
				}
			}
		}
		return buffer.toString();
	}

}
