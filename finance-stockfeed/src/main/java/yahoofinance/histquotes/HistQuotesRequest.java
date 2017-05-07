package yahoofinance.histquotes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.LocalDate;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.yahoo.YahooFeed;
import com.leonarduk.finance.utils.HtmlTools;
import com.leonarduk.finance.utils.NumberUtils;
import com.leonarduk.finance.utils.StringUtils;

/**
 *
 * @author Stijn Strickx
 */
public class HistQuotesRequest {

	public static final Calendar DEFAULT_FROM = Calendar.getInstance();

	static {
		DEFAULT_FROM.add(Calendar.YEAR, -1);
	}
	public static final Calendar DEFAULT_TO = Calendar.getInstance();

	public static final Interval DEFAULT_INTERVAL = Interval.MONTHLY;

	public static final Logger logger = Logger.getLogger(HistQuotesRequest.class.getName());

	private final Instrument instrument;
	private final Calendar from;
	private final Calendar to;

	private final Interval interval;

	public HistQuotesRequest(final Instrument instrument) {
		this(instrument, DEFAULT_INTERVAL);
	}

	public HistQuotesRequest(final Instrument instrument, final Calendar from, final Calendar to) {
		this(instrument, from, to, DEFAULT_INTERVAL);
	}

	public HistQuotesRequest(final Instrument instrument, final Calendar from, final Calendar to,
			final Interval interval) {
		this.instrument = instrument;
		this.from = this.cleanHistCalendar(from);
		this.to = this.cleanHistCalendar(to);
		this.interval = interval;
	}

	public HistQuotesRequest(final Instrument instrument, final Date from, final Date to) {
		this(instrument, from, to, DEFAULT_INTERVAL);
	}

	public HistQuotesRequest(final Instrument instrument, final Date from, final Date to, final Interval interval) {
		this(instrument, interval);
		this.from.setTime(from);
		this.to.setTime(to);
		this.cleanHistCalendar(this.from);
		this.cleanHistCalendar(this.to);
	}

	public HistQuotesRequest(final Instrument instrument, final Interval interval) {
		this(instrument, DEFAULT_FROM, DEFAULT_TO, interval);
	}

	/**
	 * Put everything smaller than days at 0
	 *
	 * @param cal
	 *            calendar to be cleaned
	 */
	private Calendar cleanHistCalendar(final Calendar cal) {
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		return cal;
	}

	public List<HistoricalQuote> getResult() throws IOException {

		final List<HistoricalQuote> result = new ArrayList<>();

		if (this.from.after(this.to)) {
			YahooFeed.logger.log(Level.WARNING,
					"Unable to retrieve historical quotes. " + "From-date should not be after to-date. From: "
							+ this.from.getTime() + ", to: " + this.to.getTime());
			return result;
		}

		final Map<String, String> params = new LinkedHashMap<>();
		params.put("s", YahooFeed.getQueryName(this.instrument));

		params.put("a", String.valueOf(this.from.get(Calendar.MONTH)));
		params.put("b", String.valueOf(this.from.get(Calendar.DAY_OF_MONTH)));
		params.put("c", String.valueOf(this.from.get(Calendar.YEAR)));

		params.put("d", String.valueOf(this.to.get(Calendar.MONTH)));
		params.put("e", String.valueOf(this.to.get(Calendar.DAY_OF_MONTH)));
		params.put("f", String.valueOf(this.to.get(Calendar.YEAR)));

		params.put("g", this.interval.getTag());

		params.put("ignore", ".csv");

		final String url = YahooFeed.HISTQUOTES_BASE_URL + "?" + HtmlTools.getURLParameters(params);

		// Get CSV from Yahoo
		YahooFeed.logger.log(Level.INFO, ("Sending request: " + url));

		final URL request = new URL(url);
		final URLConnection connection = request.openConnection();
		connection.setConnectTimeout(YahooFeed.CONNECTION_TIMEOUT);
		connection.setReadTimeout(YahooFeed.CONNECTION_TIMEOUT);
		final InputStreamReader is = new InputStreamReader(connection.getInputStream());
		final BufferedReader br = new BufferedReader(is);
		br.readLine(); // skip the first line
		// Parse CSV
		for (String line = br.readLine(); line != null; line = br.readLine()) {

			YahooFeed.logger.log(Level.INFO, ("Parsing CSV line: " + StringUtils.unescape(line)));
			final HistoricalQuote quote = this.parseCSVLine(line);
			result.add(quote);
		}
		return result;
	}

	private HistoricalQuote parseCSVLine(final String line) {
		final String[] data = line.split(YahooFeed.QUOTES_CSV_DELIMITER);
		return new HistoricalQuote(this.instrument, LocalDate.parse(data[0]), NumberUtils.getBigDecimal(data[1]),
				NumberUtils.getBigDecimal(data[3]), NumberUtils.getBigDecimal(data[2]),
				NumberUtils.getBigDecimal(data[4]), NumberUtils.getBigDecimal(data[6]), NumberUtils.getLong(data[5]),
				"Yahoo");
	}

}
