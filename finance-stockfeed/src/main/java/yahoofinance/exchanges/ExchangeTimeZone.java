
package yahoofinance.exchanges;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stijn Strickx
 */
public class ExchangeTimeZone {

	public static final Map<String, TimeZone> SUFFIX_TIMEZONES = new HashMap<>();
	public static final Map<String, TimeZone> INDEX_TIMEZONES = new HashMap<>();

	static {
		String newYork = "America/New_York";
		SUFFIX_TIMEZONES.put("", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("CBT", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("CME", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("NYB", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("CMX", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("NYM", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("OB", TimeZone.getTimeZone(newYork));
		SUFFIX_TIMEZONES.put("PK", TimeZone.getTimeZone(newYork));
		String ba = "America/Buenos_Aires";
		SUFFIX_TIMEZONES.put("BA", TimeZone.getTimeZone(ba));
		String vi = "Europe/Vienna";
		SUFFIX_TIMEZONES.put("VI", TimeZone.getTimeZone(vi));
		String act = "Australia/ACT";
		SUFFIX_TIMEZONES.put("AX", TimeZone.getTimeZone(act));
		String sp = "America/Sao_Paulo";
		SUFFIX_TIMEZONES.put("SA", TimeZone.getTimeZone(sp));
		String toronto = "America/Toronto";
		SUFFIX_TIMEZONES.put("TO", TimeZone.getTimeZone(toronto));
		SUFFIX_TIMEZONES.put("V", TimeZone.getTimeZone(toronto));
		String sa = "America/Santiago";
		SUFFIX_TIMEZONES.put("SN", TimeZone.getTimeZone(sa));
		String shanghai = "Asia/Shanghai";
		SUFFIX_TIMEZONES.put("SS", TimeZone.getTimeZone(shanghai));
		SUFFIX_TIMEZONES.put("SZ", TimeZone.getTimeZone(shanghai));
		String co = "Europe/Copenhagen";
		SUFFIX_TIMEZONES.put("CO", TimeZone.getTimeZone(co));
		String paris = "Europe/Paris";
		SUFFIX_TIMEZONES.put("NX", TimeZone.getTimeZone(paris));
		SUFFIX_TIMEZONES.put("PA", TimeZone.getTimeZone(paris));
		String berlin = "Europe/Berlin";
		SUFFIX_TIMEZONES.put("BE", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("BM", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("DU", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("F", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("HM", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("HA", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("MU", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("SG", TimeZone.getTimeZone(berlin));
		SUFFIX_TIMEZONES.put("DE", TimeZone.getTimeZone(berlin));
		String du = "Europe/Dublin";
		SUFFIX_TIMEZONES.put("IR", TimeZone.getTimeZone(du));
		String br = "Europe/Brussels";
		SUFFIX_TIMEZONES.put("BR", TimeZone.getTimeZone(br));
		String he = "Europe/Helsinki";
		SUFFIX_TIMEZONES.put("HE", TimeZone.getTimeZone(he));
		String hk = "Asia/Hong_Kong";
		SUFFIX_TIMEZONES.put("HK", TimeZone.getTimeZone(hk));
		String kolkata = "Asia/Kolkata";
		SUFFIX_TIMEZONES.put("BO", TimeZone.getTimeZone(kolkata));
		SUFFIX_TIMEZONES.put("NS", TimeZone.getTimeZone(kolkata));
		String jk = "Asia/Jakarta";
		SUFFIX_TIMEZONES.put("JK", TimeZone.getTimeZone(jk));
		String telAviv = "Asia/Tel_Aviv";
		SUFFIX_TIMEZONES.put("TA", TimeZone.getTimeZone(telAviv));
		String ro = "Europe/Rome";
		SUFFIX_TIMEZONES.put("MI", TimeZone.getTimeZone(ro));
		String mexico = "America/Mexico_City";
		SUFFIX_TIMEZONES.put("MX", TimeZone.getTimeZone(mexico));
		String am = "Europe/Amsterdam";
		SUFFIX_TIMEZONES.put("AS", TimeZone.getTimeZone(am));
		String auckland = "Pacific/Auckland";
		SUFFIX_TIMEZONES.put("NZ", TimeZone.getTimeZone(auckland));
		String oslo = "Europe/Oslo";
		SUFFIX_TIMEZONES.put("OL", TimeZone.getTimeZone(oslo));
		String singapore = "Asia/Singapore";
		SUFFIX_TIMEZONES.put("SI", TimeZone.getTimeZone(singapore));
		String seoul = "Asia/Seoul";
		SUFFIX_TIMEZONES.put("KS", TimeZone.getTimeZone(seoul));
		SUFFIX_TIMEZONES.put("KQ", TimeZone.getTimeZone(seoul));
		String kl = "Asia/Kuala_Lumpur";
		SUFFIX_TIMEZONES.put("KL", TimeZone.getTimeZone(kl));
		String madrid = "Europe/Madrid";
		SUFFIX_TIMEZONES.put("BC", TimeZone.getTimeZone(madrid));
		SUFFIX_TIMEZONES.put("BI", TimeZone.getTimeZone(madrid));
		SUFFIX_TIMEZONES.put("MF", TimeZone.getTimeZone(madrid));
		SUFFIX_TIMEZONES.put("MC", TimeZone.getTimeZone(madrid));
		SUFFIX_TIMEZONES.put("MA", TimeZone.getTimeZone(madrid));
		String st = "Europe/Stockholm";
		SUFFIX_TIMEZONES.put("ST", TimeZone.getTimeZone(st));
		String zurich = "Europe/Zurich";
		SUFFIX_TIMEZONES.put("SW", TimeZone.getTimeZone(zurich));
		SUFFIX_TIMEZONES.put("Z", TimeZone.getTimeZone(zurich));
		SUFFIX_TIMEZONES.put("VX", TimeZone.getTimeZone(zurich));
		String taipei = "Asia/Taipei";
		SUFFIX_TIMEZONES.put("TWO", TimeZone.getTimeZone(taipei));
		SUFFIX_TIMEZONES.put("TW", TimeZone.getTimeZone(taipei));
		String london = "Europe/London";
		SUFFIX_TIMEZONES.put("L", TimeZone.getTimeZone(london));
		String pe = "Europe/Prague";
		SUFFIX_TIMEZONES.put("PR", TimeZone.getTimeZone(pe));
		String mo = "Europe/Moscow";
		SUFFIX_TIMEZONES.put("ME", TimeZone.getTimeZone(mo));
		String at = "Europe/Athens";
		SUFFIX_TIMEZONES.put("AT", TimeZone.getTimeZone(at));
		String li = "Europe/Lisbon";
		SUFFIX_TIMEZONES.put("LS", TimeZone.getTimeZone(li));

		INDEX_TIMEZONES.put("^FTSE", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^GDAXI", TimeZone.getTimeZone(berlin));
		INDEX_TIMEZONES.put("^FCHI", TimeZone.getTimeZone(paris));
		INDEX_TIMEZONES.put("^IBEX", TimeZone.getTimeZone(madrid));
		INDEX_TIMEZONES.put("^OMX", TimeZone.getTimeZone(st));
		INDEX_TIMEZONES.put("^OSEAX", TimeZone.getTimeZone(oslo));
		INDEX_TIMEZONES.put("ATX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^SSMI", TimeZone.getTimeZone(zurich));
		INDEX_TIMEZONES.put("^BFX", TimeZone.getTimeZone(br));
		INDEX_TIMEZONES.put("^DJI", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^OEX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NDX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^BATSK", TimeZone.getTimeZone(newYork));
		String tk = "Asia/Tokyo";
		INDEX_TIMEZONES.put("^N225", TimeZone.getTimeZone(tk));
		INDEX_TIMEZONES.put("^HSI", TimeZone.getTimeZone(hk));
		INDEX_TIMEZONES.put("^STI", TimeZone.getTimeZone(singapore));
		INDEX_TIMEZONES.put("^AORD", TimeZone.getTimeZone(act));
		INDEX_TIMEZONES.put("^BSESN", TimeZone.getTimeZone(kolkata));
		INDEX_TIMEZONES.put("^JKSE", TimeZone.getTimeZone(jk));
		INDEX_TIMEZONES.put("^KLSE", TimeZone.getTimeZone(kl));
		INDEX_TIMEZONES.put("^NZ50", TimeZone.getTimeZone(auckland));
		INDEX_TIMEZONES.put("^NSEI", TimeZone.getTimeZone(kolkata));
		INDEX_TIMEZONES.put("^KS11", TimeZone.getTimeZone(seoul));
		INDEX_TIMEZONES.put("^TWII", TimeZone.getTimeZone(taipei));
		INDEX_TIMEZONES.put("^MERV", TimeZone.getTimeZone(ba));
		INDEX_TIMEZONES.put("^BVSP", TimeZone.getTimeZone(sp));
		INDEX_TIMEZONES.put("^GSPTSE", TimeZone.getTimeZone(toronto));
		INDEX_TIMEZONES.put("^MXX", TimeZone.getTimeZone(mexico));
		INDEX_TIMEZONES.put("^GSPC", TimeZone.getTimeZone(newYork));
		String cairo = "Africa/Cairo";
		INDEX_TIMEZONES.put("^CCSI", TimeZone.getTimeZone(cairo));
		INDEX_TIMEZONES.put("^TA100", TimeZone.getTimeZone(telAviv));
		INDEX_TIMEZONES.put("^FTMC", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^FTLC", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^FTAI", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^FTAS", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^FTSC", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^FTT1X", TimeZone.getTimeZone(london));
		INDEX_TIMEZONES.put("^MID", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^SP600", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^SPSUPX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^VIX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DJC", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^XAU", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DJT", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DJU", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DJA", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DWCF", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^DJU", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^IXIC", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^BANK", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NBI", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^IXCO", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^IXF", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^INDS", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^INSR", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^OFIN", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^IXTC", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^TRAN", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYA", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYE", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYK", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYP", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYY", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYI", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NY", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^NYL", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^XMI", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^XAX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^BATSK", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^RUI", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^RUT", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^RUA", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^SOX", TimeZone.getTimeZone(newYork));
		INDEX_TIMEZONES.put("^BKX", TimeZone.getTimeZone(newYork));
	}

	public static final Logger logger = Logger.getLogger(ExchangeTimeZone.class.getName());

	/**
	 * Get the time zone for a specific exchange suffix
	 *
	 * @param suffix
	 *            suffix for the exchange in YahooFinance
	 * @return time zone of the exchange
	 */
	@SuppressWarnings("LoggerStringConcat")
	public static TimeZone get(final String suffix) {
		if (SUFFIX_TIMEZONES.containsKey(suffix)) {
			return SUFFIX_TIMEZONES.get(suffix);
		}
		logger.log(Level.WARNING,
				"Cannot find time zone for exchange suffix: '" + suffix + "'. Using default: America/New_York");
		return SUFFIX_TIMEZONES.get("");
	}

	/**
	 * Get the time zone for a specific stock or index. For stocks, the exchange
	 * suffix is extracted from the stock symbol to retrieve the time zone.
	 *
	 * @param symbol
	 *            stock symbol in YahooFinance
	 * @return time zone of the exchange on which this stock is traded
	 */
	public static TimeZone getStockTimeZone(final String symbol) {
		// First check if it's a known stock index
		if (INDEX_TIMEZONES.containsKey(symbol)) {
			return INDEX_TIMEZONES.get(symbol);
		}

		if (!symbol.contains(".")) {
			return ExchangeTimeZone.get("");
		}
		final String[] split = symbol.split("\\.");
		return ExchangeTimeZone.get(split[split.length - 1]);
	}
}
