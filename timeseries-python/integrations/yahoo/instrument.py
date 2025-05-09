import json
from integrations.portfolioperformance.api.instrument_builder import InstrumentBuilder
from integrations.portfolioperformance.api.instrument_details import upsert_instrument_from_json, extract_instrument, \
    ftse_tickers_missing_from_file


def fetch_instrument_from_yahoo(ticker_symbol: str):
    return yf.Ticker(ticker_symbol)


def create_instrument_from_yahoo(ticker_symbol: str, xml_file: str, output_file: str):
    info = fetch_instrument_from_yahoo(ticker_symbol=ticker_symbol).info

    required_fields = ["longName","shortName", "currency", "symbol"]
    for field in required_fields:
        if field not in info:
            print(f"Missing '{field}' from Yahoo Finance data for {ticker_symbol}")
            return

    currency = info["currency"].upper()
    if currency == "GBp":
        currency = "GBX"  # Adjust for PortfolioPerformance expectations

    instrument = (
        InstrumentBuilder()
        .with_name(info["longName"])
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

    missing = ftse_tickers_missing_from_file(xml_file)
    print(f"\n⛔  {len(missing)} FTSE‑All‑Share tickers are NOT in your XML:")
    print(sorted(list(missing))[:50], "…")  # preview first 50


    for ticker in missing:
        create_instrument_from_yahoo(
            ticker_symbol=ticker,
            xml_file=xml_file,
            output_file=output_file
        )
        data = extract_instrument(output_file, ticker, format="json")
        print("\n✅ JSON Format:")
        print(json.dumps(data, indent=2))


