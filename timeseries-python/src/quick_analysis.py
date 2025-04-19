import pandas as pd
from pypfopt import EfficientFrontier, risk_models, expected_returns
import numpy as np
from timeseries_api import get_time_series

MARKET_VALUE = "Market Value"
SYMBOL = "Symbol"

# Load holdings
positions = pd.read_csv("steve_positions.csv")
tickers = positions[SYMBOL].tolist()

# Download price data
data = get_time_series(ticker=tickers, years=1).dropna()

# Ensure the Date column is the index and sorted
data.index = pd.to_datetime(data.index)
data = data.sort_index()

# Get the most recent price for each ticker
most_recent_prices = data.iloc[-1]

print(most_recent_prices)

# Filter positions to only include symbols present in the data DataFrame
positions = positions[positions[SYMBOL].isin(data.columns)]

# Convert the Market Value column to float
positions[MARKET_VALUE] = positions[MARKET_VALUE].str.replace(',', '').astype(float)

weights = (positions[MARKET_VALUE] /
           positions[MARKET_VALUE].sum())

# Calculate returns and covariance
mu = expected_returns.mean_historical_return(data)
S = risk_models.sample_cov(data)

# Optimize portfolio
ef = EfficientFrontier(mu, S)
cleaned_weights = ef.max_sharpe()
ef.portfolio_performance(verbose=True)

# Calculate historical VaR at 95% confidence level
returns = data.pct_change().dropna()
portfolio_returns = returns.dot(list(cleaned_weights.values()))
var_95 = np.percentile(portfolio_returns, 5)
print(f"VaR (95%): {var_95:.2%}")
