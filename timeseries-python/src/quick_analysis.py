import pandas as pd
import yfinance as yf
from pypfopt import EfficientFrontier, risk_models, expected_returns
import numpy as np

# Load holdings
positions = pd.read_csv("holdings.csv")
tickers = positions["Ticker"].tolist()
weights = positions["Quantity"] / positions["Quantity"].sum()

# Download price data
data = yf.download(tickers, period="1y")["Adj Close"].dropna()

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
