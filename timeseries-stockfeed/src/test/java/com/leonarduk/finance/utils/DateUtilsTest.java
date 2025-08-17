package com.leonarduk.finance.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class DateUtilsTest {

    private static final String APRIL10 = "2017-04-10";
    private static final String APRIL3 = "2017-04-03";
    private static final String APRIL4 = "2017-04-04";

    @Test
    @Disabled
    public final void testGetDiffInWorkDays() {

        Assertions.assertEquals(3,
                DateUtils.getDiffInWorkDays(LocalDate.parse("2009-08-28"), LocalDate.parse("2009-09-01")));

        Assertions.assertEquals(8,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-12")));
        Assertions.assertEquals(2, DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL4)));
        Assertions.assertEquals(6, DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL10)));
        Assertions.assertEquals(7,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-11")));
        Assertions.assertEquals(9,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-13")));
        Assertions.assertEquals(11,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-17")));
    }

    @Test
    public void testGetDiffInWorkDaysSkipsConfiguredHolidays() {
        LocalDate start = LocalDate.parse("2023-04-28");
        LocalDate end = LocalDate.parse("2023-05-09");
        List<LocalDate> holidays = DateUtils.loadHolidays("/uk_bank_holidays.json");
        Assertions.assertTrue(holidays.contains(LocalDate.parse("2023-05-08")));
        Assertions.assertEquals(6, DateUtils.getDiffInWorkDays(start, end, Optional.of(holidays)));
    }

    @Test
    public void testIsHolidayFromConfig() {
        Assertions.assertTrue(DateUtils.isHoliday().test(LocalDate.parse("2023-05-08")));
    }

    @Test
    public void testLocalDateIteratorSkipsBankHolidays() {
        Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(LocalDate.parse("2022-12-23"),
                LocalDate.parse("2022-12-28"));
        Assertions.assertEquals(LocalDate.parse("2022-12-23"), iter.next());
        Assertions.assertEquals(LocalDate.parse("2022-12-28"), iter.next());
        Assertions.assertFalse(iter.hasNext());
    }

    @Test
    public final void testGetLocalDateIterator() {
        final Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL10));
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(LocalDate.parse(DateUtilsTest.APRIL3), iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(LocalDate.parse(DateUtilsTest.APRIL4), iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(LocalDate.parse("2017-04-05"), iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(LocalDate.parse("2017-04-06"), iter.next());
        Assertions.assertTrue(iter.hasNext());
        Assertions.assertEquals(LocalDate.parse("2017-04-07"), iter.next());
        Assertions.assertEquals(LocalDate.parse(DateUtilsTest.APRIL10), iter.next());
        Assertions.assertFalse(iter.hasNext());
    }

    @Test
    public void testGetPreviousDate() throws Exception {
        Assertions.assertEquals(LocalDate.parse("2017-06-02"), DateUtils.getPreviousDate(LocalDate.parse("2017-06-05")));

        Assertions.assertEquals(LocalDate.parse("2017-06-01"), DateUtils.getPreviousDate(LocalDate.parse("2017-06-02")));

    }

    @Test
    public void testLocalDateToCalendarConversion() {
        LocalDate localDate = LocalDate.of(2023, 5, 20);
        Calendar calendar = DateUtils.localDateToCalendar(localDate);
        Assertions.assertEquals(localDate.getYear(), calendar.get(Calendar.YEAR));
        Assertions.assertEquals(localDate.getMonthValue() - 1, calendar.get(Calendar.MONTH));
        Assertions.assertEquals(localDate.getDayOfMonth(), calendar.get(Calendar.DAY_OF_MONTH));
        Assertions.assertEquals(ZoneId.systemDefault(), calendar.getTimeZone().toZoneId());
    }

    @Test
    public void testCalendarToLocalDateConversion() {
        LocalDate localDate = LocalDate.of(2023, 5, 20);
        Calendar calendar = DateUtils.localDateToCalendar(localDate);
        LocalDate converted = DateUtils.calendarToLocalDate(calendar);
        Assertions.assertEquals(localDate, converted);
    }
}
