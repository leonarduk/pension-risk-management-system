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

	public static class OrderWithDate extends Order {
		private final LocalDate	date;

		private final Decimal	price;

		public OrderWithDate(final Order order, final TimeSeries series) {
			super(order.getIndex(), order.getType(), order.getPrice(),
			        order.getAmount());
			final int index = order.getIndex();
			this.date = series.getTick(index).getEndTime().toLocalDate();
			this.price = series.getTick(index).getClosePrice();
		}

		public LocalDate getDate() {
			return this.date;
		}

		@Override
		public Decimal getPrice() {
			return this.price;
		}

		@Override
		public String toString() {
			return "OrderWithDate [date=" + this.date + " price=" + this.price
			        + " , " + super.toString() + "]";
		}

	}

	public static class TradeWithStrategy extends Trade {
		private final Strategy	strategy;
		private final String	strategyName;

		public TradeWithStrategy(final Order entry, final Order exit,
		        final Strategy strategy, final String strategyName) {
			super(entry, exit);
			this.strategy = strategy;
			this.strategyName = strategyName;
		}

		@Override
		public String toString() {
			return "TradeWithStrategy [strategy=" + this.strategy
			        + ", strategyName=" + this.strategyName + ", "
			        + super.toString() + "]";
		}
	}

	public static List<Trade> getOrdersList(final List<Trade> trades,
	        final TimeSeries series, final Strategy strategy,
	        final String strategyName) {
		final List<Trade> orders = new LinkedList<>();
		for (final Trade trade : trades) {
			final Trade newTrade = new TradeWithStrategy(
			        new OrderWithDate(trade.getEntry(), series),
			        new OrderWithDate(trade.getExit(), series), strategy,
			        strategyName);
			orders.add(newTrade);
		}
		return orders;
	}
}
