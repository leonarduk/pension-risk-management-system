package com.leonarduk.stockmarketview.strategies;

import eu.verdelhan.ta4j.Strategy;

abstract public class AbstractStrategy {
	private final String name;
	private final Strategy strategy;

	public AbstractStrategy(String name, Strategy strategy) {
		this.name = name;
		this.strategy = strategy;
	}

	public String getName() {
		return name;
	}

	public Strategy getStrategy() {
		return strategy;
	}
}
