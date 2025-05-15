# risk_trails.py
"""ATR‑based trailing stop and time exit helper."""

import pandas as pd
import yfinance as yf
from ta.volatility import AverageTrueRange
from datetime import timedelta

def atr_stop(ticker: str, entry_price: float, entry_date: pd.Timestamp,
             multiple: float = 2.0, lookback: int = 14):
    data = yf.download(ticker, period='6mo', interval='1d')
    if data.empty:
        return None
    atr = AverageTrueRange(data['High'], data['Low'], data['Close'], window=lookback).average_true_range()
    atr_on_entry = float(atr.loc[:entry_date].iloc[-1])
    return entry_price - multiple * atr_on_entry  # long position

def time_exit(entry_date: pd.Timestamp, max_hold_days: int = 60):
    return entry_date + timedelta(days=max_hold_days)
