
package yahoofinance.quotes.stock;

import java.math.BigDecimal;

import com.leonarduk.finance.utils.NumberUtils;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Stijn Strickx
 */
public class StockStats {

	private BigDecimal		bookValuePerShare;

	private BigDecimal		EBITDA;										 // ttm

	private BigDecimal		eps;

	private BigDecimal		epsEstimateCurrentYear;
	private BigDecimal		epsEstimateNextQuarter;
	private BigDecimal		epsEstimateNextYear;
	private BigDecimal		marketCap;

	private BigDecimal		oneYearTargetPrice;
	private BigDecimal		pe;
	private BigDecimal		peg;

	private BigDecimal		priceBook;
	private BigDecimal		priceSales;
	private BigDecimal		revenue;								 // ttm

	private Long			sharesFloat;
	private Long			sharesOutstanding;
	private Long			sharesOwned;

	private BigDecimal		shortRatio;
	private final String	symbol;

	public StockStats(final String symbol) {
		this.symbol = symbol;
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
		final StockStats other = (StockStats) obj;
		if (this.EBITDA == null) {
			if (other.EBITDA != null) {
				return false;
			}
		}
		else if (!this.EBITDA.equals(other.EBITDA)) {
			return false;
		}
		if (this.bookValuePerShare == null) {
			if (other.bookValuePerShare != null) {
				return false;
			}
		}
		else if (!this.bookValuePerShare.equals(other.bookValuePerShare)) {
			return false;
		}
		if (this.eps == null) {
			if (other.eps != null) {
				return false;
			}
		}
		else if (!this.eps.equals(other.eps)) {
			return false;
		}
		if (this.epsEstimateCurrentYear == null) {
			if (other.epsEstimateCurrentYear != null) {
				return false;
			}
		}
		else if (!this.epsEstimateCurrentYear.equals(other.epsEstimateCurrentYear)) {
			return false;
		}
		if (this.epsEstimateNextQuarter == null) {
			if (other.epsEstimateNextQuarter != null) {
				return false;
			}
		}
		else if (!this.epsEstimateNextQuarter.equals(other.epsEstimateNextQuarter)) {
			return false;
		}
		if (this.epsEstimateNextYear == null) {
			if (other.epsEstimateNextYear != null) {
				return false;
			}
		}
		else if (!this.epsEstimateNextYear.equals(other.epsEstimateNextYear)) {
			return false;
		}
		if (this.marketCap == null) {
			if (other.marketCap != null) {
				return false;
			}
		}
		else if (!this.marketCap.equals(other.marketCap)) {
			return false;
		}
		if (this.oneYearTargetPrice == null) {
			if (other.oneYearTargetPrice != null) {
				return false;
			}
		}
		else if (!this.oneYearTargetPrice.equals(other.oneYearTargetPrice)) {
			return false;
		}
		if (this.pe == null) {
			if (other.pe != null) {
				return false;
			}
		}
		else if (!this.pe.equals(other.pe)) {
			return false;
		}
		if (this.peg == null) {
			if (other.peg != null) {
				return false;
			}
		}
		else if (!this.peg.equals(other.peg)) {
			return false;
		}
		if (this.priceBook == null) {
			if (other.priceBook != null) {
				return false;
			}
		}
		else if (!this.priceBook.equals(other.priceBook)) {
			return false;
		}
		if (this.priceSales == null) {
			if (other.priceSales != null) {
				return false;
			}
		}
		else if (!this.priceSales.equals(other.priceSales)) {
			return false;
		}
		if (this.revenue == null) {
			if (other.revenue != null) {
				return false;
			}
		}
		else if (!this.revenue.equals(other.revenue)) {
			return false;
		}
		if (this.sharesFloat == null) {
			if (other.sharesFloat != null) {
				return false;
			}
		}
		else if (!this.sharesFloat.equals(other.sharesFloat)) {
			return false;
		}
		if (this.sharesOutstanding == null) {
			if (other.sharesOutstanding != null) {
				return false;
			}
		}
		else if (!this.sharesOutstanding.equals(other.sharesOutstanding)) {
			return false;
		}
		if (this.sharesOwned == null) {
			if (other.sharesOwned != null) {
				return false;
			}
		}
		else if (!this.sharesOwned.equals(other.sharesOwned)) {
			return false;
		}
		if (this.shortRatio == null) {
			if (other.shortRatio != null) {
				return false;
			}
		}
		else if (!this.shortRatio.equals(other.shortRatio)) {
			return false;
		}
		if (this.symbol == null) {
			if (other.symbol != null) {
				return false;
			}
		}
		else if (!this.symbol.equals(other.symbol)) {
			return false;
		}
		return true;
	}

	public BigDecimal getBookValuePerShare() {
		return this.bookValuePerShare;
	}

	public BigDecimal getEBITDA() {
		return this.EBITDA;
	}

	public BigDecimal getEps() {
		return this.eps;
	}

	public BigDecimal getEpsEstimateCurrentYear() {
		return this.epsEstimateCurrentYear;
	}

	public BigDecimal getEpsEstimateNextQuarter() {
		return this.epsEstimateNextQuarter;
	}

	public BigDecimal getEpsEstimateNextYear() {
		return this.epsEstimateNextYear;
	}

