package com.leonarduk.finance.analysis;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.jfree.chart.axis.Tick;
import org.junit.Test;
import org.ta4j.core.Order;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;

import jersey.repackaged.com.google.common.collect.Lists;

public class TraderOrderUtilsTest {

//	@Test
//	public void testGetOrdersList() {
//		List<Trade> trades = Lists.newLinkedList();
//		trades.add(new Trade(Order.buyAt(0), Order.sellAt(1)));
//		TimeSeries series = new TimeSeries(
//				Lists.newArrayList(new Tick(DateTime.parse("2017-04-01"), 100, 103, 98, 101, 2),
//						new Tick(DateTime.parse("2017-04-02"), 101, 105, 100, 104, 1)));
//		List<Trade> orders = TraderOrderUtils.getOrdersList(trades, series, mock(Strategy.class), "test");
//		assertEquals(1, orders.size());
//	}

}
