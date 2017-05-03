
package yahoofinance.quotes.fx;

import java.util.ArrayList;
import java.util.List;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.utils.Utils;

import yahoofinance.YahooFinance;
import yahoofinance.quotes.QuotesProperty;
import yahoofinance.quotes.QuotesRequest;

/**
 *
 * @author Stijn Strickx
 */
public class FxQuotesRequest extends QuotesRequest<FxQuote> {

	public static final List<QuotesProperty> DEFAULT_PROPERTIES = new ArrayList<>();
	static {
		DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		DEFAULT_PROPERTIES.add(QuotesProperty.LastTradePriceOnly);
	}

	public FxQuotesRequest(final Instrument instrument) {
		super(instrument, FxQuotesRequest.DEFAULT_PROPERTIES);
	}

	@Override
	protected FxQuote parseCSVLine(final String line) {
		final String[] split = Utils.stripOverhead(line).split(YahooFinance.QUOTES_CSV_DELIMITER);
		if (split.length >= 2) {
			return new FxQuote(Instrument.fromString(split[0]), Utils.getBigDecimal(split[1]));
		}
		return null;
	}

}
