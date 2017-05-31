
package yahoofinance.quotes.fx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;
import com.leonarduk.finance.utils.NumberUtils;
import com.leonarduk.finance.utils.StringUtils;

import yahoofinance.quotes.QuotesProperty;
import yahoofinance.quotes.QuotesRequest;

/**
 *
 * @author Stijn Strickx
 */
public class FxQuotesRequest extends QuotesRequest<FxQuote> {

	public static final List<QuotesProperty> DEFAULT_PROPERTIES = new ArrayList<>();
	static {
		FxQuotesRequest.DEFAULT_PROPERTIES.add(QuotesProperty.Symbol);
		FxQuotesRequest.DEFAULT_PROPERTIES
		        .add(QuotesProperty.LastTradePriceOnly);
	}

	public FxQuotesRequest(final Instrument instrument) {
		super(instrument, FxQuotesRequest.DEFAULT_PROPERTIES);
	}

	@Override
	protected FxQuote parseCSVLine(final String line) throws IOException {
		final String[] split = StringUtils.stripOverhead(line)
		        .split(YahooFeed.QUOTES_CSV_DELIMITER);
		if (split.length >= 2) {
			return new FxQuote(Instrument.fromString(split[0]),
			        NumberUtils.getBigDecimal(split[1]));
		}
		return null;
	}

}
