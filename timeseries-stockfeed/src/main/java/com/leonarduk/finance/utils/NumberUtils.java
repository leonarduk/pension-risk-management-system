package com.leonarduk.finance.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class NumberUtils {

    public static final BigDecimal BILLION = NumberUtils.getBigDecimal("1000000000");
    private static DecimalFormat format;
    public static final BigDecimal HUNDRED = NumberUtils.getBigDecimal("100");
    public static final Logger logger = LoggerFactory.getLogger(NumberUtils.class.getName());

    public static final BigDecimal MILLION = NumberUtils.getBigDecimal("1000000");

    public static final BigDecimal THOUSAND = NumberUtils.getBigDecimal("1000");

    public static boolean areSame(final BigDecimal thisOne, final BigDecimal thatOne) {
        if (((thisOne == null) && (thatOne != null)) || ((thatOne == null) && (thisOne != null))) {
            return false;
        }

        return NumberUtils.roundDecimal(thisOne).equals(NumberUtils.roundDecimal(thatOne));
    }

    private static BigDecimal calculateBigDecimal(final String dataRaw) {
        if (!StringUtils.isParseable(dataRaw)) {
            return null;
        }
        String data;
        try {
            data = NumberUtils.cleanNumberString(dataRaw);
            final char lastChar = data.charAt(data.length() - 1);
            BigDecimal multiplier = BigDecimal.ONE;
            switch (lastChar) {
                case 'B':
                    data = data.substring(0, data.length() - 1);
                    multiplier = NumberUtils.BILLION;
                    break;
                case 'M':
                    data = data.substring(0, data.length() - 1);
                    multiplier = NumberUtils.MILLION;
                    break;
                case 'K':
                    data = data.substring(0, data.length() - 1);
                    multiplier = NumberUtils.THOUSAND;
                    break;
                default:
                    break;
            }
            return new BigDecimal(data).multiply(multiplier);
        } catch (final NumberFormatException e) {
            NumberUtils.logger.warn("Failed to parse: {}", dataRaw, e);
        }
        return null;
    }

    public static BigDecimal cleanBigDecimal(final BigDecimal input) {
        if (input == null) {
            return BigDecimal.ZERO;
        }
        return input.setScale(2, RoundingMode.DOWN);
    }

    private static String cleanNumberString(final String data) {
        return StringUtils.join(data.trim().split(","), "");
    }

    public static BigDecimal getBigDecimal(final String data) {
        return NumberUtils.calculateBigDecimal(data);
    }

    public static BigDecimal getBigDecimal(final String dataMain, final String dataSub) {
        final BigDecimal main = NumberUtils.getBigDecimal(dataMain);
        final BigDecimal sub = NumberUtils.getBigDecimal(dataSub);
        if ((main == null) || (main.compareTo(BigDecimal.ZERO) == 0)) {
            return sub;
        }
        return main;
    }

    public static Long getLong(final String data) {
        Long result = null;
        if (!StringUtils.isParseable(data)) {
            return result;
        }
        try {
            result = Long.parseLong(NumberUtils.cleanNumberString(data));
        } catch (final NumberFormatException e) {
            NumberUtils.logger.warn("Failed to parse: " + data);
            NumberUtils.logger.trace("Failed to parse: " + data, e);
        }
        return result;
    }

    public static BigDecimal getPercent(final BigDecimal numerator, final BigDecimal denominator) {
        if ((denominator == null) || (numerator == null) || (denominator.compareTo(BigDecimal.ZERO) == 0)) {
            return BigDecimal.ZERO;
        }
        return numerator
                .divide(denominator, 4, RoundingMode.HALF_EVEN)
                .multiply(NumberUtils.HUNDRED)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    public static BigDecimal roundDecimal(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Double roundDecimal(final Double dbl) {
        if (dbl.isNaN()) {
            return dbl;
        }
        NumberUtils.format = new DecimalFormat("#.##");
        return java.lang.Double.valueOf(NumberUtils.format.format(dbl.doubleValue()));
    }

}
