import xml.etree.ElementTree as ET
import pandas as pd
from typing import Union

DATE = "Date"


def parse_price_value(v_string):
    parts = v_string.strip().split()
    if len(parts) == 2:
        major, minor = map(int, parts)
        return major + minor / 1e8
    elif len(parts) == 1:
        return int(parts[0]) / 1e8
    else:
        raise ValueError(f"Unexpected price format: '{v_string}'")


def get_time_series_from_xml(xml_file: str, identifiers: Union[str, list, set, dict]) -> pd.DataFrame:
    """
    Fetch time series data from XML by matching ISIN, tickerSymbol, or name.

    Args:
        xml_file (str): Path to XML file.
        identifiers (str | list | set | dict): Identifiers to match (ISIN, tickerSymbol, or name).

    Returns:
        pd.DataFrame: Combined DataFrame of matched time series.
    """
    if isinstance(identifiers, dict):
        identifiers = set(identifiers.keys())
    elif isinstance(identifiers, (list, set)):
        identifiers = set(identifiers)
    else:
        identifiers = {identifiers}

    tree = ET.parse(xml_file)
    root = tree.getroot()
    securities = root.findall(".//security")

    dfs = []

    for security in securities:
        isin = security.findtext("isin", "").strip()
        ticker_symbol = security.findtext("tickerSymbol", "").strip()
        name = security.findtext("name", "").strip()

        if any(id_ in {isin, ticker_symbol, name} for id_ in identifiers):
            prices = security.find("prices").findall("price")
            data = [(price.attrib['t'], parse_price_value(price.attrib['v'])) for price in prices]

            column_name = f"{name} ({ticker_symbol})" if ticker_symbol else name
            df = pd.DataFrame(data, columns=[DATE, column_name])
            df[DATE] = pd.to_datetime(df[DATE])
            df.set_index(DATE, inplace=True)

            if not df.empty:
                start_date = df.index.min()
                end_date = df.index.max()
                print(f"{column_name} - Start date: {start_date.date()}, End date: {end_date.date()}")
                dfs.append(df)
            else:
                print(f"No data for {column_name}")

    if not dfs:
        print("❌ No matching securities found.")
        return pd.DataFrame()

    return pd.concat(dfs, axis=1)


if __name__ == "__main__":
    xml_path = r"C:\Users\User\workspaces\bitbucket\luk\data\portfolio\investments-with-id.xml"

    # You can search by:
    search_terms = ["EXPN.L", "GB00B19NLV48", "Experian plc"]  # name, ISIN, or ticker

    df = get_time_series_from_xml(xml_path, search_terms)
    print(df.head())

    df.to_csv("output/selected_timeseries.csv")

