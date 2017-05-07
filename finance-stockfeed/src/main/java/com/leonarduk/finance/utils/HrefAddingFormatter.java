package com.leonarduk.finance.utils;

public class HrefAddingFormatter implements ValueFormatter {
	String ticker;
	int years;

	public HrefAddingFormatter(final String ticker, final int years) {
		this.years = years;
		this.ticker = ticker;
	}

	@Override
	public String format(final Object value) {
		return new StringBuilder("<a href=\"/stock/display?ticker=").append(this.ticker).append("&years=")
				.append(this.years).append("\">").append(value).append("</a>").toString();
	}

}
