
package yahoofinance.quotes.stock;

import java.math.BigDecimal;
import java.util.Calendar;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Stijn Strickx
 */
public class StockDividend {

	private BigDecimal		annualYield;

	private BigDecimal		annualYieldPercent;

	private Calendar		exDate;

	private Calendar		payDate;
	private final String	symbol;

	public StockDividend(final String symbol) {
		this.symbol = symbol;
	}

	public StockDividend(final String symbol, final Calendar payDate, final Calendar exDate,
	        final BigDecimal annualYield, final BigDecimal annualYieldPercent) {
		this(symbol);
		this.payDate = payDate;
		this.exDate = exDate;
		this.annualYield = annualYield;
		this.annualYieldPercent = annualYieldPercent;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StockDividend)) {
			return false;
		}
		final StockDividend castOther = (StockDividend) other;
		return new EqualsBuilder().append(this.annualYield, castOther.annualYield)
		        .append(this.annualYieldPercent, castOther.annualYieldPercent)
		        .append(this.exDate, castOther.exDate).append(this.payDate, castOther.payDate)
		        .append(this.symbol, castOther.symbol).isEquals();
	}

	public BigDecimal getAnnualYield() {
		return this.annualYield;
	}

	public BigDecimal getAnnualYieldPercent() {
		return this.annualYieldPercent;
	}

	public Calendar getExDate() {
		return this.exDate;
	}

	public Calendar getPayDate() {
		return this.payDate;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.annualYield).append(this.annualYieldPercent)
		        .append(this.exDate).append(this.payDate).append(this.symbol).toHashCode();
	}

	public void setAnnualYield(final BigDecimal annualYield) {
		this.annualYield = annualYield;
	}

	public void setAnnualYieldPercent(final BigDecimal annualYieldPercent) {
		this.annualYieldPercent = annualYieldPercent;
	}

	public void setExDate(final Calendar exDate) {
		this.exDate = exDate;
	}

	public void setPayDate(final Calendar payDate) {
		this.payDate = payDate;
	}

	@Override
	public String toString() {
		String payDateStr = "/";
		String exDateStr = "/";
		String annualYieldStr = "/";
		if (this.payDate != null) {
			payDateStr = this.payDate.getTime().toString();
		}
		if (this.exDate != null) {
			exDateStr = this.exDate.getTime().toString();
		}
		if (this.annualYieldPercent != null) {
			annualYieldStr = this.annualYieldPercent.toString() + "%";
		}
		return "Pay date: " + payDateStr + ", Ex date: " + exDateStr + ", Annual yield: "
		        + annualYieldStr;
	}

}
