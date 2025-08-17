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
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Helpers for common dates
 */
@Slf4j
public class DateUtils {
    private static Map<String, Date> dates;

    /**
     * Path to the default holiday configuration file on the classpath.
     */
    private static final String HOLIDAY_RESOURCE = "/uk_bank_holidays.json";

    /**
     * List of UK bank holidays loaded from a configuration file. The default
     * configuration is bundled with the application but can be replaced or
     * reloaded using {@link #loadHolidays(String)}.
     */
    public static final List<LocalDate> UK_BANK_HOLIDAYS = Collections
            .unmodifiableList(loadHolidays(HOLIDAY_RESOURCE));

    /**
     * Load a list of holidays from a JSON resource containing an array of ISO-8601
     * date strings. The resource is read from the classpath.
     *
     * @param resourcePath path to the JSON resource
     * @return list of {@link LocalDate} instances. An empty list is returned if the
     * resource cannot be read.
     */
    public static List<LocalDate> loadHolidays(String resourcePath) {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream in = DateUtils.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                log.warn("Holiday resource {} not found", resourcePath);
                return Collections.emptyList();
            }
            List<String> dates = mapper.readValue(in, new TypeReference<List<String>>() {
            });
            return dates.stream().map(LocalDate::parse).collect(Collectors.toList());
        } catch (IOException e) {
            log.warn("Failed to load holidays from {}", resourcePath, e);
            return Collections.emptyList();
        }
    }

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
        } catch (final ParseException ex) {
            log.warn("Failed to parse dividend date: {}", date);
            log.trace("Failed to parse dividend date: {}", date, ex);
            return null;
        }
    }

    public static Date convertToDateViaInstant(final LocalDate fromDate) {
        return java.util.Date.from(fromDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
