import os
import xml.etree.ElementTree as ET
import pandas as pd
from datetime import datetime, timedelta

DATE = "Date"
PRICE = "Price"

def get_time_series(ticker, years: int = 0, xml_file="C:/path/to/investments-with-id.xml"):
    """
    Replacement for get_time_series that pulls from Portfolio Performance XML export.
    Matches by ticker, name, or ISIN.
    Filters to the last `years` if specified.
    Returns a DataFrame with Date as index and one column per ticker.
    """
    if isinstance(ticker, dict):
        tickers = ticker.keys()
    elif isinstance(ticker, list):
        tickers = set(ticker)
    elif isinstance(ticker, set):
        tickers = ticker
    else:
        tickers = {ticker}

    if not os.path.exists(xml_file):
        raise FileNotFoundError(f"XML file not found: {xml_file}")

    tree = ET.parse(xml_file)
    root = tree.getroot()
    dfs = []

    cutoff_date = None
    if years > 0:
        cutoff_date = datetime.today() - timedelta(days=365 * years)

    for tkr in tickers:
        found = False
        for security in root.findall(".//securities/security"):
            name = security.findtext("name", default="")
            isin = security.findtext("isin", default="")
            symbol = security.findtext("tickerSymbol", default="")

            if tkr.lower() in (name.lower(), isin.lower(), symbol.lower()):
                found = True
                series = []
                for price in security.findall(".//prices/price"):
                    date = price.attrib.get("t")
                    value = price.attrib.get("v")
                    if date and value:
                        date_obj = datetime.strptime(date, "%Y-%m-%d")
                        if cutoff_date is None or date_obj >= cutoff_date:
                            series.append((date_obj, int(value) / 1e9))

                if series:
                    df = pd.DataFrame(series, columns=[DATE, tkr])
                    df.set_index(DATE, inplace=True)
                    dfs.append(df)
                break

        if not found:
            print(f"⚠️ No data found for: {tkr}")

    if dfs:
        return pd.concat(dfs, axis=1).sort_index()
    else:
        print("❌ No data extracted from XML.")
        return pd.DataFrame()

# ✅ Pass values directly to main()
def main(xml_file, tickers, years=0):
    df = get_time_series(tickers, years=years, xml_file=xml_file)

    if not df.empty:
        print(f"\n✅ Extracted {len(df)} rows for {len(tickers)} ticker(s):")
        print(df.head())
    else:
        print("❌ No data returned.")

# 🎯 Call main directly here for PyCharm
if __name__ == "__main__":
    main(
        xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        tickers=["EXPN.L", "Experian plc", "GB00B19NLV48"],
        years=5
    )
