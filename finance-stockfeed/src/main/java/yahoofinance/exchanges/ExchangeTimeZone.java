
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

	public static final Map<String, TimeZone>	INDEX_TIMEZONES		= new HashMap<>();
	public static final Logger					logger				= Logger
	        .getLogger(ExchangeTimeZone.class.getName());

	public static final Map<String, TimeZone>	SUFFIX_TIMEZONES	= new HashMap<>();

	static {
		final String newYork = "America/New_York";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("CBT",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("CME",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("NYB",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("CMX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("NYM",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("OB",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("PK",
		        TimeZone.getTimeZone(newYork));
		final String ba = "America/Buenos_Aires";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BA", TimeZone.getTimeZone(ba));
		final String vi = "Europe/Vienna";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("VI", TimeZone.getTimeZone(vi));
		final String act = "Australia/ACT";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("AX", TimeZone.getTimeZone(act));
		final String sp = "America/Sao_Paulo";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SA", TimeZone.getTimeZone(sp));
		final String toronto = "America/Toronto";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("TO",
		        TimeZone.getTimeZone(toronto));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("V",
		        TimeZone.getTimeZone(toronto));
		final String sa = "America/Santiago";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SN", TimeZone.getTimeZone(sa));
		final String shanghai = "Asia/Shanghai";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SS",
		        TimeZone.getTimeZone(shanghai));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SZ",
		        TimeZone.getTimeZone(shanghai));
		final String co = "Europe/Copenhagen";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("CO", TimeZone.getTimeZone(co));
		final String paris = "Europe/Paris";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("NX",
		        TimeZone.getTimeZone(paris));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("PA",
		        TimeZone.getTimeZone(paris));
		final String berlin = "Europe/Berlin";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BE",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BM",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("DU",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("F",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("HM",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("HA",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MU",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SG",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("DE",
		        TimeZone.getTimeZone(berlin));
		final String du = "Europe/Dublin";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("IR", TimeZone.getTimeZone(du));
		final String br = "Europe/Brussels";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BR", TimeZone.getTimeZone(br));
		final String he = "Europe/Helsinki";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("HE", TimeZone.getTimeZone(he));
		final String hk = "Asia/Hong_Kong";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("HK", TimeZone.getTimeZone(hk));
		final String kolkata = "Asia/Kolkata";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BO",
		        TimeZone.getTimeZone(kolkata));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("NS",
		        TimeZone.getTimeZone(kolkata));
		final String jk = "Asia/Jakarta";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("JK", TimeZone.getTimeZone(jk));
		final String telAviv = "Asia/Tel_Aviv";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("TA",
		        TimeZone.getTimeZone(telAviv));
		final String ro = "Europe/Rome";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MI", TimeZone.getTimeZone(ro));
		final String mexico = "America/Mexico_City";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MX",
		        TimeZone.getTimeZone(mexico));
		final String am = "Europe/Amsterdam";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("AS", TimeZone.getTimeZone(am));
		final String auckland = "Pacific/Auckland";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("NZ",
		        TimeZone.getTimeZone(auckland));
		final String oslo = "Europe/Oslo";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("OL", TimeZone.getTimeZone(oslo));
		final String singapore = "Asia/Singapore";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SI",
		        TimeZone.getTimeZone(singapore));
		final String seoul = "Asia/Seoul";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("KS",
		        TimeZone.getTimeZone(seoul));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("KQ",
		        TimeZone.getTimeZone(seoul));
		final String kl = "Asia/Kuala_Lumpur";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("KL", TimeZone.getTimeZone(kl));
		final String madrid = "Europe/Madrid";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BC",
		        TimeZone.getTimeZone(madrid));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("BI",
		        TimeZone.getTimeZone(madrid));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MF",
		        TimeZone.getTimeZone(madrid));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MC",
		        TimeZone.getTimeZone(madrid));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("MA",
		        TimeZone.getTimeZone(madrid));
		final String st = "Europe/Stockholm";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("ST", TimeZone.getTimeZone(st));
		final String zurich = "Europe/Zurich";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("SW",
		        TimeZone.getTimeZone(zurich));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("Z",
		        TimeZone.getTimeZone(zurich));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("VX",
		        TimeZone.getTimeZone(zurich));
		final String taipei = "Asia/Taipei";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("TWO",
		        TimeZone.getTimeZone(taipei));
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("TW",
		        TimeZone.getTimeZone(taipei));
		final String london = "Europe/London";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("L",
		        TimeZone.getTimeZone(london));
		final String pe = "Europe/Prague";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("PR", TimeZone.getTimeZone(pe));
		final String mo = "Europe/Moscow";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("ME", TimeZone.getTimeZone(mo));
		final String at = "Europe/Athens";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("AT", TimeZone.getTimeZone(at));
		final String li = "Europe/Lisbon";
		ExchangeTimeZone.SUFFIX_TIMEZONES.put("LS", TimeZone.getTimeZone(li));

		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTSE",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^GDAXI",
		        TimeZone.getTimeZone(berlin));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FCHI",
		        TimeZone.getTimeZone(paris));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^IBEX",
		        TimeZone.getTimeZone(madrid));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^OMX", TimeZone.getTimeZone(st));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^OSEAX",
		        TimeZone.getTimeZone(oslo));
		ExchangeTimeZone.INDEX_TIMEZONES.put("ATX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^SSMI",
		        TimeZone.getTimeZone(zurich));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BFX", TimeZone.getTimeZone(br));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJI",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^OEX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NDX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BATSK",
		        TimeZone.getTimeZone(newYork));
		final String tk = "Asia/Tokyo";
		ExchangeTimeZone.INDEX_TIMEZONES.put("^N225", TimeZone.getTimeZone(tk));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^HSI", TimeZone.getTimeZone(hk));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^STI",
		        TimeZone.getTimeZone(singapore));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^AORD",
		        TimeZone.getTimeZone(act));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BSESN",
		        TimeZone.getTimeZone(kolkata));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^JKSE", TimeZone.getTimeZone(jk));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^KLSE", TimeZone.getTimeZone(kl));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NZ50",
		        TimeZone.getTimeZone(auckland));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NSEI",
		        TimeZone.getTimeZone(kolkata));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^KS11",
		        TimeZone.getTimeZone(seoul));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^TWII",
		        TimeZone.getTimeZone(taipei));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^MERV", TimeZone.getTimeZone(ba));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BVSP", TimeZone.getTimeZone(sp));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^GSPTSE",
		        TimeZone.getTimeZone(toronto));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^MXX",
		        TimeZone.getTimeZone(mexico));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^GSPC",
		        TimeZone.getTimeZone(newYork));
		final String cairo = "Africa/Cairo";
		ExchangeTimeZone.INDEX_TIMEZONES.put("^CCSI",
		        TimeZone.getTimeZone(cairo));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^TA100",
		        TimeZone.getTimeZone(telAviv));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTMC",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTLC",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTAI",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTAS",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTSC",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^FTT1X",
		        TimeZone.getTimeZone(london));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^MID",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^SP600",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^SPSUPX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^VIX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJC",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^XAU",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJT",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJU",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJA",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DWCF",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^DJU",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^IXIC",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BANK",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NBI",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^IXCO",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^IXF",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^INDS",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^INSR",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^OFIN",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^IXTC",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^TRAN",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYA",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYE",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYK",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYP",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYY",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYI",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NY",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^NYL",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^XMI",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^XAX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BATSK",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^RUI",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^RUT",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^RUA",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^SOX",
		        TimeZone.getTimeZone(newYork));
		ExchangeTimeZone.INDEX_TIMEZONES.put("^BKX",
		        TimeZone.getTimeZone(newYork));
	}

	/**
	 * Get the time zone for a specific exchange suffix
	 *
	 * @param suffix
	 *            suffix for the exchange in YahooFinance
	 * @return time zone of the exchange
	 */
	public static TimeZone get(final String suffix) {
		if (ExchangeTimeZone.SUFFIX_TIMEZONES.containsKey(suffix)) {
			return ExchangeTimeZone.SUFFIX_TIMEZONES.get(suffix);
		}
		ExchangeTimeZone.logger.log(Level.WARNING,
		        "Cannot find time zone for exchange suffix: '" + suffix
		                + "'. Using default: America/New_York");
		return ExchangeTimeZone.SUFFIX_TIMEZONES.get("");
	}

	/**
	 * Get the time zone for a specific stock or index. For stocks, the exchange suffix is extracted
	 * from the stock symbol to retrieve the time zone.
	 *
	 * @param symbol
	 *            stock symbol in YahooFinance
	 * @return time zone of the exchange on which this stock is traded
	 */
	public static TimeZone getStockTimeZone(final String symbol) {
		// First check if it's a known stock index
		if (ExchangeTimeZone.INDEX_TIMEZONES.containsKey(symbol)) {
			return ExchangeTimeZone.INDEX_TIMEZONES.get(symbol);
		}

		if (!symbol.contains(".")) {
			return ExchangeTimeZone.get("");
		}
		final String[] split = symbol.split("\\.");
		return ExchangeTimeZone.get(split[split.length - 1]);
	}
}
