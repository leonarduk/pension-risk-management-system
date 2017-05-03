
package yahoofinance.histquotes;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.common.base.Objects;
import com.leonarduk.finance.stockfeed.Instrument;

/**
 * All getters can return null in case the data is not available from Yahoo
 * Finance.
 *
 * @author Stijn Strickx
 */
public class HistoricalQuote {

	private Calendar date;

	private BigDecimal open;
	private BigDecimal low;
	private BigDecimal high;
	private BigDecimal close;

	private BigDecimal adjClose;

	private Long volume;

	private Instrument instrument;

	public HistoricalQuote() {
	}

	public HistoricalQuote(final Instrument instrument, final Calendar date, final BigDecimal open,
			final BigDecimal low, final BigDecimal high, final BigDecimal close, final BigDecimal adjClose,
			final Long volume) {
		this.instrument = instrument;
		this.date = date;
		this.open = open;
		this.low = low;
		this.high = high;
		this.close = close;
		this.adjClose = adjClose;
		this.volume = volume;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final HistoricalQuote other = (HistoricalQuote) obj;
		return (this.getClose().compareTo(other.getClose()) == 0) && (this.getHigh().compareTo(other.getHigh()) == 0)
				&& (this.getLow().compareTo(other.getLow()) == 0) && Objects.equal(this.getDate(), other.getDate())
				&& (this.getInstrument().compareTo(other.getInstrument()) == 0)
				&& Objects.equal(this.getVolume(), other.getVolume())
				&& (this.getOpen().compareTo(other.getOpen()) == 0);
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

	public Calendar getDate() {
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
		return Objects.hashCode(this.getClose(), this.getHigh(), this.getDate(), this.getLow(), this.getOpen(),
				this.getInstrument(), this.getVolume());
	}

	public void setAdjClose(final BigDecimal adjClose) {
		this.adjClose = adjClose;
	}

	public void setClose(final BigDecimal close) {
		this.close = close;
	}

	public void setDate(final Calendar date) {
		this.date = date;
	}

	public void setHigh(final BigDecimal high) {
		this.high = high;
	}

	public void setLow(final BigDecimal low) {
		this.low = low;
	}

	public void setOpen(final BigDecimal open) {
		this.open = open;
	}

	public void setVolume(final Long volume) {
		this.volume = volume;
	}

	@Override
	public String toString() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		final String dateStr = dateFormat.format(this.date.getTime());
		return this.instrument + "@" + dateStr + ": " + this.low + "-" + this.high + ", " + this.open + "->"
				+ this.close + " (" + this.adjClose + ")";
	}
}
