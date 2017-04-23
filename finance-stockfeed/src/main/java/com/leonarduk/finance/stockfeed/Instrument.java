package com.leonarduk.finance.stockfeed;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.io.Resources;

import edu.emory.mathcs.backport.java.util.Arrays;

public class Instrument {
	public enum AssetType {
		CASH, ETF, FUND, UNKNOWN
	}

	static class InstrumentLoader {
		private static InstrumentLoader instance;

		private Map<String, Instrument> instruments;

		private Instrument create(String line) {
			@SuppressWarnings("unchecked")
			Iterator<String> iter = Arrays.asList(line.split(",")).iterator();
			return new Instrument(iter.next(), AssetType.valueOf(iter.next().toUpperCase()), Source.valueOf(iter.next()), iter.next(),
					iter.next(), iter.next(), iter.next(), iter.next());
		}

		private void init() throws IOException {
			URL url = new File(Resources.getResource("data/instruments_list.csv").getFile()).toURL();
			if (url == null) {
				throw new IOException("Failed to find instrument list");
			}
			instruments = Resources.readLines(url, Charset.defaultCharset()).stream().skip(1).map(line -> create(line))
					.collect(Collectors.toConcurrentMap(i -> i.getCode(), i -> i));
			instruments.values().stream().forEach(i -> instruments.put(i.getIsin(), i));
		}

	}

	public static Instrument fromString(String symbol) {
		if (InstrumentLoader.instance == null) {
			InstrumentLoader.instance = new InstrumentLoader();
			try {
				InstrumentLoader.instance.init();
			} catch (IOException e) {
				LOGGER.warning(e.getMessage());
				LOGGER.warning("Could not map " + symbol);
				return Instrument.UNKNOWN;
			}
		}
		if (InstrumentLoader.instance.instruments.containsKey(symbol)) {
			return InstrumentLoader.instance.instruments.get(symbol);
		}

		LOGGER.warning("Could not map " + symbol);
		return Instrument.UNKNOWN;
	}

	private static final Logger LOGGER = Logger.getLogger(Instrument.class.getName());

	public static final Instrument UNKNOWN = new Instrument("UNKNOWN", AssetType.UNKNOWN, Source.MANUAL, "UNKNOWN",
			"UNKNOWN", "UNKNOWN", "GBP", "UNKNOWN");
	public static final Instrument CASH = new Instrument("CASH", AssetType.CASH, Source.MANUAL, "Cash", "Cash", "Cash",
			"GBP", "");
	
	private AssetType assetType;
	private String category;
	private String code;

	private String currency;

	private String googleCode;

	private String isin;

	private String name;

	private Source source;

	Instrument(String name, AssetType type, Source source, String isin, String code, String category, String currency,
			String googleCode) {
		this.assetType = type;
		this.source = source;
		this.isin = isin;
		this.code = code;
		this.name = name;
		this.category = category;
		this.currency = currency;
		this.googleCode = googleCode;
	}

	public AssetType assetType() {
		return assetType;
	}

	public String category() {
		return category;
	}

	public String code() {
		return code;
	}

	public String currency() {
		return currency;
	}

	public String fullName() {
		return isin;
	}

	public AssetType getAssetType() {
		return assetType;
	}

	public String getCategory() {
		return category;
	}

	public String getCode() {
		return code;
	}

	public String getCurrency() {
		return currency;
	}

	public String getGoogleCode() {
		return googleCode;
	}

	public String getIsin() {
		return isin;
	}

	public String getName() {
		return name;
	}

	public Source getSource() {
		return source;
	}

	public Source source() {
		return source;
	}

	public static Collection<Instrument> values() {
		return InstrumentLoader.instance.instruments.values();
	}

}
