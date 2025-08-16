package com.leonarduk.finance.utils;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Iterator;

public class DateUtilsTest {

    private static final String APRIL10 = "2017-04-10";
    private static final String APRIL3 = "2017-04-03";
    private static final String APRIL4 = "2017-04-04";

    @Test
    @Ignore
    public final void testGetDiffInWorkDays() {

        Assert.assertEquals(3,
                DateUtils.getDiffInWorkDays(LocalDate.parse("2009-08-28"), LocalDate.parse("2009-09-01")));

        Assert.assertEquals(8,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-12")));
        Assert.assertEquals(2, DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL4)));
        Assert.assertEquals(6, DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL10)));
        Assert.assertEquals(7,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-11")));
        Assert.assertEquals(9,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-13")));
        Assert.assertEquals(11,
                DateUtils.getDiffInWorkDays(LocalDate.parse(DateUtilsTest.APRIL3), LocalDate.parse("2017-04-17")));
    }

    @Test
    public void testGetDiffInWorkDaysSkipsBankHolidays() {
        LocalDate start = LocalDate.parse("2023-04-28");
        LocalDate end = LocalDate.parse("2023-05-09");
        Assert.assertEquals(6, DateUtils.getDiffInWorkDays(start, end));
    }

    @Test
    public void testLocalDateIteratorSkipsBankHolidays() {
        Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(LocalDate.parse("2022-12-23"),
                LocalDate.parse("2022-12-28"));
        Assert.assertEquals(LocalDate.parse("2022-12-23"), iter.next());
        Assert.assertEquals(LocalDate.parse("2022-12-28"), iter.next());
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public final void testGetLocalDateIterator() {
        final Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(LocalDate.parse(DateUtilsTest.APRIL3),
                LocalDate.parse(DateUtilsTest.APRIL10));
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(LocalDate.parse(DateUtilsTest.APRIL3), iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(LocalDate.parse(DateUtilsTest.APRIL4), iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(LocalDate.parse("2017-04-05"), iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(LocalDate.parse("2017-04-06"), iter.next());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals(LocalDate.parse("2017-04-07"), iter.next());
        Assert.assertEquals(LocalDate.parse(DateUtilsTest.APRIL10), iter.next());
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void testGetPreviousDate() throws Exception {
        Assert.assertEquals(LocalDate.parse("2017-06-02"), DateUtils.getPreviousDate(LocalDate.parse("2017-06-05")));

        Assert.assertEquals(LocalDate.parse("2017-06-01"), DateUtils.getPreviousDate(LocalDate.parse("2017-06-02")));

    }

    @Test
    public void testLocalDateToCalendarConversion() {
        LocalDate localDate = LocalDate.of(2023, 5, 20);
        Calendar calendar = DateUtils.localDateToCalendar(localDate);
        Assert.assertEquals(localDate.getYear(), calendar.get(Calendar.YEAR));
        Assert.assertEquals(localDate.getMonthValue() - 1, calendar.get(Calendar.MONTH));
        Assert.assertEquals(localDate.getDayOfMonth(), calendar.get(Calendar.DAY_OF_MONTH));
        Assert.assertEquals(ZoneId.systemDefault(), calendar.getTimeZone().toZoneId());
    }

    @Test
    public void testCalendarToLocalDateConversion() {
        LocalDate localDate = LocalDate.of(2023, 5, 20);
        Calendar calendar = DateUtils.localDateToCalendar(localDate);
        LocalDate converted = DateUtils.calendarToLocalDate(calendar);
        Assert.assertEquals(localDate, converted);
    }
}
