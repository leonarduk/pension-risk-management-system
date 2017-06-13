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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;

import jersey.repackaged.com.google.common.collect.Maps;

/**
 * Helpers for common dates
 */
public class DateUtils {
	private static Map<String, Date>	dates;

	public static final Logger			logger	= Logger
	        .getLogger(DateUtils.class.getName());

	public static Calendar dateToCalendar(final Date date) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}

	public static Calendar dateToCalendar(final LocalDate fromDate) {
		return DateUtils.dateToCalendar(fromDate.toDate());
	}

	public static int getDiffInWorkDays(final LocalDate currentDate,
	        final LocalDate nextDate) {
		final int calendarDaysDiff = Math
		        .abs(Days.daysBetween(nextDate, currentDate).getDays());
		final int weeks = Math.round(calendarDaysDiff / 7);
		return Math.abs((5 * weeks) + Math.min(calendarDaysDiff % 7, 5));
	}

	private static String getDividendDateFormat(final String date) {
		if (date.matches("[0-9][0-9]-...-[0-9][0-9]")) {
			return "dd-MMM-yy";
		}
		else if (date.matches("[0-9]-...-[0-9][0-9]")) {
			return "d-MMM-yy";
		}
		else if (date.matches("...[ ]+[0-9]+")) {
			return "MMM d";
		}
		else {
			return "M/d/yy";
		}
	}

	public static Iterator<LocalDate> getLocalDateIterator(
	        final LocalDate startDate, final LocalDate lastDate) {
		return new Iterator<LocalDate>() {

			LocalDate nextDate = startDate;

			@Override
			public boolean hasNext() {
				return this.nextDate.isBefore(lastDate)
				        || this.nextDate.equals(lastDate);
			}

			@Override
			public LocalDate next() {
				final LocalDate currentDate = this.nextDate;
				if (this.nextDate.getDayOfWeek() == DateTimeConstants.FRIDAY) {
					this.nextDate = this.nextDate.plusDays(2);
				}
				this.nextDate = this.nextDate.plusDays(1);
				return currentDate;
			}

		};

	}

	public static Iterator<LocalDate> getLocalDateNewToOldIterator(
	        final LocalDate startDate, final LocalDate lastDate) {
		return new Iterator<LocalDate>() {

			LocalDate nextDate = startDate;

			@Override
			public boolean hasNext() {
				return this.nextDate.isAfter(lastDate)
				        || this.nextDate.equals(lastDate);
			}

			@Override
			public LocalDate next() {
				if (this.nextDate.getDayOfWeek() == DateTimeConstants.SUNDAY) {
					this.nextDate = this.nextDate.minusDays(2);
				}
				if (this.nextDate
				        .getDayOfWeek() == DateTimeConstants.SATURDAY) {
					this.nextDate = this.nextDate.minusDays(1);
				}
				final LocalDate currentDate = this.nextDate;
				this.nextDate = this.nextDate.minusDays(1);
				return currentDate;
			}

		};

	}

	public static LocalDate getPreviousDate(final LocalDate currentDate) {
		final LocalDate returnDate = currentDate.minusDays(1);
		if ((returnDate.getDayOfWeek() == DateTimeConstants.SATURDAY)
		        || (returnDate.getDayOfWeek() == DateTimeConstants.SUNDAY)) {
			return DateUtils.getPreviousDate(returnDate);
		}
		return returnDate;
	}

	public static Date parseDate(final String fieldValue)
	        throws ParseException {
		if (null == DateUtils.dates) {
			DateUtils.dates = Maps.newConcurrentMap();
		}
		return (DateUtils.dates.computeIfAbsent(fieldValue,
		        v -> LocalDate.parse(v).toDate()));
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
	public static Calendar parseDateTime(final String date, final String time,
	        final TimeZone timeZone) {
		final String datetime = date + " " + time;
		final SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy h:mma",
		        Locale.US);

		format.setTimeZone(timeZone);
		try {
			if (StringUtils.isParseable(date)
			        && StringUtils.isParseable(time)) {
				final Calendar c = Calendar.getInstance();
				c.setTime(format.parse(datetime));
				return c;
			}
		}
		catch (final ParseException ex) {
			DateUtils.logger.log(Level.WARNING,
			        "Failed to parse datetime: " + datetime);
			DateUtils.logger.log(Level.FINEST,
			        "Failed to parse datetime: " + datetime, ex);
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
	public static Calendar parseDividendDate(final String date) {
		if (!StringUtils.isParseable(date)) {
			return null;
		}
		final SimpleDateFormat format = new SimpleDateFormat(
		        DateUtils.getDividendDateFormat(date.trim()), Locale.US);
		format.setTimeZone(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
		try {
			final Calendar today = Calendar
			        .getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
			final Calendar parsedDate = Calendar
			        .getInstance(TimeZone.getTimeZone(YahooFeed.TIMEZONE));
			parsedDate.setTime(format.parse(date.trim()));

			if (parsedDate.get(Calendar.YEAR) == 1970) {
				// Not really clear which year the dividend date is... making a
				// reasonable guess.
				final int monthDiff = parsedDate.get(Calendar.MONTH)
				        - today.get(Calendar.MONTH);
				int year = today.get(Calendar.YEAR);
				if (monthDiff > 6) {
					year -= 1;
				}
				else if (monthDiff < -6) {
					year += 1;
				}
				parsedDate.set(Calendar.YEAR, year);
			}

			return parsedDate;
		}
		catch (final ParseException ex) {
			DateUtils.logger.log(Level.WARNING,
			        "Failed to parse dividend date: " + date);
			DateUtils.logger.log(Level.FINEST,
			        "Failed to parse dividend date: " + date, ex);
			return null;
		}
	}

}
