package com.leonarduk.finance.stockfeed;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.io.Resources;
import com.leonarduk.finance.stockfeed.StockFeed.Exchange;

public class Instrument {
	public enum AssetType {
		CASH, EQUITY, BOND, COMMODITIES, PROPERTY, ETF, FUND, FX, UNKNOWN;

		public static AssetType fromString(final String value) {
			try {
				return AssetType.valueOf(value.toUpperCase());
			} catch (final IllegalArgumentException e) {
				LOGGER.warning("Cannot map " + e + " to AssetType");
				return AssetType.UNKNOWN;
			}
		}
	}

	static class InstrumentLoader {
		private static InstrumentLoader instance;

		private Map<String, Instrument> instruments;

		private Instrument create(final String line) {
			final Iterator<String> iter = Arrays.asList(line.split(",")).iterator();
			return new Instrument(iter.next(), AssetType.fromString(iter.next().toUpperCase()),
					AssetType.fromString(iter.next().toUpperCase()), Source.valueOf(iter.next()), iter.next(),
					iter.next(), Exchange.valueOf(iter.next()), iter.next(), iter.next(), iter.next());
		}

		private void init() throws IOException {
			final URL url = new File(Resources.getResource("data/instruments_list.csv").getFile()).toURL();
			if (url == null) {
				throw new IOException("Failed to find instrument list");
			}
			this.instruments = Resources.readLines(url, Charset.defaultCharset()).stream().skip(1)
					.map(line -> this.create(line)).collect(Collectors.toConcurrentMap(i -> i.getCode(), i -> i));
			this.instruments.values().stream().forEach(i -> this.instruments.put(i.getIsin().toUpperCase(), i));
			this.instruments.put(CASH.isin.toUpperCase(), CASH);
		}

	}

	private static final Logger LOGGER = Logger.getLogger(Instrument.class.getName());

	public static final Instrument UNKNOWN = new Instrument("UNKNOWN", AssetType.UNKNOWN, AssetType.UNKNOWN,
			Source.MANUAL, "UNKNOWN", "UNKNOWN", Exchange.London, "UNKNOWN", "GBP", "UNKNOWN");

	public static final Instrument CASH = new Instrument("CASH", AssetType.CASH, AssetType.CASH, Source.MANUAL, "Cash",
			"Cash", Exchange.London, "Cash", "GBP", "");

	public static Instrument fromString(final String symbol) {
		if (InstrumentLoader.instance == null) {
			InstrumentLoader.instance = new InstrumentLoader();
			try {
				InstrumentLoader.instance.init();
			} catch (final IOException e) {
				LOGGER.warning(e.getMessage());
				LOGGER.warning("Could not map " + symbol);
				return Instrument.UNKNOWN;
			}
		}
		if (InstrumentLoader.instance.instruments.containsKey(symbol.toUpperCase())) {
			return InstrumentLoader.instance.instruments.get(symbol.toUpperCase());
		}

		LOGGER.warning("Could not map " + symbol);
		return Instrument.UNKNOWN;
	}

	public static Collection<Instrument> values() {
		return InstrumentLoader.instance.instruments.values();
	}

	private final AssetType assetType;
	private final String category;

	private final String code;

	private final String currency;

	private final String googleCode;

	private final String isin;

	private final String name;

	private final Source source;

	private final AssetType underlyingType;

	private final Exchange exchange;

	Instrument(final String name, final AssetType type, final AssetType underlying, final Source source,
			final String isin, final String code, final Exchange exchange, final String category, final String currency,
			final String googleCode) {
		this.assetType = type;
		this.underlyingType = underlying;
		this.source = source;
		this.isin = isin;
		this.code = code;
		this.name = name;
		this.category = category;
		this.currency = currency;
		this.googleCode = googleCode;
		this.exchange = exchange;
	}

	public AssetType assetType() {
		return this.assetType;
	}

	public String category() {
		return this.category;
	}

	public String code() {
		return this.code;
	}

	public String currency() {
		return this.currency;
	}

	public AssetType getAssetType() {
		return this.assetType;
	}

	public String getCategory() {
		return this.category;
	}

	public String getCode() {
		return this.code;
	}

	public String getCurrency() {
		return this.currency;
	}

	public Exchange getExchange() {
		return this.exchange;
	}

	public String getGoogleCode() {
		return this.googleCode;
	}

	public String getIsin() {
		return this.isin;
	}

	public String getName() {
		return this.name;
	}

	public Source getSource() {
		return this.source;
	}

	public String isin() {
		return this.isin;
	}

	public Source source() {
		return this.source;
	}

	public AssetType underlyingType() {
		return this.underlyingType;
	}

}
