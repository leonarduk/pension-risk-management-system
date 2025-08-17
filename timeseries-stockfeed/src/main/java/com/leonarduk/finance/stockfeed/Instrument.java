package com.leonarduk.finance.stockfeed;

import com.leonarduk.finance.stockfeed.StockFeed.Exchange;
import com.leonarduk.finance.utils.ResourceTools;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public record Instrument(
        String name,
        AssetType assetType,
        AssetType underlyingType,
        Source source,
        String isin,
        String code,
        Exchange exchange,
        String category,
        String indexCategory,
        String currency,
        String googleCode,
        boolean active
) implements Comparable<Instrument> {

    public static final String GBP = "GBP";
    private static final String CASH_TEXT = "Cash";
    private static final String UNKNOWN_TEXT = "UNKNOWN";

    public static final Instrument CASH = new Instrument(CASH_TEXT, AssetType.CASH, AssetType.CASH, Source.MANUAL,
            CASH_TEXT, CASH_TEXT, Exchange.LONDON, CASH_TEXT, "", GBP, "N/A", true);

    public static final Instrument UNKNOWN = new Instrument(UNKNOWN_TEXT, AssetType.UNKNOWN, AssetType.UNKNOWN,
            Source.MANUAL, UNKNOWN_TEXT, UNKNOWN_TEXT, Exchange.LONDON, UNKNOWN_TEXT, "", GBP, UNKNOWN_TEXT, true);

    public enum AssetType {
        BOND, CASH, COMMODITIES, EQUITY, ETF, FUND, FX, PORTFOLIO, PROPERTY, INDEX, INVESTMENT_TRUST, UNKNOWN, OTHER;

        public static AssetType fromString(final String value) {
            try {
                return AssetType.valueOf(value.toUpperCase());
            } catch (final IllegalArgumentException e) {
                log.warn("Cannot map {} to AssetType", value);
                return AssetType.UNKNOWN;
            }
        }
    }

    @Slf4j
    public static class InstrumentLoader {
        private Map<String, Instrument> instruments = null;

        private static volatile InstrumentLoader instance;

        public static InstrumentLoader getInstance() throws IOException {
            if (instance == null) {
                synchronized (InstrumentLoader.class) {
                    if (instance == null) {
                        InstrumentLoader loader = new InstrumentLoader();
                        try {
                            loader.init("resources/data/instruments_list.csv");
                        } catch (URISyntaxException e) {
                            throw new IOException(e);
                        }
                        instance = loader;
                    }
                }
            }
            return instance;
        }

        private void init(String resource) throws IOException, URISyntaxException {
            List<String> lines = ResourceTools.getLines(resource);
            this.instruments = lines.stream()
                    .skip(1)
                    .map(this::create)
                    .filter(Instrument::isActive)
                    .collect(Collectors.toMap(i -> i.isin().toUpperCase(), Function.identity()));
            this.instruments.put(Instrument.CASH.isin().toUpperCase(), Instrument.CASH);
        }

        private Instrument create(final String line) {
            try {
                List<String> strings = Arrays.asList(line.split(","));

                final Iterator<String> iter = strings.iterator();

                String name = StringUtils.defaultIfEmpty(iter.next(), "");
                AssetType assetType = AssetType.fromString(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase());
                AssetType underlying = AssetType.fromString(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase());
                Source source = Source.valueOf(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase());
                String isin = StringUtils.defaultIfEmpty(iter.next(), "");
                String code = StringUtils.defaultIfEmpty(iter.next(), "");
                Exchange exchange = Exchange.valueOf(StringUtils.defaultIfEmpty(iter.next(), "").toUpperCase());
                String category = StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", "");
                String indexCategory = StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", "");
                String currency = StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", "");
                String googleCode = StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "", "");
                boolean active = Boolean.parseBoolean(StringUtils.defaultIfEmpty(iter.hasNext() ? iter.next() : "TRUE", "TRUE"));

                return new Instrument(name, assetType, underlying, source, isin, code, exchange, category, indexCategory, currency, googleCode, active);
            } catch (Exception e) {
                log.warn(String.format("Could not map %s to an instrument", line), e);
                throw e;
            }
        }

        public Map<String, Instrument> getInstruments() {
            return Collections.unmodifiableMap(instruments);
        }
    }

    public static Instrument fromString(final String symbol) {
        if(symbol.contains(":")){
            String[] parts = symbol.split(":");
            return fromString(parts[0], parts[1], parts[2], parts[3]);
        }
        return fromString(symbol, "L", "UNKNOWN", UNKNOWN_TEXT);
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
                Exchange.valueOf(region), "", "", currency, symbol, true);
    }

    public static String resolveCurrency(final String symbol) {
        String lookupSymbol = symbol;
        final String fullStop = ".";
        if (lookupSymbol.contains(fullStop) && !lookupSymbol.endsWith(".A")) {
            lookupSymbol = lookupSymbol.substring(0, lookupSymbol.indexOf(fullStop));
        }

        try {
            if (InstrumentLoader.getInstance().getInstruments().containsKey(lookupSymbol.toUpperCase())) {
                Instrument instrument = InstrumentLoader.getInstance().getInstruments().get(lookupSymbol.toUpperCase());
                if (StringUtils.isNotBlank(instrument.currency())) {
                    return instrument.currency();
                }
            }
        } catch (IOException e) {
            log.warn("Unable to load instruments for currency resolution", e);
        }

        try {
            Stock stock = YahooFinance.get(lookupSymbol);
            if (stock != null && stock.getCurrency() != null) {
                return stock.getCurrency();
            }
        } catch (IOException e) {
            log.warn("Unable to resolve currency for " + symbol, e);
        }

        return UNKNOWN_TEXT;
    }

    public static Instrument populateCurrency(final Instrument instrument) {
        if (instrument == null) {
            return null;
        }
        if (StringUtils.isNotBlank(instrument.currency()) && !UNKNOWN_TEXT.equalsIgnoreCase(instrument.currency())) {
            return instrument;
        }
        String resolved = resolveCurrency(instrument.code());
        return fromString(instrument.code(), instrument.exchange().name(), instrument.assetType().name(), resolved);
    }

    public static Instrument fxInstrument(Source source, String currencyOne, String currencyTwo) {
        return new Instrument(currencyOne + "/" + currencyTwo,
                AssetType.FX, AssetType.FX, source,
                currencyOne + currencyTwo, currencyOne + currencyTwo,
                Exchange.NA, currencyOne, currencyTwo, currencyTwo, "", true);
    }

    // compatibility getters
    public AssetType getAssetType() { return assetType; }
    public String getCategory() { return category; }
    public String getIndexCategory() { return indexCategory; }
    public String getCode() { return code; }
    public String getCurrency() { return currency; }
    public Exchange getExchange() { return exchange; }
    public String getGoogleCode() { return googleCode; }
    public String getIsin() { return isin; }
    public String getName() { return name; }
    public Source getSource() { return source; }
    public boolean isActive() { return active; }

    @Override
    public int compareTo(final Instrument instrument) {
        return this.code.compareTo(instrument.code);
    }
}
