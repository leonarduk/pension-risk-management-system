package com.leonarduk.finance.utils;

import java.math.BigDecimal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NumberUtilsTest {

    @Test
    public void testGetBigDecimalParsesSuffixes() {
        Assertions.assertEquals(new BigDecimal("1000"), NumberUtils.getBigDecimal("1K"));
        Assertions.assertEquals(
                0,
                NumberUtils.getBigDecimal("2.5M")
                        .compareTo(new BigDecimal("2500000")));
        Assertions.assertEquals(
                0,
                NumberUtils.getBigDecimal("3B")
                        .compareTo(new BigDecimal("3000000000")));
    }

    @Test
    public void testGetBigDecimalHandlesPlainAndInvalid() {
        Assertions.assertEquals(new BigDecimal("123"), NumberUtils.getBigDecimal("123"));
        Assertions.assertNull(NumberUtils.getBigDecimal("bad"));
    }

    @Test
    public void testGetPercentAndRoundDecimal() {
        BigDecimal numerator = new BigDecimal("50");
        BigDecimal denominator = new BigDecimal("200");
        Assertions.assertEquals(new BigDecimal("25.00"), NumberUtils.getPercent(numerator, denominator));
        Assertions.assertEquals(BigDecimal.ZERO, NumberUtils.getPercent(numerator, BigDecimal.ZERO));
        Assertions.assertEquals(new BigDecimal("1.23"), NumberUtils.roundDecimal(new BigDecimal("1.234")));
        Assertions.assertEquals(1.23, NumberUtils.roundDecimal(1.234), 0.0001);
    }
}
