from __future__ import annotations

from typing import Iterable, Union

import numpy as np
import pandas as pd

from .portfolio.timeseries_api import get_time_series

TRADING_DAYS = 252


def risk_return(ticker: Union[str, Iterable[str]], years: int = 5) -> pd.DataFrame:
    """Calculate annualised return and risk for each ticker.

    Args:
        ticker: Single ticker or iterable of tickers.
        years: Number of years of price history to request.

    Returns:
        DataFrame with columns ``ticker``, ``annual_return`` and ``annual_std``.
    """

    tickers = [ticker] if isinstance(ticker, str) else list(ticker)
    prices = get_time_series(ticker=tickers, years=years)
    if prices.empty:
        return pd.DataFrame(columns=["ticker", "annual_return", "annual_std"])

    daily_returns = prices.pct_change().dropna()
    annual_return = (1 + daily_returns.mean()) ** TRADING_DAYS - 1
    annual_std = daily_returns.std() * np.sqrt(TRADING_DAYS)

    return pd.DataFrame(
        {
            "ticker": annual_return.index,
            "annual_return": annual_return.values,
            "annual_std": annual_std.values,
        }
    )
