package com.leonarduk.finance.portfolio;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.strategies.AbstractStrategy;

public class Recommendation {


	private RecommendedTrade tradeRecommendation;
	private AbstractStrategy strategy;
	private Instrument instrument;

	public Recommendation(RecommendedTrade tradeRecommendation, AbstractStrategy strategy, Instrument stock2) {
		this.tradeRecommendation = tradeRecommendation;
		this.strategy = strategy;
		this.instrument = stock2;
	}

	public RecommendedTrade getTradeRecommendation() {
		return tradeRecommendation;
	}

	public AbstractStrategy getStrategy() {
		return strategy;
	}

	public Instrument getSymbol() {
		return instrument;
	}

	@Override
	public String toString() {
		return "Recommendation [tradeRecommendation=" + tradeRecommendation + ", strategy=" + strategy + ", symbol="
				+ instrument + "]";
	}

}