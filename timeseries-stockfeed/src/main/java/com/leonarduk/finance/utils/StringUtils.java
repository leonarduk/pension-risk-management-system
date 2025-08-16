package com.leonarduk.finance.utils;

import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringUtils {
    private static final NumberFormat formatter = new DecimalFormat("#0.00");

    public static void addValue(final StringBuilder buf, final Num num) {
        if (num == null) {
            StringUtils.addValue(buf, "");
        } else {
            final String format = StringUtils.formatter.format(num.doubleValue());
            StringUtils.addValue(buf, format);
        }
    }

    public static void addValue(final StringBuilder buf, final BigDecimal value) {
        if (value == null) {
            StringUtils.addValue(buf, "");
        } else {
            StringUtils.addValue(buf, value.doubleValue());
        }
    }

    public static void addValue(final StringBuilder buf, final Double value) {
        if (value == null) {
            StringUtils.addValue(buf, "");
        } else {
            StringUtils.addValue(buf, String.valueOf(value));
        }
    }

    public static void addValue(final StringBuilder buf, final long value) {
        StringUtils.addValue(buf, String.valueOf(value));
    }

    public static void addValue(final StringBuilder buf, final String value) {
        buf.append(',');
        if (value == null) {
            return;
        }
        String escaped = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            buf.append('"').append(escaped).append('"');
        } else {
            buf.append(escaped);
        }
    }

    public static String getString(final String data) {
        if (StringUtils.isNotParseable(data)) {
            return null;
        }
        return data;
    }

    public static boolean isNotParseable(final String data) {
        return (data == null) || data.equals("N/A") || data.equals("-") || data.equals("") || data.equals("nan");
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
