
package yahoofinance.quotes.fx;

import java.io.IOException;
import java.math.BigDecimal;

import com.leonarduk.finance.stockfeed.Instrument;

/**
 *
 * @author Stijn Strickx
 */
public class FxQuote {

	private BigDecimal	price;
	private Instrument	symbol;

	public FxQuote(final Instrument symbol) {
		this.symbol = symbol;
		this.price = BigDecimal.ZERO;
	}

	public FxQuote(final Instrument symbol, final BigDecimal price) {
		this.symbol = symbol;
		this.price = price;
	}

	/**
	 * Returns the requested FX rate.
	 *
	 * @return the requested FX rate
	 */
	public BigDecimal getPrice() {
		return this.price;
	}

	/**
	 * Returns the requested FX rate. This method will return 0 in the following
	 * situations:
	 * <ul>
	 * <li>the data hasn't been loaded yet in a previous request and refresh is
	 * set to false.
	 * <li>refresh is true and the data cannot be retrieved from Yahoo Finance
	 * for whatever reason (symbol not recognized, no network connection, ...)
	 * </ul>
	 *
	 * @param refresh
	 *            indicates whether the data should be requested again to Yahoo
	 *            Finance
	 * @return the requested FX rate
	 * @throws java.io.IOException
	 *             when there's a connection problem
	 */
	public BigDecimal getPrice(final boolean refresh) throws IOException {
		if (refresh) {
			final FxQuotesRequest request = new FxQuotesRequest(this.symbol);
			this.price = request.getSingleResult().getPrice();
		}
		return this.price;
	}

	public Instrument getSymbol() {
		return this.symbol;
	}

	public void setPrice(final BigDecimal price) {
		this.price = price;
	}

	public void setSymbol(final Instrument symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return this.symbol + ": " + this.price;
	}

}
