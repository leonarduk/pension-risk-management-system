package com.leonarduk.finance.portfolio;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import com.google.common.collect.Maps;
import com.leonarduk.finance.stockfeed.Stock;
// import com.leonarduk.finance.strategies.AbstractStrategy;

public class Valuation {
	private final Position					position;

	private final BigDecimal				price;

	private final Map<String, String>		recommendation;

	private final Map<Period, BigDecimal>	returns;

	private final BigDecimal				valuation;

	private final String					valuationDate;

	public Valuation(final Position position, final BigDecimal valuation,
	        final LocalDate valuationDate, final BigDecimal price) {
		this.position = position;
		final Optional<Stock> stock = this.position.getStock();
		if (stock.isPresent()) {
			try {
				stock.get().getHistory().clear();
			}
			catch (final IOException e) {
				// ignore
			}
		}
		this.valuation = valuation;
		this.valuationDate = valuationDate.toString();
		this.recommendation = Maps.newHashMap();
		this.returns = new HashMap<>();
		this.price = price;
	}

	public void addRecommendation(final String strategyName, final Recommendation recommendation2) {
		this.recommendation.put(strategyName.replaceAll(" ", "_").replaceAll("\\p{P}", ""),
		        recommendation2.getTradeRecommendation().name());
	}

	public void addReturn(final Period days, final BigDecimal change) {
		this.returns.put(days, change);
	}

	public Position getPosition() {
		return this.position;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public Map<String, String> getRecommendation() {
		return this.recommendation;
	}

	public String getRecommendation(final String name) {
		return this.recommendation.getOrDefault(name, RecommendedTrade.HOLD.name());
	}

	public BigDecimal getReturn(final Period days) {
		return this.returns.get(days);
	}

	public Map<Period, BigDecimal> getReturns() {
		return this.returns;
	}

	public BigDecimal getValuation() {
		return this.valuation;
	}

	public String getValuationDate() {
		return this.valuationDate.toString();
	}

	public void setValuation(final Map<Period, BigDecimal> returns2) {
		this.returns.clear();
		this.returns.putAll(returns2);
	}

	@Override
	public String toString() {
		return "Valuation [position=" + this.position + ", valuation=" + this.valuation
		        + ", valuationDate=" + this.valuationDate + ", recommendation="
		        + this.recommendation + ", returns=" + this.returns + "]";
	}

}
