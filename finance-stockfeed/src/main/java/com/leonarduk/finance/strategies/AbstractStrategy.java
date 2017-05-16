package com.leonarduk.finance.strategies;

import eu.verdelhan.ta4j.Strategy;

abstract public class AbstractStrategy {
	private final String	name;
	private final Strategy	strategy;

	public AbstractStrategy(final String name, final Strategy strategy) {
		this.name = name;
		this.strategy = strategy;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final AbstractStrategy other = (AbstractStrategy) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}

	public String getName() {
		return this.name;
	}

	public Strategy getStrategy() {
		return this.strategy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return this.getName();
	}

}
