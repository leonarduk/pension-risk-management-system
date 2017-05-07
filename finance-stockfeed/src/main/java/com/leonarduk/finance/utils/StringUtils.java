package com.leonarduk.finance.utils;

public class StringUtils {
	static boolean isParseable(final String data) {
		return !((data == null) || data.equals("N/A") || data.equals("-") || data.equals("") || data.equals("nan"));
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
