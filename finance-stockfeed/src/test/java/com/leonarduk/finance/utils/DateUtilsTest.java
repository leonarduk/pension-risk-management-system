package com.leonarduk.finance.utils;

import java.util.Iterator;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;

public class DateUtilsTest {

	private static final String	APRIL10	= "2017-04-10";
	private static final String	APRIL3	= "2017-04-03";
	private static final String	APRIL4	= "2017-04-04";

	@Test
	public final void testGetDiffInWorkDays() {
		Assert.assertEquals(7,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse("2017-04-12")));
		Assert.assertEquals(1,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse(DateUtilsTest.APRIL4)));
		Assert.assertEquals(5,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse(DateUtilsTest.APRIL10)));
		Assert.assertEquals(6,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse("2017-04-11")));
		Assert.assertEquals(8,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse("2017-04-13")));
		Assert.assertEquals(10,
		        DateUtils.getDiffInWorkDays(
		                LocalDate.parse(DateUtilsTest.APRIL3),
		                LocalDate.parse("2017-04-17")));
	}

	@Test
	public final void testGetLocalDateIterator() {
		final Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(
		        LocalDate.parse(DateUtilsTest.APRIL3),
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
		Assert.assertEquals(LocalDate.parse(DateUtilsTest.APRIL10),
		        iter.next());
		Assert.assertFalse(iter.hasNext());
	}

	@Test
	public void testGetPreviousDate() throws Exception {
		Assert.assertEquals(LocalDate.parse("2017-06-02"),
		        DateUtils.getPreviousDate(LocalDate.parse("2017-06-05")));

		Assert.assertEquals(LocalDate.parse("2017-06-01"),
		        DateUtils.getPreviousDate(LocalDate.parse("2017-06-02")));

	}
}
