
package yahoofinance.histquotes;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.NumberUtils;

/**
 * All getters can return null in case the data is not available from Yahoo
 * Finance.
 *
 * @author Stijn Strickx
 */
public class HistoricalQuote {

	private final BigDecimal	adjClose;

	private final BigDecimal	close;
	private final String		comment;
	private final LocalDate		date;
	private final BigDecimal	high;

	private final Instrument	instrument;

	private final BigDecimal	low;

	private final BigDecimal	open;
	private final Long			volume;

	public HistoricalQuote(final HistoricalQuote that, final LocalDate today,
	        final String comment) {
		this(that.getInstrument(), today, that.getOpen(), that.getLow(),
		        that.getHigh(), that.getClose(), that.getAdjClose(),
		        that.getVolume(), comment);
	}

	public HistoricalQuote(final Instrument instrument, final LocalDate date,
	        final BigDecimal open, final BigDecimal low, final BigDecimal high,
	        final BigDecimal close, final BigDecimal adjClose,
	        final Long volume, final String comment) {
		this.instrument = instrument;
		this.date = date;
		this.open = NumberUtils.cleanBigDecimal(open);
		this.low = NumberUtils.cleanBigDecimal(low);
		this.high = NumberUtils.cleanBigDecimal(high);
		this.close = NumberUtils.cleanBigDecimal(close);
		this.adjClose = NumberUtils.cleanBigDecimal(adjClose);
		this.volume = volume;
		this.comment = comment;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof HistoricalQuote)) {
			return false;
		}
		final HistoricalQuote castOther = (HistoricalQuote) other;
		return new EqualsBuilder().append(this.adjClose, castOther.adjClose)
		        .append(this.close, castOther.close)
		        .append(this.comment, castOther.comment)
		        .append(this.date, castOther.date)
		        .append(this.high, castOther.high)
		        .append(this.instrument, castOther.instrument)
		        .append(this.low, castOther.low)
		        .append(this.open, castOther.open)
		        .append(this.volume, castOther.volume).isEquals();
	}

	/**
	 * The adjusted closing price on a specific date reflects all of the
	 * dividends and splits since that day. The adjusted closing price from a
	 * date in history can be used to calculate a close estimate of the total
	 * return, including dividends, that an investor earned if shares were
	 * purchased on that date.
	 *
	 * @return the adjusted close price
	 */
	public BigDecimal getAdjClose() {
		return this.adjClose;
	}

	public BigDecimal getClose() {
		return this.close;
	}

	public String getComment() {
		return this.comment;
	}

	public LocalDate getDate() {
		return this.date;
	}

	/**
	 *
	 * @return the intra-day high
	 */
	public BigDecimal getHigh() {
		return this.high;
	}

	public Instrument getInstrument() {
		return this.instrument;
	}

	/**
	 *
	 * @return the intra-day low
	 */
	public BigDecimal getLow() {
		return this.low;
	}

	public BigDecimal getOpen() {
		return this.open;
	}

	public Long getVolume() {
		return this.volume;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.adjClose).append(this.close)
		        .append(this.comment).append(this.date).append(this.high)
		        .append(this.instrument).append(this.low).append(this.open)
		        .append(this.volume).toHashCode();
	}

	@Override
	public String toString() {
		return this.instrument + "@" + this.date.toString() + ": " + this.low
		        + "-" + this.high + ", " + this.open + "->" + this.close + " ("
		        + this.adjClose + ") " + this.comment;
	}
}
