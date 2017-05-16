package com.leonarduk.finance.utils;

import java.util.Iterator;

import org.joda.time.LocalDate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testGetDiffInWorkDays() {
		Assert.assertEquals(7, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-12")));
		Assert.assertEquals(1, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-04")));
		Assert.assertEquals(5, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-10")));
		Assert.assertEquals(6, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-11")));
		Assert.assertEquals(8, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-13")));
		Assert.assertEquals(10, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"),
		        LocalDate.parse("2017-04-17")));
	}

	@Test
	public final void testGetLocalDateIterator() {
		final Iterator<LocalDate> iter = DateUtils
		        .getLocalDateIterator(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-10"));
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-03"), iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-04"), iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-05"), iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-06"), iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-07"), iter.next());
		Assert.assertTrue(iter.hasNext());
		Assert.assertEquals(LocalDate.parse("2017-04-10"), iter.next());
		Assert.assertFalse(iter.hasNext());
	}

}
