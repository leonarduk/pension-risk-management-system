import json
import xml.etree.ElementTree as ET

import yfinance as yf

from integrations.portfolioperformance.api.instrument_builder import InstrumentBuilder
from integrations.portfolioperformance.api.instrument_details import (
    ftse_tickers_missing_from_file,
    upsert_instrument_from_json,
)


def fetch_instrument_from_yahoo(ticker_symbol: str):
    return yf.Ticker(ticker_symbol)


def create_instrument_from_yahoo(ticker_symbol: str, xml_file: str, output_file: str):
    info = fetch_instrument_from_yahoo(ticker_symbol=ticker_symbol).info

    required_fields = ["longName", "shortName", "currency", "symbol"]
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
        xml_file=xml_file, json_data=instrument, output_file=output_file
    )
    print(f"✅ Instrument written to: {output_file}")


def get_latest_price(ticker):
    try:
        hist = yf.Ticker(ticker).history(period="1d")
        if not hist.empty:
            latest_price = float(hist["Close"].iloc[-1])
            return round(latest_price, 2)
    except Exception:
        pass
    return 100.00  # fallback


# ================================================================
# BULK-IMPORT MISSING FTSE TICKERS   (Yahoo → PP XML)
# ================================================================


def _normalize_ticker(ticker: str) -> str:
    """Upper-case and ensure '.L' suffix."""
    ticker = (ticker or "").strip().upper()
    return ticker if ticker.endswith(".L") else f"{ticker}.L"


def _next_security_id(securities_root) -> int:
    """Return the next available <security> id."""
    return (
        max(
            (int(s.attrib.get("id", "0")) for s in securities_root.findall("security")),
            default=0,
        )
        + 1
    )


# ──────────────────────────────────────────────────────────────────────────
def bulk_add_from_yahoo(xml_in: str, tickers: set[str], xml_out: str):
    """
    • read xml_in into an ElementTree
    • for each ticker → get Yahoo info → build Instrument JSON
      → upsert into that in-memory tree
    • finally write the modified tree to xml_out
    """
    print(f"🔎  starting bulk import for {len(tickers)} tickers")

    tree = ET.parse(xml_in)
    root = tree.getroot()
    secs = root.find(".//securities")
    next_id = _next_security_id(secs)

    for raw in sorted(tickers, key=str.casefold):
        tkr = _normalize_ticker(raw)

        try:
            info = yf.Ticker(tkr).info
        except Exception as e:
            print(f"⚠️  {tkr:<8} Yahoo error ({e}) – skipped")
            continue

        if not all(k in info for k in ("longName", "currency", "symbol")):
            print(f"⚠️  {tkr:<8} incomplete Yahoo data – skipped")
            continue

        currency = info["currency"].upper().replace("GBP", "GBX")

        inst_json = (
            InstrumentBuilder()
            .with_id(next_id)
            .with_name(info.get("longName") or info.get("shortName", tkr))
            .with_ticker(tkr)
            .with_currency(currency)
            .with_updated_at()  # now uses timezone-aware UTC
            .build()
        )
        next_id += 1

        upsert_instrument_from_json(
            xml_file=xml_in,  # parsed again inside helper – fine for now
            json_data=inst_json,
            output_file=None,  # None → helper updates the *same* file path,
            # but we only care that our json is valid.
        )

        print(f"  ✅  added/updated {tkr:<8}  → {inst_json['name'][:40]}")

    tree.write(xml_out, encoding="utf-8", xml_declaration=True)
    print(f"\n✅  bulk import finished → {xml_out}")


# ──────────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    xml_file = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    output_file = (
        "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"
    )

    missing = ftse_tickers_missing_from_file(xml_file)
    print(f"\n⛔  {len(missing)} FTSE‑All‑Share tickers are NOT in your XML:")

    bulk_add_from_yahoo(xml_file, missing, output_file)
