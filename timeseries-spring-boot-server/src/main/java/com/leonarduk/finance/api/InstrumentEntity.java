package com.leonarduk.finance.api;

import com.leonarduk.finance.stockfeed.Instrument;
import com.leonarduk.finance.stockfeed.Source;
import com.leonarduk.finance.stockfeed.StockFeed;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class InstrumentEntity {
    private Instrument.AssetType assetType;
    private String category;
    @Id
    private String code;
    private String currency;
    private StockFeed.Exchange exchange;
    private String googleCode;
    private String isin;
    private String name;
    private Source source;
    private Instrument.AssetType underlyingType;

    public Instrument.AssetType getAssetType() {
        return assetType;
    }

    public void setAssetType(Instrument.AssetType assetType) {
        this.assetType = assetType;
    }

    @Override
    public String toString() {
        return "InstrumentEntity{" +
            "assetType=" + assetType +
            ", category='" + category + '\'' +
            ", code='" + code + '\'' +
            ", currency='" + currency + '\'' +
            ", exchange=" + exchange +
            ", googleCode='" + googleCode + '\'' +
            ", isin='" + isin + '\'' +
            ", name='" + name + '\'' +
            ", source=" + source +
            ", underlyingType=" + underlyingType +
            '}';
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public StockFeed.Exchange getExchange() {
        return exchange;
    }

    public void setExchange(StockFeed.Exchange exchange) {
        this.exchange = exchange;
    }

    public String getGoogleCode() {
        return googleCode;
    }

    public void setGoogleCode(String googleCode) {
        this.googleCode = googleCode;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Instrument.AssetType getUnderlyingType() {
        return underlyingType;
    }

    public void setUnderlyingType(Instrument.AssetType underlyingType) {
        this.underlyingType = underlyingType;
    }

}
