package com.leonarduk.finance.portfolio;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.strategies.AbstractStrategy;

public class Recommendation {

	private final Instrument		instrument;
	private final AbstractStrategy	strategy;
	private final RecommendedTrade	tradeRecommendation;

	public Recommendation(final RecommendedTrade tradeRecommendation,
	        final AbstractStrategy strategy, final Instrument stock2) {
		this.tradeRecommendation = tradeRecommendation;
		this.strategy = strategy;
		this.instrument = stock2;
	}

	public AbstractStrategy getStrategy() {
		return this.strategy;
	}

	public Instrument getSymbol() {
		return this.instrument;
	}

	public RecommendedTrade getTradeRecommendation() {
		return this.tradeRecommendation;
	}

	@Override
	public String toString() {
		return "Recommendation [tradeRecommendation=" + this.tradeRecommendation
		        + ", strategy=" + this.strategy + ", symbol=" + this.instrument
		        + "]";
	}

}
