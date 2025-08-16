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
public enum DateUtils {
    ;
    public static final Logger logger = LoggerFactory.getLogger(DateUtils.class.getName());
    private static Map<String, Date> dates;

    /**
     * Static list of UK bank holidays used when determining working days.
     * <p>
     * The list is intentionally limited to the most recent years required by the
     * application and test cases. It can be extended as needed.
     */
    public static final List<LocalDate> UK_BANK_HOLIDAYS = Collections.unmodifiableList(Arrays.asList(
            // 2022
            LocalDate.of(2022, 1, 3), // New Year's Day (substitute)
            LocalDate.of(2022, 4, 15), // Good Friday
            LocalDate.of(2022, 4, 18), // Easter Monday
            LocalDate.of(2022, 5, 2), // Early May bank holiday
            LocalDate.of(2022, 6, 2), // Spring bank holiday
            LocalDate.of(2022, 6, 3), // Platinum Jubilee bank holiday
            LocalDate.of(2022, 8, 29), // Summer bank holiday
            LocalDate.of(2022, 9, 19), // Queen's funeral
            LocalDate.of(2022, 12, 26), // Boxing Day
            LocalDate.of(2022, 12, 27), // Christmas Day (substitute)
            // 2023
            LocalDate.of(2023, 1, 2), // New Year's Day (substitute)
            LocalDate.of(2023, 4, 7), // Good Friday
            LocalDate.of(2023, 4, 10), // Easter Monday
            LocalDate.of(2023, 5, 1), // Early May bank holiday
            LocalDate.of(2023, 5, 8), // Coronation of King Charles III
            LocalDate.of(2023, 5, 29), // Spring bank holiday
            LocalDate.of(2023, 8, 28), // Summer bank holiday
            LocalDate.of(2023, 12, 25), // Christmas Day
            LocalDate.of(2023, 12, 26), // Boxing Day
            // 2024
            LocalDate.of(2024, 1, 1), // New Year's Day
            LocalDate.of(2024, 3, 29), // Good Friday
            LocalDate.of(2024, 4, 1), // Easter Monday
            LocalDate.of(2024, 5, 6), // Early May bank holiday
            LocalDate.of(2024, 5, 27), // Spring bank holiday
            LocalDate.of(2024, 8, 26), // Summer bank holiday
            LocalDate.of(2024, 12, 25), // Christmas Day
            LocalDate.of(2024, 12, 26)  // Boxing Day
    ));

    public static LocalDate calendarToLocalDate(final Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toLocalDate();
    }

    public static Calendar localDateToCalendar(final LocalDate date) {
        final ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.systemDefault());
        return GregorianCalendar.from(zonedDateTime);
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    public static int getDiffInWorkDays(LocalDate startDate, LocalDate endDate) {
        return DateUtils.getDiffInWorkDays(startDate, endDate, Optional.of(DateUtils.UK_BANK_HOLIDAYS));
    }

    public static int getDiffInWorkDays(LocalDate startDate, LocalDate endDate,
                                        Optional<List<LocalDate>> holidays) {
        // Validate method arguments
        if (null == startDate || null == endDate) {
            throw new IllegalArgumentException(
                    "Invalid method argument(s) to countBusinessDaysBetween (" + startDate + "," + endDate + "," + holidays + ")");
        }

        final List<LocalDate> holidayList = holidays.orElse(DateUtils.UK_BANK_HOLIDAYS);
        // Predicate 1: Is a given date a holiday
        final Predicate<LocalDate> isHoliday = holidayList::contains;

        // Iterate over stream of all dates and check each day against any weekday or
        // holiday
        // `datesUntil` excludes the final date. Include it to count the end date when
        // it falls on a working day.
        final List<LocalDate> businessDays = startDate.datesUntil(endDate.plusDays(1))
                .filter(DateUtils.isWeekend().or(isHoliday).negate())
                .collect(Collectors.toList());

        return businessDays.size();
    }

    public static Predicate<LocalDate> isWeekend() {
        return date -> DayOfWeek.SATURDAY == date.getDayOfWeek()
                || DayOfWeek.SUNDAY == date.getDayOfWeek();
    }

    public static Predicate<LocalDate> isHoliday() {
        return DateUtils.UK_BANK_HOLIDAYS::contains;
    }

    private static String getDividendDateFormat(String date) {
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

    public static Iterator<LocalDate> getLocalDateIterator(LocalDate oldestDate, LocalDate mostRecentDate) {
        return new Iterator<>() {

            LocalDate nextDate = oldestDate;

            @Override
            public boolean hasNext() {
                return !nextDate.isAfter(mostRecentDate);
            }

            @Override
            public LocalDate next() {
                LocalDate currentDate = nextDate;
                do {
                    nextDate = nextDate.plusDays(1);
                } while (DateUtils.isWeekend().or(DateUtils.isHoliday()).test(nextDate) && nextDate.isBefore(mostRecentDate.plusDays(1)));
                return currentDate;
            }

        };

    }

    public static LocalDate getPreviousDate(LocalDate localDate) {
        LocalDate returnDate = localDate.minusDays(1);
        return DateUtils.getLastWeekday(returnDate);
    }

    public static LocalDate getLastWeekday(LocalDate returnDate) {
        if (DateUtils.isWeekend().or(DateUtils.isHoliday()).test(returnDate)) {
            return getPreviousDate(returnDate);
        }
        return returnDate;
    }

    public static Date parseDate(String fieldValue) throws ParseException {
        if (null == dates) {
            dates = Maps.newConcurrentMap();
        }
        return (dates.computeIfAbsent(fieldValue,
                v -> convertToDateViaInstant(LocalDate.parse(v, DateTimeFormatter.ISO_DATE))));
    }

    public static final String TIMEZONE = "America/New_York";

    /**
     * Used to parse the dividend dates. Returns null if the date cannot be parsed.
     *
     * @param date String received that represents the date
     * @return Calendar object representing the parsed date
     */
    public static Calendar parseDividendDate(String date) {
        if (StringUtils.isNotParseable(date)) {
            return null;
        }
        
        SimpleDateFormat format = new SimpleDateFormat(getDividendDateFormat(date.trim()), Locale.US);
        format.setTimeZone(TimeZone.getTimeZone(DateUtils.TIMEZONE));
        try {
            Calendar today = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.TIMEZONE));
            Calendar parsedDate = Calendar.getInstance(TimeZone.getTimeZone(DateUtils.TIMEZONE));
            parsedDate.setTime(format.parse(date.trim()));

            if (1970 == parsedDate.get(Calendar.YEAR)) {
                // Not really clear which year the dividend date is... making a
                // reasonable guess.
                int monthDiff = parsedDate.get(Calendar.MONTH) - today.get(Calendar.MONTH);
                int year = today.get(Calendar.YEAR);
                if (6 < monthDiff) {
                    year -= 1;
                } else if (-6 > monthDiff) {
                    year += 1;
                }
                parsedDate.set(Calendar.YEAR, year);
            }

            return parsedDate;
        } catch (ParseException ex) {
            logger.warn("Failed to parse dividend date: {}", date);
            logger.trace("Failed to parse dividend date: {}", date, ex);
            return null;
        }
    }

    public static Date convertToDateViaInstant(final LocalDate fromDate) {
        return java.util.Date.from(fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