	public BigDecimal getMarketCap() {
		return this.marketCap;
	}

	public BigDecimal getOneYearTargetPrice() {
		return this.oneYearTargetPrice;
	}

	public BigDecimal getPe() {
		return this.pe;
	}

	public BigDecimal getPeg() {
		return this.peg;
	}

	public BigDecimal getPriceBook() {
		return this.priceBook;
	}

	public BigDecimal getPriceSales() {
		return this.priceSales;
	}

	public BigDecimal getRevenue() {
		return this.revenue;
	}

	public BigDecimal getROE() {
		return NumberUtils.getPercent(this.EBITDA, this.marketCap);
	}

	public Long getSharesFloat() {
		return this.sharesFloat;
	}

	public Long getSharesOutstanding() {
		return this.sharesOutstanding;
	}

	public Long getSharesOwned() {
		return this.sharesOwned;
	}

	public BigDecimal getShortRatio() {
		return this.shortRatio;
	}

	public String getSymbol() {
		return this.symbol;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.EBITDA == null) ? 0 : this.EBITDA.hashCode());
		result = (prime * result)
		        + ((this.bookValuePerShare == null) ? 0 : this.bookValuePerShare.hashCode());
		result = (prime * result) + ((this.eps == null) ? 0 : this.eps.hashCode());
		result = (prime * result) + ((this.epsEstimateCurrentYear == null) ? 0
		        : this.epsEstimateCurrentYear.hashCode());
		result = (prime * result) + ((this.epsEstimateNextQuarter == null) ? 0
		        : this.epsEstimateNextQuarter.hashCode());
		result = (prime * result)
		        + ((this.epsEstimateNextYear == null) ? 0 : this.epsEstimateNextYear.hashCode());
		result = (prime * result) + ((this.marketCap == null) ? 0 : this.marketCap.hashCode());
		result = (prime * result)
		        + ((this.oneYearTargetPrice == null) ? 0 : this.oneYearTargetPrice.hashCode());
		result = (prime * result) + ((this.pe == null) ? 0 : this.pe.hashCode());
		result = (prime * result) + ((this.peg == null) ? 0 : this.peg.hashCode());
		result = (prime * result) + ((this.priceBook == null) ? 0 : this.priceBook.hashCode());
		result = (prime * result) + ((this.priceSales == null) ? 0 : this.priceSales.hashCode());
		result = (prime * result) + ((this.revenue == null) ? 0 : this.revenue.hashCode());
		result = (prime * result) + ((this.sharesFloat == null) ? 0 : this.sharesFloat.hashCode());
		result = (prime * result)
		        + ((this.sharesOutstanding == null) ? 0 : this.sharesOutstanding.hashCode());
		result = (prime * result) + ((this.sharesOwned == null) ? 0 : this.sharesOwned.hashCode());
		result = (prime * result) + ((this.shortRatio == null) ? 0 : this.shortRatio.hashCode());
		result = (prime * result) + ((this.symbol == null) ? 0 : this.symbol.hashCode());
		return result;
	}

	public void setBookValuePerShare(final BigDecimal bookValuePerShare) {
		this.bookValuePerShare = bookValuePerShare;
	}

	public void setEBITDA(final BigDecimal EBITDA) {
		this.EBITDA = EBITDA;
	}

	public void setEps(final BigDecimal eps) {
		this.eps = eps;
	}

	public void setEpsEstimateCurrentYear(final BigDecimal epsEstimateCurrentYear) {
		this.epsEstimateCurrentYear = epsEstimateCurrentYear;
	}

	public void setEpsEstimateNextQuarter(final BigDecimal epsEstimateNextQuarter) {
		this.epsEstimateNextQuarter = epsEstimateNextQuarter;
	}

	public void setEpsEstimateNextYear(final BigDecimal epsEstimateNextYear) {
		this.epsEstimateNextYear = epsEstimateNextYear;
	}

	public void setMarketCap(final BigDecimal marketCap) {
		this.marketCap = marketCap;
	}

	public void setOneYearTargetPrice(final BigDecimal oneYearTargetPrice) {
		this.oneYearTargetPrice = oneYearTargetPrice;
	}

	public void setPe(final BigDecimal pe) {
		this.pe = pe;
	}

	public void setPeg(final BigDecimal peg) {
		this.peg = peg;
	}

	public void setPriceBook(final BigDecimal priceBook) {
		this.priceBook = priceBook;
	}

	public void setPriceSales(final BigDecimal priceSales) {
		this.priceSales = priceSales;
	}

	public void setRevenue(final BigDecimal revenue) {
		this.revenue = revenue;
	}

	public void setSharesFloat(final Long sharesFloat) {
		this.sharesFloat = sharesFloat;
	}

	public void setSharesOutstanding(final Long sharesOutstanding) {
		this.sharesOutstanding = sharesOutstanding;
	}

	public void setSharesOwned(final Long sharesOwned) {
		this.sharesOwned = sharesOwned;
	}

	public void setShortRatio(final BigDecimal shortRatio) {
		this.shortRatio = shortRatio;
	}

	@Override
	public String toString() {
		return "EPS: " + this.eps + ", PE: " + this.pe + ", PEG: " + this.peg;
	}

}
