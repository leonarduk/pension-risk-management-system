package com.leonarduk.finance.portfolio;

import java.util.List;

import org.joda.time.LocalDate;

import com.google.common.collect.ImmutableList;

public class ValuationReport {
	private final String			fromDate;
	private final String			toDate;
	private final List<Valuation>	valuations;

	public ValuationReport(final List<Valuation> valuations, final LocalDate fromDate,
	        final LocalDate toDate) {
		this.valuations = ImmutableList.copyOf(valuations);
		this.fromDate = fromDate.toString();
		this.toDate = toDate.toString();
	}

	public String getFromDate() {
		return this.fromDate;
	}

	public String getToDate() {
		return this.toDate;
	}

	public List<Valuation> getValuations() {
		return this.valuations;
	}

}
