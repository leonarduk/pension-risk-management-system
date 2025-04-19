import requests
import pandas as pd

ticker = "NESF"
response = requests.get(f"http://localhost:8091/stock/ticker/{ticker}/json")
data = response.json()

# Extract the data for the dynamic ticker key
if ticker in data:
    df = pd.DataFrame(data[ticker].items(), columns=["Date", "Price"])
    df["Date"] = pd.to_datetime(df["Date"])
    df.set_index("Date", inplace=True)
    print(df)
else:
    print(f"Ticker {ticker} not found in the response.")