#!/usr/bin/env python3
"""Refresh dividend yields for known tickers.

This utility uses :class:`DividendFetcher` from
``analysis.instrument.analyse_instrument`` to obtain the cash dividends for
each ticker over the last 12 months and combines it with the latest closing
price from Yahoo! Finance to compute a trailing dividend yield.

The results are written to ``db/dividend_yields.csv`` in the repository
root.  Existing data will be overwritten.
"""

from __future__ import annotations

import csv
import datetime as dt
from pathlib import Path

import yfinance as yf
from dateutil.relativedelta import relativedelta

from analysis.instrument.analyse_instrument import DividendFetcher, _harmonise_units


def _discover_tickers(db_dir: Path) -> list[str]:
    """Return a sorted list of Yahoo Finance tickers derived from DB files."""
    tickers: set[str] = set()
    for file in db_dir.glob("LONDON_*.csv"):
        name = file.stem.split("_", 1)[1]
        if name:
            tickers.add(f"{name}.L")
    return sorted(tickers)


def _fetch_yield(ticker: str, ccy: str = "GBX") -> float:
    """Compute the trailing twelve month dividend yield for ``ticker``.

    Parameters
    ----------
    ticker: str
        Yahoo Finance ticker symbol, e.g. ``"VOD.L"``.
    ccy: str
        Currency code used for unit harmonisation.  Defaults to GBX.
    """
    today = dt.date.today()
    window_start = today - relativedelta(months=12)

    div_series = DividendFetcher(ticker).series(window_start, today)
    div_cash = div_series.sum()

    price = yf.Ticker(ticker).history(period="1d")["Close"].iloc[-1]
    div_cash, price = _harmonise_units(div_cash, price, ccy)

    if price == 0:
        return 0.0
    return float(div_cash / price) * 100


def refresh(output_csv: Path) -> None:
    """Refresh dividend yields and write them to ``output_csv``."""
    repo_root = Path(__file__).resolve().parents[1]
    db_dir = repo_root / "db"
    tickers = _discover_tickers(db_dir)

    rows = []
    for ticker in tickers:
        try:
            yield_pct = _fetch_yield(ticker)
            rows.append({"ticker": ticker, "yield": round(yield_pct, 4)})
        except Exception as exc:  # pragma: no cover - network errors
            print(f"Skipping {ticker}: {exc}")

    output_csv.parent.mkdir(parents=True, exist_ok=True)
    with output_csv.open("w", newline="") as fh:
        writer = csv.DictWriter(fh, fieldnames=["ticker", "yield"])
        writer.writeheader()
        writer.writerows(rows)


def main() -> None:  # pragma: no cover - CLI
    repo_root = Path(__file__).resolve().parents[1]
    out_file = repo_root / "db" / "dividend_yields.csv"
    refresh(out_file)


if __name__ == "__main__":  # pragma: no cover - CLI
    main()
