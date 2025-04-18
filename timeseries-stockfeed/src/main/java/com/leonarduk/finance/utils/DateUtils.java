/*
 * Copyright (c) 2011 Kevin Sawicki <kevinsawicki@gmail.com>
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
package com.leonarduk.finance.utils;

import com.google.common.collect.Maps;
import com.leonarduk.finance.stockfeed.feed.yahoofinance.YahooFeed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Helpers for common dates
 */
public class DateUtils {
    public static final Logger logger = LoggerFactory.getLogger(DateUtils.class.getName());
    private static Map<String, Date> dates;

    public static LocalDate calendarToLocalDate(Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    public static Calendar dateToCalendar(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static int getDiffInWorkDays(final LocalDate startDate, final LocalDate endDate) {
        return getDiffInWorkDays(startDate, endDate, Optional.empty());
    }

    public static int getDiffInWorkDays(final LocalDate startDate, final LocalDate endDate, final Optional<List<LocalDate>> holidays) {
        // Validate method arguments
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Invalid method argument(s) to countBusinessDaysBetween (" + startDate + "," + endDate + "," + holidays + ")");
        }

        // Predicate 1: Is a given date is a holiday
        Predicate<LocalDate> isHoliday = date -> holidays.isPresent()
                && holidays.get().contains(date);

        // Iterate over stream of all dates and check each day against any weekday or
        // holiday
        List<LocalDate> businessDays = startDate.datesUntil(endDate)
                .filter(isWeekend().or(isHoliday).negate())
                .collect(Collectors.toList());

        return businessDays.size();
    }

    public static Predicate<LocalDate> isWeekend() {
        return date -> date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY;
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

    public static Iterator<LocalDate> getLocalDateIterator(final LocalDate oldestDate, final LocalDate mostRecentDate) {
        return new Iterator<>() {

            LocalDate nextDate = oldestDate;

            @Override
            public boolean hasNext() {
                return this.nextDate.isBefore(mostRecentDate) || this.nextDate.equals(mostRecentDate);
            }

            @Override
            public LocalDate next() {
                final LocalDate currentDate = this.nextDate;
                if (this.nextDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
                    this.nextDate = this.nextDate.plusDays(2);
                }
                this.nextDate = this.nextDate.plusDays(1);
                return currentDate;
            }

        };

    }

    public static LocalDate getPreviousDate(final LocalDate localDate) {
        final LocalDate returnDate = localDate.minusDays(1);
        return getLastWeekday(returnDate);
    }

    public static LocalDate getLastWeekday(final LocalDate returnDate) {
        if ((returnDate.getDayOfWeek() == DayOfWeek.SATURDAY) || (returnDate.getDayOfWeek() == DayOfWeek.SUNDAY)) {
            return DateUtils.getPreviousDate(returnDate);
        }
        return returnDate;
    }

    public static Date parseDate(final String fieldValue) throws ParseException {
        if (null == DateUtils.dates) {
            DateUtils.dates = Maps.newConcurrentMap();
        }
        return (DateUtils.dates.computeIfAbsent(fieldValue,
                v -> DateUtils.convertToDateViaInstant(LocalDate.parse(v, DateTimeFormatter.ISO_DATE))));
    }

    /**
     * Used to parse the dividend dates. Returns null if the date cannot be parsed.
     *
     * @param date String received that represents the date
     * @return Calendar object representing the parsed date
     */
    public static Calendar parseDividendDate(final String date) {
        if (!StringUtils.isParseable(date)) {
            return null;
        }
        final SimpleDateFormat format = new SimpleDateFormat(DateUtils.getDividendDateFormat(date.trim()), Locale.US);
        format.setTimeZone(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
        try {
            final Calendar today = Calendar.getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
            final Calendar parsedDate = Calendar.getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
            parsedDate.setTime(format.parse(date.trim()));

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
            DateUtils.logger.warn("Failed to parse dividend date: " + date);
            DateUtils.logger.trace("Failed to parse dividend date: " + date, ex);
            return null;
        }
    }

    public static Date convertToDateViaInstant(LocalDate fromDate) {
        return java.util.Date.from(fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
