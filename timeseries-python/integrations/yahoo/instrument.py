import json
from integrations.portfolioperformance.api.instrument_builder import InstrumentBuilder
from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json, extract_instrument

def fetch_instrument_from_yahoo(ticker_symbol: str):
    return yf.Ticker(ticker_symbol)


def create_instrument_from_yahoo(ticker_symbol: str, isin: str, xml_file: str, output_file: str):
    info = fetch_instrument_from_yahoo(ticker_symbol=ticker_symbol).info

    required_fields = ["longName","shortName", "currency", "symbol",'trailingPE']
    for field in required_fields:
        if field not in info:
            raise ValueError(f"Missing '{field}' from Yahoo Finance data for {ticker_symbol}")
    currency = info["currency"].upper()
    if currency == "GBP":
        currency = "GBX"  # Adjust for PortfolioPerformance expectations

    instrument = (
        InstrumentBuilder()
        .with_name(info["shortName"])
        .with_isin(isin)
        .with_ticker(info["symbol"])
        .with_currency(currency)
        # .with_feed("GENERIC_HTML_TABLE", feed_url=f"http://localhost:8091/stock/ticker/{ticker_symbol}")
        .with_updated_at()
        .build()
    )

    print("✅ Built instrument from Yahoo:")
    print(json.dumps(instrument, indent=2))

    upsert_instrument_from_json(
        xml_file=xml_file,
        json_data=instrument,
        output_file=output_file
    )
    print(f"✅ Instrument written to: {output_file}")

import yfinance as yf

def get_latest_price(ticker):
    try:
        hist = yf.Ticker(ticker).history(period="1d")
        if not hist.empty:
            latest_price = float(hist["Close"].iloc[-1])
            return round(latest_price, 2)
    except Exception:
        pass
    return 100.00  # fallback

# 🧪 Example usage:
if __name__ == "__main__":
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"

    # instrument = extract_instrument(xml_file=xml_file, identifier="GB00B19NLV48", format="json")
    # print("\n✅ JSON Format:")
    # print(json.dumps(instrument, indent=2))
    #
    # instrument["tickerSymbol"] = "EXPN.L"
    #
    # upsert_instrument_from_json(
    #     xml_file=xml_file,
    #     json_data=instrument,
    #     output_file=xml_file
    # )
    #
    isin = "GG00B90J5Z95"
    create_instrument_from_yahoo(
        ticker_symbol="TFIF.L",
        isin=isin,  # ← replace with the actual ISIN for TFIF
        xml_file=xml_file,
        output_file=output_file
    )

    data = extract_instrument(output_file, isin, format="json")
    print("\n✅ JSON Format:")
    print(json.dumps(data, indent=2))


