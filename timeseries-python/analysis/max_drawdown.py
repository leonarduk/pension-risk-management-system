from __future__ import annotations

from typing import Iterable, Union

import numpy as np
import pandas as pd

from .portfolio.timeseries_api import get_time_series


def max_drawdown(ticker: Union[str, Iterable[str]], years: int = 5) -> pd.DataFrame:
    """Calculate the maximum drawdown for each ticker.

    Args:
        ticker: Single ticker or iterable of tickers.
        years: Number of years of price history to request.

    Returns:
        DataFrame with columns ``ticker`` and ``max_drawdown`` (as a positive
        fraction).
    """

    tickers = [ticker] if isinstance(ticker, str) else list(ticker)
    prices = get_time_series(ticker=tickers, years=years)
    if prices.empty:
        return pd.DataFrame(columns=["ticker", "max_drawdown"])

    drawdowns: dict[str, float] = {}
    for t in tickers:
        series = prices[t].dropna()
        if series.empty:
            drawdowns[t] = np.nan
            continue
        running_max = series.cummax()
        drawdown_series = series / running_max - 1
        drawdowns[t] = abs(drawdown_series.min())

    return pd.DataFrame({"ticker": list(drawdowns.keys()), "max_drawdown": list(drawdowns.values())})
