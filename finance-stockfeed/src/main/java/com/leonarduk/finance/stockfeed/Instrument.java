package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import com.leonarduk.finance.utils.ResourceTools;

public class Instrument {
	private final AssetType			assetType;

	private final String			category;

	private final String			code;

	private final String			currency;

	private final Exchange			exchange;

	private final String			googleCode;

	private final String			isin;

	private final String			name;

	private final Source			source;

	private final AssetType			underlyingType;

	public static final Instrument	CASH			= new Instrument("CASH", AssetType.CASH,
	        AssetType.CASH, Source.MANUAL, Instrument.CASH_TEXT, Instrument.CASH_TEXT,
	        Exchange.London, Instrument.CASH_TEXT, Instrument.GBP, "");

	private static final String		CASH_TEXT		= "Cash";

	private static final String		GBP				= "GBP";

	private static final Logger		LOGGER			= Logger.getLogger(Instrument.class.getName());
	public static final Instrument	UNKNOWN			= new Instrument(Instrument.UNKNOWN_TEXT,
	        AssetType.UNKNOWN, AssetType.UNKNOWN, Source.MANUAL, Instrument.UNKNOWN_TEXT,
	        Instrument.UNKNOWN_TEXT, Exchange.London, Instrument.UNKNOWN_TEXT, Instrument.GBP,
	        Instrument.UNKNOWN_TEXT);

	private static final String		UNKNOWN_TEXT	= "UNKNOWN";

	public enum AssetType {
		BOND, CASH, COMMODITIES, EQUITY, ETF, FUND, FX, PROPERTY, UNKNOWN;

		public static AssetType fromString(final String value) {
			try {
				return AssetType.valueOf(value.toUpperCase());
			}
			catch (final IllegalArgumentException e) {
				Instrument.LOGGER.warning("Cannot map " + e + " to AssetType");
				return AssetType.UNKNOWN;
			}
		}
	}

	static class InstrumentLoader {
		private Map<String, Instrument>	instruments	= null;

		private static InstrumentLoader	instance;

		public static InstrumentLoader getInstance() throws IOException {
			if (InstrumentLoader.instance == null) {
				InstrumentLoader.instance = new InstrumentLoader();
				try {
					InstrumentLoader.instance.init();
				}
				catch (final IOException | URISyntaxException e) {
					throw new IOException(e);
				}
			}

			return InstrumentLoader.instance;
		}

		private Instrument create(final String line) {
			final Iterator<String> iter = Arrays.asList(line.split(",")).iterator();
			return new Instrument(iter.next(), AssetType.fromString(iter.next().toUpperCase()),
			        AssetType.fromString(iter.next().toUpperCase()), Source.valueOf(iter.next()),
			        iter.next(), iter.next(), Exchange.valueOf(iter.next()), iter.next(),
			        iter.next(), iter.next());
		}

		private void init() throws IOException, URISyntaxException {
			this.instruments = ResourceTools
			        .getResourceAsLines("resources/data/instruments_list.csv").stream().skip(1)
			        .map(line -> this.create(line))
			        .collect(Collectors.toConcurrentMap(i -> i.getCode(), i -> i));
			this.instruments.values().stream()
			        .forEach(i -> this.instruments.put(i.getIsin().toUpperCase(), i));
			this.instruments.values().stream()
			        .forEach(i -> this.instruments.put(i.getGoogleCode().toUpperCase(), i));
			this.instruments.put(Instrument.CASH.isin.toUpperCase(), Instrument.CASH);
		}

	}

	public static Instrument fromString(final String symbol) throws IOException {
		String localSymbol = symbol;
		if (localSymbol.contains(".")) {
			localSymbol = localSymbol.substring(0, localSymbol.indexOf("."));
		}
		if (InstrumentLoader.getInstance().instruments.containsKey(localSymbol.toUpperCase())) {
			return InstrumentLoader.getInstance().instruments.get(localSymbol.toUpperCase());
		}

		Instrument.LOGGER.warning("Could not map " + symbol);
		return Instrument.UNKNOWN;
	}

	public static Collection<Instrument> values() throws IOException {
		return InstrumentLoader.getInstance().instruments.values();
	}

	Instrument(final String name, final AssetType type, final AssetType underlying,
	        final Source source, final String isin, final String code, final Exchange exchange,
	        final String category, final String currency, final String googleCode) {
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

	public int compareTo(final Instrument instrument) {
		return this.code.compareTo(instrument.code);
	}

	public String currency() {
		return this.currency;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Instrument other = (Instrument) obj;
		if (this.assetType != other.assetType) {
			return false;
		}
		if (this.category == null) {
			if (other.category != null) {
				return false;
			}
		}
		else if (!this.category.equals(other.category)) {
			return false;
		}
		if (this.code == null) {
			if (other.code != null) {
				return false;
			}
		}
		else if (!this.code.equals(other.code)) {
			return false;
		}
		if (this.currency == null) {
			if (other.currency != null) {
				return false;
			}
		}
		else if (!this.currency.equals(other.currency)) {
			return false;
		}
		if (this.exchange != other.exchange) {
			return false;
		}
		if (this.googleCode == null) {
			if (other.googleCode != null) {
				return false;
			}
		}
		else if (!this.googleCode.equals(other.googleCode)) {
			return false;
		}
		if (this.isin == null) {
			if (other.isin != null) {
				return false;
			}
		}
		else if (!this.isin.equals(other.isin)) {
			return false;
		}
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		}
		else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.source != other.source) {
			return false;
		}
		if (this.underlyingType != other.underlyingType) {
			return false;
		}
		return true;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.assetType == null) ? 0 : this.assetType.hashCode());
		result = (prime * result) + ((this.category == null) ? 0 : this.category.hashCode());
		result = (prime * result) + ((this.code == null) ? 0 : this.code.hashCode());
		result = (prime * result) + ((this.currency == null) ? 0 : this.currency.hashCode());
		result = (prime * result) + ((this.exchange == null) ? 0 : this.exchange.hashCode());
		result = (prime * result) + ((this.googleCode == null) ? 0 : this.googleCode.hashCode());
		result = (prime * result) + ((this.isin == null) ? 0 : this.isin.hashCode());
		result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
		result = (prime * result) + ((this.source == null) ? 0 : this.source.hashCode());
		result = (prime * result)
		        + ((this.underlyingType == null) ? 0 : this.underlyingType.hashCode());
		return result;
	}

	public String isin() {
		return this.isin;
	}

	public Source source() {
		return this.source;
	}

	@Override
	public String toString() {
		return "Instrument [assetType=" + this.assetType + ", category=" + this.category + ", code="
		        + this.code + ", currency=" + this.currency + ", googleCode=" + this.googleCode
		        + ", isin=" + this.isin + ", name=" + this.name + ", source=" + this.source
		        + ", underlyingType=" + this.underlyingType + ", exchange=" + this.exchange + "]";
	}

	public AssetType underlyingType() {
		return this.underlyingType;
	}

}
