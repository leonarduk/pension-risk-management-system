package com.leonarduk.finance.utils;

import org.junit.Test;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

    @Test
    public void testAddValueNull() {
        StringBuilder sb = new StringBuilder();
        StringUtils.addValue(sb, (String) null);
        assertEquals(",", sb.toString());

        sb = new StringBuilder();
        StringUtils.addValue(sb, (Double) null);
        assertEquals(",", sb.toString());

        sb = new StringBuilder();
        StringUtils.addValue(sb, (BigDecimal) null);
        assertEquals(",", sb.toString());

        sb = new StringBuilder();
        StringUtils.addValue(sb, (Num) null);
        assertEquals(",", sb.toString());
    }

    @Test
    public void testAddValueWithComma() {
        StringBuilder sb = new StringBuilder("prefix");
        StringUtils.addValue(sb, "a,b");
        assertEquals("prefix,\"a,b\"", sb.toString());
    }

    @Test
    public void testAddValueWithQuote() {
        StringBuilder sb = new StringBuilder("prefix");
        StringUtils.addValue(sb, "a\"b");
        assertEquals("prefix,\"a\"\"b\"", sb.toString());
    }

    @Test
    public void testAddValueWithNewline() {
        StringBuilder sb = new StringBuilder("prefix");
        StringUtils.addValue(sb, "a\nb");
        assertEquals("prefix,\"a\nb\"", sb.toString());
    }
}
