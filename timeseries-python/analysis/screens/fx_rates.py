# fx_rates.py
"""Lightweight FX rate fetcher with caching.
Uses FRED series (DEXUSUK for GBP/USD, DEXUSEU for GBP/EUR) but falls back
to yfinance if FRED API key not set.

Example:
    from fx_rates import convert
    gbp_price = convert(100, from_ccy='USD', to_ccy='GBP')
"""

import os
from functools import lru_cache
from datetime import date
from typing import Literal

Pair = Literal["GBP/USD", "GBP/EUR"]

try:
    from fredapi import Fred

    _fred = Fred(api_key=os.getenv("FRED_API_KEY"))
except ImportError:
    _fred = None


@lru_cache(maxsize=365)
def _rate(pair: Pair, as_of: date | None = None) -> float:
    as_of = as_of or date.today()
    series_map = {"GBP/USD": "DEXUSUK", "GBP/EUR": "DEXUSEU"}
    code = series_map[pair]
    if _fred:
        try:
            val = _fred.get_series(code).loc[str(as_of)]
            if val:
                return float(val)
        except Exception:
            pass

    # --- fallback to yfinance
    import yfinance as yf

    yf_symbol = {"GBP/USD": "GBPUSD=X", "GBP/EUR": "GBPEUR=X"}[pair]
    hist = yf.download(yf_symbol, period="5d")
    return float(hist["Close"].dropna().iloc[-1])


def convert(
    amount: float, *, from_ccy: str, to_ccy: str, as_of: date | None = None
) -> float:
    from_ccy = from_ccy.upper()
    to_ccy = to_ccy.upper()
    if from_ccy == to_ccy:
        return amount
    if {from_ccy, to_ccy} == {"GBP", "USD"}:
        rate = _rate("GBP/USD", as_of)
        return amount / rate if from_ccy == "USD" else amount * rate
    if {from_ccy, to_ccy} == {"GBP", "EUR"}:
        rate = _rate("GBP/EUR", as_of)
        return amount / rate if from_ccy == "EUR" else amount * rate
    raise ValueError(f"Unsupported conversion {from_ccy}->{to_ccy}")
