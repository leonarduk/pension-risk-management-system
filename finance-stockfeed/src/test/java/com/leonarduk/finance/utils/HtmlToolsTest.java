package com.leonarduk.finance.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;

import eu.verdelhan.ta4j.Decimal;
import jersey.repackaged.com.google.common.collect.Maps;

public class HtmlToolsTest {

	@Test
	public final void testAddField() {
		final StringBuilder sb = new StringBuilder();
		final ValueFormatter formatter = (Object::toString);
		HtmlTools.addField("testvalue", sb, formatter);
		assertEquals("<td bgcolor='white'>testvalue</td>", sb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddFieldWithNulls() {
		final StringBuilder sb = null;
		final ValueFormatter formatter = null;
		HtmlTools.addField(null, sb, formatter);
	}

	@Test
	public final void testAddFieldWithNullValue() {
		final StringBuilder sb = new StringBuilder();
		final ValueFormatter formatter = null;
		HtmlTools.addField(null, sb, formatter);
		assertEquals("<td bgcolor='white'></td>", sb.toString());
	}

	@Test
	public final void testAddHeader() {
		final StringBuilder sb = new StringBuilder();
		HtmlTools.addHeader("test", sb);
		assertEquals("<th>test</th>", sb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public final void testAddHeaderWithNulls() {
		final StringBuilder sb = null;
		HtmlTools.addHeader(null, sb);
	}

	@Test
	public final void testAddHeaderWithNullValue() {
		final StringBuilder sb = new StringBuilder();
		HtmlTools.addHeader(null, sb);
		assertEquals("<th></th>", sb.toString());
	}

	// TODO
	@Test
	public final void testAddPieChartAndTable() {
		// HtmlTools.addPieChartAndTable(assetTypeMap, sbBody, valuations,
		// title, key, value);
	}

	@Test
	public final void testCreateHtmlText() {
		final StringBuilder sbHead = new StringBuilder("head");
		final StringBuilder sbBody = new StringBuilder("body");
		final StringBuilder actual = HtmlTools.createHtmlText(sbHead, sbBody);
		assertEquals("<html><head>head</head><body>body</body></html>\n", actual.toString());
	}

	@Test
	public final void testGetColour() {
		assertEquals("red", HtmlTools.getColour(Decimal.valueOf(-123)));
	}

	@Test
	public final void testGetColourWithNull() {
		assertEquals("white", HtmlTools.getColour(null));
	}

	@Test
	public final void testGetURLParameters() {
		final Map<String, String> params = Maps.newHashMap();
		assertEquals("", HtmlTools.getURLParameters(params));
	}

	@Test
	public final void testPrintTable() {
		final StringBuilder sb = new StringBuilder();
		final List<List<DataField>> records = Lists.newLinkedList();
		HtmlTools.printTable(sb, records);
		assertEquals("", sb.toString());
	}

}
