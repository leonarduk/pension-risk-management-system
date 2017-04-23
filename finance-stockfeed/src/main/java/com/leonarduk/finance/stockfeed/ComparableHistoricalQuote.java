package com.leonarduk.finance.stockfeed;

import java.math.BigDecimal;
import java.util.Calendar;

import com.google.common.base.Objects;

import yahoofinance.histquotes.HistoricalQuote;

public class ComparableHistoricalQuote extends HistoricalQuote {

	public ComparableHistoricalQuote(String symbol, Calendar date, BigDecimal open, BigDecimal low, BigDecimal high,
			BigDecimal close, BigDecimal adjClose, Long volume) {
		super(symbol, date, open, low, high, close, adjClose, volume);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.getClose(), this.getHigh(), this.getDate(), this.getLow(), this.getOpen(),
				this.getSymbol(), this.getVolume());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ComparableHistoricalQuote other = (ComparableHistoricalQuote) obj;
		return (this.getClose().compareTo(other.getClose()) == 0) && (this.getHigh().compareTo(other.getHigh()) == 0)
				&& (this.getLow().compareTo(other.getLow()) == 0) && Objects.equal(this.getDate(), other.getDate())
				&& (this.getSymbol().compareTo(other.getSymbol()) == 0)
				&& Objects.equal(this.getVolume(), other.getVolume())
				&& (this.getOpen().compareTo(other.getOpen()) == 0);
	}
}
