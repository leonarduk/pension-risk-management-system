package com.leonarduk.finance.portfolio;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;

import jersey.repackaged.com.google.common.collect.Sets;

public class Position {
	final private BigDecimal		amount;

	final private Instrument		instrument;

	final private Set<String>		portfolios;
	final private Optional<Stock>	stock;
	private final String			symbol;

	public Position(final String portfolio, final Instrument instrument,
	        final BigDecimal amount, final Optional<Stock> stock2,
	        final String symbol) {
		this.portfolios = Sets.newHashSet(portfolio.split(":"));
		this.instrument = instrument;
		this.amount = amount;
		this.stock = stock2;
		this.symbol = symbol;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof Position)) {
			return false;
		}
		final Position castOther = (Position) other;
		return new EqualsBuilder().append(this.amount, castOther.amount)
		        .append(this.instrument, castOther.instrument)
		        .append(this.portfolios, castOther.portfolios)
		        .append(this.stock, castOther.stock)
		        .append(this.symbol, castOther.symbol).isEquals();
	}

	public BigDecimal getAmount() {
		return this.amount;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	public Set<String> getPortfolios() {
		return this.portfolios;
	}

	public Optional<Stock> getStock() {
		return this.stock;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.amount).append(this.instrument)
		        .append(this.portfolios).append(this.stock).append(this.symbol)
		        .toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("amount", this.amount)
		        .append("instrument", this.instrument)
		        .append("portfolios", this.portfolios)
		        .append("stock", this.stock).append("symbol", this.symbol)
		        .toString();
	}

}
