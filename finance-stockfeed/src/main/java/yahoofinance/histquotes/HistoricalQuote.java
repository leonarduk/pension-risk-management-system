
package yahoofinance.histquotes;

import java.math.BigDecimal;

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

	private final LocalDate date;

	private final BigDecimal open;
	private final BigDecimal low;
	private final BigDecimal high;
	private final BigDecimal close;

	private final BigDecimal adjClose;

	private final String comment;

	private final Long volume;
	private final Instrument instrument;

	public HistoricalQuote(final HistoricalQuote that, final LocalDate today, final String comment) {
		this(that.getInstrument(), today, that.getOpen(), that.getLow(), that.getHigh(), that.getClose(),
				that.getAdjClose(), that.getVolume(), comment);
	}

	public HistoricalQuote(final Instrument instrument, final LocalDate date, final BigDecimal open,
			final BigDecimal low, final BigDecimal high, final BigDecimal close, final BigDecimal adjClose,
			final Long volume, final String comment) {
		this.instrument = instrument;
		this.date = date;
		this.open = open;
		this.low = low;
		this.high = high;
		this.close = close;
		this.adjClose = adjClose;
		this.volume = volume;
		this.comment = comment;
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
		final HistoricalQuote other = (HistoricalQuote) obj;
		if (!NumberUtils.areSame(this.adjClose, other.adjClose)) {
			return false;
		}
		if (!NumberUtils.areSame(this.close, other.close)) {
			return false;

		}
		if (!NumberUtils.areSame(this.high, other.high)) {
			return false;
		}
		if (!NumberUtils.areSame(this.low, other.low)) {
			return false;
		}
		if (!NumberUtils.areSame(this.open, other.open)) {
			return false;
		}

		if (this.comment == null) {
			if (other.comment != null) {
				return false;
			}
		} else if (!this.comment.equals(other.comment)) {
			return false;
		}
		if (this.date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!this.date.equals(other.date)) {
			return false;
		}
		if (this.instrument == null) {
			if (other.instrument != null) {
				return false;
			}
		} else if (!this.instrument.equals(other.instrument)) {
			return false;
		}
		if (this.volume == null) {
			if (other.volume != null) {
				return false;
			}
		} else if (!this.volume.equals(other.volume)) {
			return false;
		}
		return true;
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
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.adjClose == null) ? 0 : this.adjClose.hashCode());
		result = (prime * result) + ((this.close == null) ? 0 : this.close.hashCode());
		result = (prime * result) + ((this.comment == null) ? 0 : this.comment.hashCode());
		result = (prime * result) + ((this.date == null) ? 0 : this.date.hashCode());
		result = (prime * result) + ((this.high == null) ? 0 : this.high.hashCode());
		result = (prime * result) + ((this.instrument == null) ? 0 : this.instrument.hashCode());
		result = (prime * result) + ((this.low == null) ? 0 : this.low.hashCode());
		result = (prime * result) + ((this.open == null) ? 0 : this.open.hashCode());
		result = (prime * result) + ((this.volume == null) ? 0 : this.volume.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return this.instrument + "@" + this.date.toString() + ": " + this.low + "-" + this.high + ", " + this.open
				+ "->" + this.close + " (" + this.adjClose + ") " + this.comment;
	}
}
