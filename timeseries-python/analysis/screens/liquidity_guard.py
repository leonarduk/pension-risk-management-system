# liquidity_guard.py
"""Simple average-dollar-volume filter.
Add avg_dollar_vol_30d column to a price DataFrame or a holdings DataFrame.
"""

import pandas as pd
import yfinance as yf


def avg_dollar_volume(ticker: str, window: int = 30) -> float:
    hist = yf.download(
        ticker, period=f"{window + 5}d", interval="1d", auto_adjust=False
    )
    if hist.empty:
        return float("nan")
    dollar_vol = hist["Close"] * hist["Volume"]
    return float(dollar_vol.tail(window).mean())


def apply_liquidity(df: pd.DataFrame, min_adv: float = 5_000.0) -> pd.DataFrame:
    advs = {t: avg_dollar_volume(t) for t in df["ticker"].dropna().unique()}
    df["avg_dollar_vol_30d"] = df["ticker"].map(advs)
    return df[df["avg_dollar_vol_30d"] >= min_adv]
