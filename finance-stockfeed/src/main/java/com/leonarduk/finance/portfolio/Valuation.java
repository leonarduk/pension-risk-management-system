package com.leonarduk.finance.portfolio;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.google.common.collect.Maps;
import com.leonarduk.finance.strategies.AbstractStrategy;

import eu.verdelhan.ta4j.Decimal;

public class Valuation {
	private Decimal price;

	public Position getPosition() {
		return position;
	}

	public Decimal getValuation() {
		return valuation;
	}

	public Decimal getPrice() {
		return price;
	}

	public LocalDate getValuationDate() {
		return valuationDate;
	}

	public Recommendation getRecommendation(String name) {
		return recommendation.getOrDefault(name, new Recommendation(RecommendedTrade.HOLD, null, null));
	}

	private Position position;
	private Decimal valuation;
	LocalDate valuationDate;
	final Map<String, Recommendation> recommendation;
	private Map<Period, Decimal> returns;

	public Valuation(Position position, Decimal valuation, LocalDate valuationDate, Decimal price) {
		this.position = position;
		this.valuation = valuation;
		this.valuationDate = valuationDate;
		this.recommendation = Maps.newHashMap();
		this.returns = new HashMap<>();
		this.price = price;
	}

	public void addRecommendation(AbstractStrategy strategy, Recommendation recommendation2) {
		this.recommendation.put(strategy.getName(), recommendation2);
	}

	@Override
	public String toString() {
		return "Valuation [position=" + position + ", valuation=" + valuation + ", valuationDate=" + valuationDate
				+ ", recommendation=" + recommendation + ", returns=" + returns + "]";
	}

	public void addReturn(Period days, Decimal change) {
		this.returns.put(days, change);
	}

	public Decimal getReturn(Period days) {
		return this.returns.get(days);
	}

}
