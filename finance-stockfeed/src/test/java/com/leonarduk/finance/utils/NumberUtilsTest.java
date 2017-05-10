package com.leonarduk.finance.utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import eu.verdelhan.ta4j.Decimal;

public class NumberUtilsTest {

	// @Before
	// public void setUp() throws Exception {
	// }
	//
	// @Test
	// public final void testGetBigDecimalString() {
	//
	// }
	//
	// @Test
	// public final void testGetBigDecimalStringString() {
	//
	// }
	//
	// @Test
	// public final void testGetDouble() {
	//
	// }
	//
	// @Test
	// public final void testGetInt() {
	//
	// }
	//
	// @Test
	// public final void testGetLong() {
	//
	// }
	//
	// @Test
	// public final void testGetPercentBigDecimalBigDecimal() {
	//
	// }
	//
	// @Test
	// public final void testGetPercentDoubleDouble() {
	//
	// }
	//
	// @Test
	// public final void testGetString() {
	//
	// }

	@Test
	public final void testRoundDecimalBigDecimal() {
		assertEquals(BigDecimal.valueOf(12.12),
		        NumberUtils.roundDecimal(BigDecimal.valueOf(12.1234)));
		assertEquals(BigDecimal.valueOf(12.13),
		        NumberUtils.roundDecimal(BigDecimal.valueOf(12.1254)));
	}

	@Test
	public final void testRoundDecimalDecimal() {
		assertEquals(Decimal.valueOf(12.12).toDouble(),
		        NumberUtils.roundDecimal(Decimal.valueOf(12.1234)).toDouble(), 0.0001);
		assertEquals(Decimal.valueOf(12.13).toDouble(),
		        NumberUtils.roundDecimal(Decimal.valueOf(12.1254)).toDouble(), 0.0001);

	}

}
