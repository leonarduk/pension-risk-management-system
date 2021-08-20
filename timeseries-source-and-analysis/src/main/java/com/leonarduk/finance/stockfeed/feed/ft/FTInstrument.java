package com.leonarduk.finance.stockfeed.feed.ft;

import com.leonarduk.finance.stockfeed.Instrument;

/**
 * https://markets.ft.com/data/etfs/tearsheet/historical?s=HMWO:LSE:GBX
 * https://markets.ft.com/data/equities/tearsheet/summary?s=TSCO:LSE
 * https://markets.ft.com/data/investment-trust/tearsheet/historical?s=TRG:LSE
 * https://markets.ft.com/data/funds/tearsheet/historical?s=GB00B6ZDFJ91:GBX
 */
public class FTInstrument {
    private final Instrument instrument;

    FTInstrument(Instrument instrument) {
        this.instrument = instrument;
    }

    public String getFTUrl() {
        return String.format("https://markets.ft.com/data/%s/tearsheet/historical?s=%s", getFTAssetType(), getFTCode());
    }


    private String getFTExchange() {
        switch (instrument.getExchange()) {
            case L:
            case LONDON:
                return "LSE";
            default:
                throw new UnsupportedOperationException("FT feed does not support " + instrument.getExchange());
        }
    }

    private String getFTCode() {
        Instrument.AssetType type = instrument.getAssetType();
        switch (type) {
            case EQUITY:
                return instrument.getCode() + ":" + getFTExchange();
            case INVESTMENT_TRUST:
            case ETF:
                return instrument.getCode() + ":" + getFTExchange() + ":" + instrument.getCurrency();
            case FUND:
                return instrument.getIsin() + ":" + instrument.getCurrency();
            default:
                throw new UnsupportedOperationException("FT feed does not support " + type);
        }
    }

    private String getFTAssetType() {
        Instrument.AssetType type = instrument.getAssetType();
        switch (type) {
            case EQUITY:
                return "equities";
            case INVESTMENT_TRUST:
                return "investment-trust";
            case ETF:
                return "etfs";
            case FUND:
                return "funds";
            default:
                throw new UnsupportedOperationException("FT feed does not support " + type);
        }

    }
}
