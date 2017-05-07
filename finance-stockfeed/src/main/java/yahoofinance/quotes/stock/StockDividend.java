
package yahoofinance.quotes.stock;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * All getters can return null in case the data is not available from Yahoo
 * Finance.
 *
 * @author Stijn Strickx
 */
public class StockDividend {

	private final String symbol;

	private Calendar payDate;

	private Calendar exDate;

	private BigDecimal annualYield;
	private BigDecimal annualYieldPercent;

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
		final StockDividend other = (StockDividend) obj;
		if (this.annualYield == null) {
			if (other.annualYield != null) {
				return false;
			}
		} else if (!this.annualYield.equals(other.annualYield)) {
			return false;
		}
		if (this.annualYieldPercent == null) {
			if (other.annualYieldPercent != null) {
				return false;
			}
		} else if (!this.annualYieldPercent.equals(other.annualYieldPercent)) {
			return false;
		}
		if (this.exDate == null) {
			if (other.exDate != null) {
				return false;
			}
		} else if (!this.exDate.equals(other.exDate)) {
			return false;
		}
		if (this.payDate == null) {
			if (other.payDate != null) {
				return false;
			}
		} else if (!this.payDate.equals(other.payDate)) {
			return false;
		}
		if (this.symbol == null) {
			if (other.symbol != null) {
				return false;
			}
		} else if (!this.symbol.equals(other.symbol)) {
			return false;
		}
		return true;
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
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.annualYield == null) ? 0 : this.annualYield.hashCode());
		result = (prime * result) + ((this.annualYieldPercent == null) ? 0 : this.annualYieldPercent.hashCode());
		result = (prime * result) + ((this.exDate == null) ? 0 : this.exDate.hashCode());
		result = (prime * result) + ((this.payDate == null) ? 0 : this.payDate.hashCode());
		result = (prime * result) + ((this.symbol == null) ? 0 : this.symbol.hashCode());
		return result;
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
		return "Pay date: " + payDateStr + ", Ex date: " + exDateStr + ", Annual yield: " + annualYieldStr;
	}

}
