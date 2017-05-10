package com.leonarduk.finance.portfolio;

import java.util.Optional;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;

import eu.verdelhan.ta4j.Decimal;

public class Position {
	final private Decimal			amount;

	final private Instrument		instrument;

	final private String			portfolio;
	final private Optional<Stock>	stock;
	private final String			symbol;

	public Position(final String portfolio, final Instrument instrument, final Decimal amount,
	        final Optional<Stock> stock2, final String symbol) {
		this.portfolio = portfolio;
		this.instrument = instrument;
		this.amount = amount;
		this.stock = stock2;
		this.symbol = symbol;
	}

	public Decimal getAmount() {
		return this.amount;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	public String getPortfolio() {
		return this.portfolio;
	}

	public Optional<Stock> getStock() {
		return this.stock;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public String toString() {
		String name = "";
		if (this.stock.isPresent()) {
			name = this.stock.get().toString();
		}
		return "Position [instrument=" + this.instrument + ", stock=" + name + ", amount="
		        + this.amount + ", portfolio=" + this.portfolio + " symbol=" + this.symbol + "]";
	}

}
