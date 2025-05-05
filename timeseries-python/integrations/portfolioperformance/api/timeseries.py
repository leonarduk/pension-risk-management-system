import os
import xml.etree.ElementTree as ET
import pandas as pd
from datetime import datetime, timedelta

DATE = "Date"

def normalize_tickers(ticker_input):
    if isinstance(ticker_input, dict):
        return set(ticker_input.keys())
    elif isinstance(ticker_input, (list, set)):
        return set(ticker_input)
    else:
        return {ticker_input}

def load_securities_from_xml(xml_file):
    if not os.path.exists(xml_file):
        raise FileNotFoundError(f"XML file not found: {xml_file}")
    tree = ET.parse(xml_file)
    return tree.getroot().findall(".//securities/security")

def match_security(tkr, security):
    name = security.findtext("name", default="")
    isin = security.findtext("isin", default="")
    symbol = security.findtext("tickerSymbol", default="")
    return tkr.lower() in (name.lower(), isin.lower(), symbol.lower())

def extract_prices(security, cutoff_date):
    records = []
    for price in security.findall(".//prices/price"):
        date_str = price.attrib.get("t")
        value_str = price.attrib.get("v")
        if not date_str or not value_str:
            continue
        date_obj = datetime.strptime(date_str, "%Y-%m-%d")
        if cutoff_date and date_obj < cutoff_date:
            continue
        value = int(value_str) / 1e8
        records.append((date_obj, value))
    return records

def get_time_series(ticker, years=0, xml_file="C:/path/to/investments-with-id.xml", label_by="ticker"):
    """
    Extracts a time series from a Portfolio Performance XML export.
    Parameters:
        - ticker: a name, ISIN, or symbol or list/set of them
        - years: restrict to last N years
        - xml_file: path to XML file
        - label_by: one of 'name', 'isin', 'ticker' — which ID to use as column label
    Returns:
        - pd.DataFrame with date index and one column per matched security
    """
    assert label_by in ("name", "isin", "ticker"), "label_by must be one of: name, isin, ticker"

    tickers = normalize_tickers(ticker)
    securities = load_securities_from_xml(xml_file)
    cutoff_date = datetime.today() - timedelta(days=365 * years) if years > 0 else None

    all_dfs = []

    for tkr in tickers:
        matched = False
        for security in securities:
            if match_security(tkr, security):
                matched = True
                label = security.findtext(
                    {"name": "name", "isin": "isin", "ticker": "tickerSymbol"}[label_by],
                    default=f"UNKNOWN_{tkr}"
                )
                prices = extract_prices(security, cutoff_date)
                if prices:
                    df = pd.DataFrame(prices, columns=[DATE, label])
                    df.set_index(DATE, inplace=True)
                    all_dfs.append(df)
                break
        if not matched:
            print(f"⚠️ No data found for: {tkr}")

    if not all_dfs:
        print("❌ No data extracted from XML.")
        return pd.DataFrame()

    return pd.concat(all_dfs, axis=1).sort_index()

# ✅ main() for use in PyCharm
def main(xml_file, tickers, years=0, label_by="ticker"):
    df = get_time_series(tickers, years=years, xml_file=xml_file, label_by=label_by)
    if not df.empty:
        print(f"\n✅ Extracted {len(df)} rows for {len(tickers)} ticker(s), labelled by '{label_by}':")
        print(df.head())
    else:
        print("❌ No data returned.")

# 🎯 Call it directly for quick dev/test
if __name__ == "__main__":
    main(
        xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        tickers=["Greggs plc", "AV.L", "GB00B00FPT80"],
        years=5,
        label_by="name"  # Options: "name", "isin", or "ticker"
    )
