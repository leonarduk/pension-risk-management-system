package com.leonarduk.finance.analysis;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.ta4j.core.Order;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.Trade;
import org.ta4j.core.num.Num;


public class TraderOrderUtils {

	public static class OrderWithDate extends Order {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private final LocalDate	date;

		private final Num	price;

		public OrderWithDate(final Order order, final TimeSeries series) {
			super(order.getIndex(), order.getType(), order.getPrice(),
			        order.getAmount());
			final int index = order.getIndex();
			this.date = series.getBar(index).getEndTime().toLocalDate();
			this.price = series.getBar(index).getClosePrice();
		}

		public LocalDate getDate() {
			return this.date;
		}

		@Override
		public Num getPrice() {
			return this.price;
		}

		@Override
		public String toString() {
			return "OrderWithDate [date=" + this.date + " price=" + this.price
			        + " , " + super.toString() + "]";
		}

	}

	public static class TradeWithStrategy extends Trade {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
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
