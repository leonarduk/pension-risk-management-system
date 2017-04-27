package com.leonarduk.finance.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

public class DateUtilsTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public final void testGetDiffInWorkDays() {
		assertEquals(7, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-12")));
		assertEquals(1, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-04")));
		assertEquals(5, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-10")));
		assertEquals(6, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-11")));
		assertEquals(8, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-13")));
		assertEquals(10, DateUtils.getDiffInWorkDays(LocalDate.parse("2017-04-03"), LocalDate.parse("2017-04-17")));
	}

	@Test
	public final void testGetLocalDateIterator() {
		final Iterator<LocalDate> iter = DateUtils.getLocalDateIterator(LocalDate.parse("2017-04-03"),
				LocalDate.parse("2017-04-10"));
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-03"), iter.next());
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-04"), iter.next());
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-05"), iter.next());
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-06"), iter.next());
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-07"), iter.next());
		assertTrue(iter.hasNext());
		assertEquals(LocalDate.parse("2017-04-10"), iter.next());
		assertFalse(iter.hasNext());
	}

}
