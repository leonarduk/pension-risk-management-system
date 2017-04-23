package com.leonarduk.finance.analysis;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.LocalDate;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Order;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;

public class TraderOrderUtils {

	public static class TradeWithStrategy extends Trade {
		private Strategy strategy;
		private String strategyName;

		public TradeWithStrategy(Order entry, Order exit, Strategy strategy, String strategyName) {
			super(entry, exit);
			this.strategy = strategy;
			this.strategyName = strategyName;
		}

		@Override
		public String toString() {
			return "TradeWithStrategy [strategy=" + strategy + ", strategyName=" + strategyName + ", "
					+ super.toString() + "]";
		}
	}

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

	public static List<Trade> getOrdersList(List<Trade> trades, TimeSeries series, Strategy strategy, String  strategyName) {
		List<Trade> orders = new LinkedList<>();
		for (Trade trade : trades) {
			Trade newTrade = new TradeWithStrategy(new OrderWithDate(trade.getEntry(), series),
					new OrderWithDate(trade.getExit(), series), strategy, strategyName);
			orders.add(newTrade);
		}
		return orders;
	}
}
