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

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractStrategy other = (AbstractStrategy) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
