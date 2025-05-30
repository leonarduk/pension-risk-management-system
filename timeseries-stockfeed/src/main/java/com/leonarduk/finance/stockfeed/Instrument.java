package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import com.leonarduk.finance.utils.ResourceTools;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

public sealed class Instrument permits FxInstrument {
    public Instrument() {
    }

    private AssetType assetType;

    private String category;

    private String code;

    private String currency;

    private Exchange exchange;

    private String googleCode;

    private String isin;

    private String name;

    private Source source;

    private AssetType underlyingType;

    public static final Instrument CASH = new Instrument("CASH", AssetType.CASH, AssetType.CASH, Source.MANUAL,
            Instrument.CASH_TEXT, Instrument.CASH_TEXT, Exchange.LONDON, Instrument.CASH_TEXT, Instrument.GBP, "N/A");

    private static final String CASH_TEXT = "Cash";

    public static final String GBP = "GBP";

    private static final Logger LOGGER = LoggerFactory.getLogger(Instrument.class.getName());

    public static final Instrument UNKNOWN = new Instrument(Instrument.UNKNOWN_TEXT, AssetType.UNKNOWN,
            AssetType.UNKNOWN, Source.MANUAL, Instrument.UNKNOWN_TEXT, Instrument.UNKNOWN_TEXT, Exchange.LONDON,
            Instrument.UNKNOWN_TEXT, Instrument.GBP, Instrument.UNKNOWN_TEXT);

    private static final String UNKNOWN_TEXT = "UNKNOWN";


    public enum AssetType {
        BOND, CASH, COMMODITIES, EQUITY, ETF, FUND, FX, PORTFOLIO, PROPERTY, INDEX, INVESTMENT_TRUST, UNKNOWN, OTHER;

        public static AssetType fromString(final String value) {
            try {
                return AssetType.valueOf(value.toUpperCase());
            } catch (final IllegalArgumentException e) {
                Instrument.LOGGER.warn("Cannot map " + e + " to AssetType");
                return AssetType.UNKNOWN;
            }
        }
    }

    public static class InstrumentLoader {
        private Map<String, Instrument> instruments = null;

        private static InstrumentLoader instance;

        public static InstrumentLoader getInstance() throws IOException {
//			if (InstrumentLoader.instance == null) {
            InstrumentLoader.instance = new InstrumentLoader();

            try {
                instance.init("resources/data/instruments_list.csv");
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                throw new IOException(e);
            }
//			}

            return InstrumentLoader.instance;
        }

        private final static Logger logger = LoggerFactory.getLogger(Instrument.class.getName());

        private Instrument create(final String line) {
            try {
                List<String> strings = Arrays.asList(line.split(","));

                final Iterator<String> iter = strings.iterator();
                return new Instrument(StringUtils.defaultIfEmpty(iter.next(), ""),
                        AssetType.fromString(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase()),
                        AssetType.fromString(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase()),
                        Source.valueOf(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase()),
                        StringUtils.defaultIfEmpty(iter.next(), ""),
                        StringUtils.defaultIfEmpty(iter.next(), ""),
                        Exchange.valueOf(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase()),
                        StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", ""),
                        StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", ""),
                        StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", "")
                );
            } catch (Exception e) {
                logger.warn(String.format("Could not map %s to an instrument", line), e);
                throw e;
            }
        }

        public void init(String filePath) throws IOException, URISyntaxException {
            this.instruments = ResourceTools.getResourceAsLines(filePath).stream().skip(1)
                    .map(this::create).collect(Collectors.toConcurrentMap(i -> i.getCode(), i -> i));
            this.getInstruments().values().forEach(i -> this.getInstruments().put(i.getIsin().toUpperCase(), i));
            this.getInstruments().values().forEach(i -> this.getInstruments().put(i.getGoogleCode().toUpperCase(), i));
            this.getInstruments().put(Instrument.CASH.isin.toUpperCase(), Instrument.CASH);
        }

        public Map<String, Instrument> getInstruments() {
            return instruments;
        }
    }

    public static Instrument fromString(final String symbol)  {
        if(symbol.contains(":")){
            String[] parts = symbol.split(":");
            return fromString(parts[0], parts[1], parts[2], parts[3]);
        }
        return fromString(symbol, "L", "UNKNOWN", "GBP");
    }

    public static Instrument fromString(final String symbol, final String region,
                                        String type, String currency)  {
        String localSymbol = symbol;
        final String fullStop = ".";
        if (localSymbol.contains(fullStop) && ! localSymbol.endsWith(".A")) {
            localSymbol = localSymbol.substring(0, localSymbol.indexOf(fullStop));
        }
        try {
            if (InstrumentLoader.getInstance().getInstruments().containsKey(localSymbol.toUpperCase())) {
                return InstrumentLoader.getInstance().getInstruments().get(localSymbol.toUpperCase());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Instrument(symbol, AssetType.fromString(type), AssetType.fromString(type), Source.MANUAL, symbol, localSymbol,
                Exchange.valueOf(region), "", currency, symbol);
    }


    protected Instrument(final String name, final AssetType type, final AssetType underlying, final Source source,
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

    public int compareTo(final Instrument instrument) {
        return this.code.compareTo(instrument.code);
    }

    public String currency() {
        return this.currency;
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Instrument castOther)) {
            return false;
        }
        return new EqualsBuilder().append(this.assetType, castOther.assetType).append(this.category, castOther.category)
                .append(this.code, castOther.code).append(this.currency, castOther.currency)
                .append(this.exchange, castOther.exchange).append(this.googleCode, castOther.googleCode)
                .append(this.isin, castOther.isin).append(this.name, castOther.name)
                .append(this.source, castOther.source).append(this.underlyingType, castOther.underlyingType).isEquals();
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
                .append(this.currency).append(this.exchange).append(this.googleCode).append(this.isin).append(this.name)
                .append(this.source).append(this.underlyingType).toHashCode();
    }

    public String isin() {
        return this.isin;
    }

    public Source source() {
        return this.source;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("assetType", this.assetType).append("category", this.category)
                .append("code", this.code).append("currency", this.currency).append("exchange", this.exchange)
                .append("googleCode", this.googleCode).append("isin", this.isin).append("name", this.name)
                .append("source", this.source).append("underlyingType", this.underlyingType).toString();
    }

    public AssetType underlyingType() {
        return this.underlyingType;
    }

}
