package com.leonarduk.finance.stockfeed;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
		final String fullStop = ".";
		if (localSymbol.contains(fullStop)) {
			localSymbol = localSymbol.substring(0, localSymbol.indexOf(fullStop));
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
	public boolean equals(final Object other) {
		if (!(other instanceof Instrument)) {
			return false;
		}
		final Instrument castOther = (Instrument) other;
		return new EqualsBuilder().append(this.assetType, castOther.assetType)
		        .append(this.category, castOther.category).append(this.code, castOther.code)
		        .append(this.currency, castOther.currency).append(this.exchange, castOther.exchange)
		        .append(this.googleCode, castOther.googleCode).append(this.isin, castOther.isin)
		        .append(this.name, castOther.name).append(this.source, castOther.source)
		        .append(this.underlyingType, castOther.underlyingType).isEquals();
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
		return new HashCodeBuilder().append(this.assetType).append(this.category).append(this.code)
		        .append(this.currency).append(this.exchange).append(this.googleCode)
		        .append(this.isin).append(this.name).append(this.source).append(this.underlyingType)
		        .toHashCode();
	}

	public String isin() {
		return this.isin;
	}

	public Source source() {
		return this.source;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("assetType", this.assetType)
		        .append("category", this.category).append("code", this.code)
		        .append("currency", this.currency).append("exchange", this.exchange)
		        .append("googleCode", this.googleCode).append("isin", this.isin)
		        .append("name", this.name).append("source", this.source)
		        .append("underlyingType", this.underlyingType).toString();
	}

	public AssetType underlyingType() {
		return this.underlyingType;
	}

}
