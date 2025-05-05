import os
import requests
import pandas as pd

DATE = "Date"
PRICE = "Price"

OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)

def get_time_series(ticker, years: int = 0):
    if isinstance(ticker, dict):
        tickers = ticker.keys()
    elif isinstance(ticker, list):
        tickers = set(ticker)
    elif isinstance(ticker, set):
        tickers = ticker
    else:
        tickers = {ticker}

    dfs = []

    for ticker in tickers:
        parameters = f"ticker={ticker}"
        if years > 0:
            parameters += f"&years={years}"

        url = f"http://localhost:8091/stock/ticker?{parameters}"
        print(f"Fetching data from {url}")
        response = requests.post(url=url)
        data = response.json()

        if ticker in data:
            df = pd.DataFrame(data[ticker].items(), columns=[DATE, ticker])
            df[DATE] = pd.to_datetime(df[DATE])
            if df.empty:
                print(f"No data for {ticker}")
                continue
            df.set_index(DATE, inplace=True)
            dfs.append(df)
        else:
            print(f"{ticker} not in response")

    if not dfs:
        return pd.DataFrame()

    return pd.concat(dfs, axis=1)


def get_name_map_from_csv(positions_csv, name_field="Name", ticker_field="Symbol"):
    """
    Extracts a mapping of asset names to tickers from a portfolio CSV file.

    Args:
        positions_csv (str): Path to the CSV file containing portfolio holdings.
        name_field (str, optional): Column name for asset names. Defaults to "Name".
        ticker_field (str, optional): Column name for stock tickers. Defaults to "Symbol".

    Returns:
        dict: A mapping from asset name to ticker symbol.
    """
    positions = pd.read_csv(positions_csv)
    return dict(zip(positions[name_field], positions[ticker_field]))

def fetch_prices_for_tickers(tickers, years=10):
    """
    Fetches historical price data for a given list of ticker symbols.

    Args:
        tickers (list or set): Ticker symbols to fetch prices for.
        years (int, optional): Number of years of historical data. Defaults to 10.

    Returns:
        pd.DataFrame: Time series price data indexed by date.
    """
    prices = get_time_series(ticker=tickers, years=years).dropna()
    prices.index = pd.to_datetime(prices.index)
    return prices.sort_index()

# 🚀 Main execution
if __name__ == '__main__':
    name_map = get_name_map_from_csv("steve_positions.csv")
    tickers = set(name_map.values())
    prices = fetch_prices_for_tickers(tickers, years=10)

    print(prices)