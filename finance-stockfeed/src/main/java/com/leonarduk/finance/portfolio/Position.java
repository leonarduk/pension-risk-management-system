package com.leonarduk.finance.portfolio;

import java.util.Optional;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Stock;

import eu.verdelhan.ta4j.Decimal;

public class Position {
	public String getSymbol() {
		return symbol;
	}

	@Override
	public String toString() {
		String name = "";
		if (stock.isPresent()) {
			name = stock.get().toString();
		}
		return "Position [instrument=" + instrument + ", stock=" + name + ", amount=" + amount + ", portfolio="
				+ portfolio + " symbol=" + symbol + "]";
	}

	final private Instrument instrument;
	final private Optional<Stock> stock;
	final private Decimal amount;
	final private String portfolio;
	private String symbol;

	public Position(String portfolio, Instrument instrument, Decimal amount, Optional<Stock> stock2, String symbol) {
		this.portfolio = portfolio;
		this.instrument = instrument;
		this.amount = amount;
		this.stock = stock2;
		this.symbol = symbol;
	}

	public Optional<Stock> getStock() {
		return stock;
	}

	public Instrument getInstrument() {
		return instrument;
	}

	public String getPortfolio() {
		return portfolio;
	}

	public Decimal getAmount() {
		return amount;
	}

}
