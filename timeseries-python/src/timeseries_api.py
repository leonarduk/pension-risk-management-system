import requests
import pandas as pd

# Constants for the API response
DATE = "Date"
PRICE = "Price"


# This script fetches time series data for a given stock ticker from a local API
def get_time_series(ticker, years: int = 0):
    tickers = set()

    if ticker is dict:
        # If ticker is a dictionary, convert it to a comma-separated string
        tickers = ticker.keys()
    elif isinstance(ticker, list):
        # If ticker is a list, convert it to a comma-separated string
        tickers = set(ticker)
    elif isinstance(ticker, set):
        # If ticker is a list, convert it to a comma-separated string
        tickers = ticker

    dfs = []
    for ticker in tickers:
        parameters = f"ticker={ticker}"
        if years > 0:
            parameters += f"&years={years}"
        # Make a POST request to the API with the ticker and parameters
        url = f"http://localhost:8091/stock/ticker?{parameters}"
        print(f"Fetching data from {url}")
        response = requests.post(url=url)
        data = response.json()

    # Convert the dictionary into a DataFrame
        if ticker in data:
            df = pd.DataFrame(data[ticker].items(), columns=[DATE, ticker])
            df[DATE] = pd.to_datetime(df[DATE])
            if df.empty:
                print(f"No data found for ticker {ticker}.")
                continue

            df.set_index(DATE, inplace=True)
            # print(df)
            dfs.append(df)
        else:
            print(f"Ticker {ticker} not found in the response.")

    # Merge all DataFrames on the Date index
    if not dfs:
        print("No data found for the provided tickers.")
        return pd.DataFrame()

    # Concatenate all DataFrames along the columns
    output = pd.concat(dfs, axis=1)
    return output


if __name__ == '__main__':
    # Example usage
    result = get_time_series(ticker=["JPM"], years=1)

    # Get the time series data for the ticker "NESF" for the last 1 year
    result = get_time_series(ticker=["NESF", "GRG"], years=1)

    print(result)
