
package yahoofinance.quotes.stock;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.leonarduk.finance.utils.NumberUtils;

/**
 * All getters can return null in case the data is not available from Yahoo Finance.
 *
 * @author Stijn Strickx
 */
public class StockStats {

	private BigDecimal		bookValuePerShare;

	private BigDecimal		EBITDA;																				 // ttm

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
	private BigDecimal		revenue;																 // ttm

	private Long			sharesFloat;
	private Long			sharesOutstanding;
	private Long			sharesOwned;

	private BigDecimal		shortRatio;
	private final String	symbol;

	public StockStats(final String symbol) {
		this.symbol = symbol;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof StockStats)) {
			return false;
		}
		final StockStats castOther = (StockStats) other;
		return new EqualsBuilder()
		        .append(this.bookValuePerShare, castOther.bookValuePerShare)
		        .append(this.EBITDA, castOther.EBITDA)
		        .append(this.eps, castOther.eps)
		        .append(this.epsEstimateCurrentYear,
		                castOther.epsEstimateCurrentYear)
		        .append(this.epsEstimateNextQuarter,
		                castOther.epsEstimateNextQuarter)
		        .append(this.epsEstimateNextYear, castOther.epsEstimateNextYear)
		        .append(this.marketCap, castOther.marketCap)
		        .append(this.oneYearTargetPrice, castOther.oneYearTargetPrice)
		        .append(this.pe, castOther.pe).append(this.peg, castOther.peg)
		        .append(this.priceBook, castOther.priceBook)
		        .append(this.priceSales, castOther.priceSales)
		        .append(this.revenue, castOther.revenue)
		        .append(this.sharesFloat, castOther.sharesFloat)
		        .append(this.sharesOutstanding, castOther.sharesOutstanding)
		        .append(this.sharesOwned, castOther.sharesOwned)
		        .append(this.shortRatio, castOther.shortRatio)
		        .append(this.symbol, castOther.symbol).isEquals();
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
		return new HashCodeBuilder().append(this.bookValuePerShare)
		        .append(this.EBITDA).append(this.eps)
		        .append(this.epsEstimateCurrentYear)
		        .append(this.epsEstimateNextQuarter)
		        .append(this.epsEstimateNextYear).append(this.marketCap)
		        .append(this.oneYearTargetPrice).append(this.pe)
		        .append(this.peg).append(this.priceBook).append(this.priceSales)
		        .append(this.revenue).append(this.sharesFloat)
		        .append(this.sharesOutstanding).append(this.sharesOwned)
		        .append(this.shortRatio).append(this.symbol).toHashCode();
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

	public void setEpsEstimateCurrentYear(
	        final BigDecimal epsEstimateCurrentYear) {
		this.epsEstimateCurrentYear = epsEstimateCurrentYear;
	}

	public void setEpsEstimateNextQuarter(
	        final BigDecimal epsEstimateNextQuarter) {
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
