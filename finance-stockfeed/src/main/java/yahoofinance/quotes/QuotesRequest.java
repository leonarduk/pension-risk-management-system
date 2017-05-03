package yahoofinance.quotes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;
import com.leonarduk.finance.utils.Utils;

import yahoofinance.YahooFinance;

/**
 *
 * @author Stijn Strickx
 * @param <T>
 *            Type of object that can contain the retrieved information from a
 *            quotes request
 */
public abstract class QuotesRequest<T> {

	protected final Instrument instrument;
	protected List<QuotesProperty> properties;

	public QuotesRequest(final Instrument instrument, final List<QuotesProperty> properties) {
		this.instrument = instrument;
		this.properties = properties;
	}

	private String getFieldsString() {
		final StringBuilder result = new StringBuilder();
		for (final QuotesProperty property : this.properties) {
			result.append(property.getTag());
		}
		return result.toString();
	}

	public List<QuotesProperty> getProperties() {
		return this.properties;
	}

	public String getQuery() {
		return YahooFeed.getQueryName(this.instrument);
	}

	/**
	 * Sends the request to Yahoo Finance and parses the result
	 *
	 * @return List of parsed objects resulting from the Yahoo Finance request
	 * @throws java.io.IOException
	 *             when there's a connection problem or the request is incorrect
	 */
	public List<T> getResult() throws IOException {
		final List<T> result = new ArrayList<>();

		final Map<String, String> params = new LinkedHashMap<>();
		params.put("s", this.getQuery());
		params.put("f", this.getFieldsString());
		params.put("e", ".csv");

		final String url = YahooFinance.QUOTES_BASE_URL + "?" + Utils.getURLParameters(params);

		// Get CSV from Yahoo
		YahooFinance.logger.log(Level.INFO, ("Sending request: " + url));

		final URL request = new URL(url);
		final URLConnection connection = request.openConnection();
		connection.setConnectTimeout(YahooFinance.CONNECTION_TIMEOUT);
		connection.setReadTimeout(YahooFinance.CONNECTION_TIMEOUT);
		final InputStreamReader is = new InputStreamReader(connection.getInputStream());
		final BufferedReader br = new BufferedReader(is);

		// Parse CSV
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			if (line.equals("Missing Symbols List.")) {
				YahooFinance.logger.log(Level.SEVERE, "The requested symbol was not recognized by Yahoo Finance");
			} else {
				YahooFinance.logger.log(Level.INFO, ("Parsing CSV line: " + Utils.unescape(line)));

				final T data = this.parseCSVLine(line);
				result.add(data);
			}
		}

		return result;
	}

	public T getSingleResult() throws IOException {
		final List<T> results = this.getResult();
		if (results.size() > 0) {
			return results.get(0);
		}
		return null;
	}

	protected abstract T parseCSVLine(String line);

	public void setProperties(final List<QuotesProperty> properties) {
		this.properties = properties;
	}

}
