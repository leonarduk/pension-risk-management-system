package com.leonarduk.stockmarketview.chart;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;

public class TraderOrderUtils {

	public static class OrderWithDate extends Order {
		public OrderWithDate(Order order, TimeSeries series) {
			super(order.getIndex(), order.getType(), order.getPrice(), order.getAmount());
			int index = order.getIndex();
			this.date = series.getTick(index).getEndTime().toLocalDate();
			this.price = series.getTick(index).getClosePrice();
		}

		private LocalDate date;
		private Decimal price;

		@Override
		public String toString() {
			return "OrderWithDate [date=" + date + " price=" + price + " , " + super.toString() + "]";
		}

		public LocalDate getDate() {
			return date;
		}

		public Decimal getPrice() {
			return price;
		}

	}

	public static List<Trade> getOrdersList(List<Trade> trades, TimeSeries series) {
		List<Trade> orders = new LinkedList<>();
		for (Trade trade : trades) {
			Trade newTrade = new Trade(new OrderWithDate(trade.getEntry(), series),
					new OrderWithDate(trade.getExit(), series));
			orders.add(newTrade);
		}
		return orders;
	}
}
