package com.leonarduk.finance.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

/**
 *
 * @author Stijn Strickx
 */
public class Utils {

	public static final BigDecimal HUNDRED = getBigDecimal("100");
	public static final BigDecimal THOUSAND = getBigDecimal("1000");
	public static final BigDecimal MILLION = getBigDecimal("1000000");
	public static final BigDecimal BILLION = getBigDecimal("1000000000");

	private static Map<String, BigDecimal> bigDecimals;

	private static Object monitor = new Object();

	public static final Logger logger = Logger.getLogger(Utils.class.getName());

	private static BigDecimal calculateBigDecimal(String data) {
		if (!Utils.isParseable(data)) {
			return null;
		}
		try {
			data = Utils.cleanNumberString(data);
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
		return Utils.join(data.trim().split(","), "");
	}

	public static BigDecimal getBigDecimal(final String data) {
		return calculateBigDecimal(data);
		// if (null == bigDecimals) {
		// bigDecimals = Maps.newConcurrentMap();
		// }
		// final BigDecimal computeIfAbsent = bigDecimals.computeIfAbsent(data,
		// d -> calculateBigDecimal(d));
		// return computeIfAbsent;
	}

	public static BigDecimal getBigDecimal(final String dataMain, final String dataSub) {
		final BigDecimal main = getBigDecimal(dataMain);
		final BigDecimal sub = getBigDecimal(dataSub);
		if ((main == null) || (main.compareTo(BigDecimal.ZERO) == 0)) {
			return sub;
		}
		return main;
	}

	private static String getDividendDateFormat(final String date) {
		if (date.matches("[0-9][0-9]-...-[0-9][0-9]")) {
			return "dd-MMM-yy";
		} else if (date.matches("[0-9]-...-[0-9][0-9]")) {
			return "d-MMM-yy";
		} else if (date.matches("...[ ]+[0-9]+")) {
			return "MMM d";
		} else {
			return "M/d/yy";
		}
	}

	public static double getDouble(String data) {
		double result = Double.NaN;
		if (!Utils.isParseable(data)) {
			return result;
		}
		try {
			data = Utils.cleanNumberString(data);
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
		if (!Utils.isParseable(data)) {
			return result;
		}
		try {
			data = Utils.cleanNumberString(data);
			result = Integer.parseInt(data);
		} catch (final NumberFormatException e) {
			logger.log(Level.WARNING, "Failed to parse: " + data);
			logger.log(Level.FINEST, "Failed to parse: " + data, e);
		}
		return result;
	}

	public static Long getLong(String data) {
		Long result = null;
		if (!Utils.isParseable(data)) {
			return result;
		}
		try {
			data = Utils.cleanNumberString(data);
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

	public static String getString(final String data) {
		if (!Utils.isParseable(data)) {
			return null;
		}
		return data;
	}

	public static String getURLParameters(final Map<String, String> params) {
		final StringBuilder sb = new StringBuilder();

		for (final Entry<String, String> entry : params.entrySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			String key = entry.getKey();
			String value = entry.getValue();
			try {
				key = URLEncoder.encode(key, "UTF-8");
				value = URLEncoder.encode(value, "UTF-8");
			} catch (final UnsupportedEncodingException ex) {
				logger.log(Level.SEVERE, ex.getMessage(), ex);
				// Still try to continue with unencoded values
			}
			sb.append(String.format("%s=%s", key, value));
		}
		return sb.toString();
	}

	private static boolean isParseable(final String data) {
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
	 * Used to parse the last trade date / time. Returns null if the date / time
	 * cannot be parsed.
	 *
	 * @param date
	 *            String received that represents the date
	 * @param time
	 *            String received that represents the time
	 * @param timeZone
	 *            time zone to use for parsing the date time
	 * @return Calendar object with the parsed datetime
	 */
	public static Calendar parseDateTime(final String date, final String time, final TimeZone timeZone) {
		final String datetime = date + " " + time;
		final SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy h:mma", Locale.US);

		format.setTimeZone(timeZone);
		try {
			if (Utils.isParseable(date) && Utils.isParseable(time)) {
				final Calendar c = Calendar.getInstance();
				c.setTime(format.parse(datetime));
				return c;
			}
		} catch (final ParseException ex) {
			logger.log(Level.WARNING, "Failed to parse datetime: " + datetime);
			logger.log(Level.FINEST, "Failed to parse datetime: " + datetime, ex);
		}
		return null;
	}

	/**
	 * Used to parse the dividend dates. Returns null if the date cannot be
	 * parsed.
	 *
	 * @param date
	 *            String received that represents the date
	 * @return Calendar object representing the parsed date
	 */
	public static Calendar parseDividendDate(String date) {
		if (!Utils.isParseable(date)) {
			return null;
		}
		date = date.trim();
		final SimpleDateFormat format = new SimpleDateFormat(Utils.getDividendDateFormat(date), Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
		try {
			final Calendar today = Calendar.getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
			final Calendar parsedDate = Calendar.getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
			parsedDate.setTime(format.parse(date));

			if (parsedDate.get(Calendar.YEAR) == 1970) {
				// Not really clear which year the dividend date is... making a
				// reasonable guess.
				final int monthDiff = parsedDate.get(Calendar.MONTH) - today.get(Calendar.MONTH);
				int year = today.get(Calendar.YEAR);
				if (monthDiff > 6) {
					year -= 1;
				} else if (monthDiff < -6) {
					year += 1;
				}
				parsedDate.set(Calendar.YEAR, year);
			}

			return parsedDate;
		} catch (final ParseException ex) {
			logger.log(Level.WARNING, "Failed to parse dividend date: " + date);
			logger.log(Level.FINEST, "Failed to parse dividend date: " + date, ex);
			return null;
		}
	}

	public static Calendar parseHistDate(final String date) {
		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		try {
			if (Utils.isParseable(date)) {
				final Calendar c = Calendar.getInstance();
				c.setTime(format.parse(date));
				return c;
			}
		} catch (final ParseException ex) {
			logger.log(Level.WARNING, "Failed to parse hist date: " + date);
			logger.log(Level.FINEST, "Failed to parse hist date: " + date, ex);
		}
		return null;
	}

	/**
	 * Strips the unwanted chars from a line returned in the CSV Used for
	 * parsing the FX CSV lines
	 *
	 * @param line
	 *            the original CSV line
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
