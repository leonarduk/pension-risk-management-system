package com.leonarduk.finance.portfolio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.google.common.collect.Maps;
import com.leonarduk.finance.strategies.AbstractStrategy;

import eu.verdelhan.ta4j.Decimal;
import yahoofinance.Stock;

public class Valuation {
	private final Decimal price;

	private final Position position;

	private final Decimal valuation;

	LocalDate valuationDate;

	final Map<String, Recommendation> recommendation;

	private final Map<Period, Decimal> returns;

	public Valuation(final Position position, final Decimal valuation, final LocalDate valuationDate,
			final Decimal price) {
		this.position = position;
		final Optional<Stock> stock = this.position.getStock();
		if (stock.isPresent()) {
			try {
				stock.get().getHistory().clear();
			} catch (final IOException e) {
				// ignore
			}
		}
		this.valuation = valuation;
		this.valuationDate = valuationDate;
		this.recommendation = Maps.newHashMap();
		this.returns = new HashMap<>();
		this.price = price;
	}

	public void addRecommendation(final AbstractStrategy strategy, final Recommendation recommendation2) {
		this.recommendation.put(strategy.getName(), recommendation2);
	}

	public void addReturn(final Period days, final Decimal change) {
		this.returns.put(days, change);
	}

	public Position getPosition() {
		return this.position;
	}

	public Decimal getPrice() {
		return this.price;
	}

	public Recommendation getRecommendation(final String name) {
		return this.recommendation.getOrDefault(name, new Recommendation(RecommendedTrade.HOLD, null, null));
	}

	public Decimal getReturn(final Period days) {
		return this.returns.get(days);
	}

	public Decimal getValuation() {
		return this.valuation;
	}

	public LocalDate getValuationDate() {
		return this.valuationDate;
	}

	@Override
	public String toString() {
		return "Valuation [position=" + this.position + ", valuation=" + this.valuation + ", valuationDate="
				+ this.valuationDate + ", recommendation=" + this.recommendation + ", returns=" + this.returns + "]";
	}

}
