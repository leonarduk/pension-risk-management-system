# timeseries_python/dividends.py
from __future__ import annotations

import datetime as dt
from dataclasses import dataclass
from typing import Optional

import pandas as pd
import yfinance as yf                 # public domain, BSD-style licence
from dateutil.relativedelta import relativedelta

__all__ = ["DividendFetcher", "last_12m_dividend_yield"]

from analysis.instrument.analyse_instrument import get_price_series


@dataclass(slots=True, frozen=True)
class DividendFetcher:
    """
    Thin wrapper around yfinance that returns a clean pandas Series of cash
    dividends (GBp for LSE symbols).  Nothing else is pulled – price lives in
    the existing price utilities already inside *timeseries-python*.
    """
    ticker: str

    def series(
        self,
        start: Optional[dt.date] = None,
        end: Optional[dt.date] = None,
    ) -> pd.Series:
        today = dt.date.today()
        start = start or today - relativedelta(years=5)
        end = end or today

        divs = yf.Ticker(self.ticker).dividends

        # ① strip the timezone so the index becomes tz-naïve
        divs.index = divs.index.tz_localize(None)

        # ② now the slice works
        return divs.loc[start:end].rename("div_cash")


# ---------- public one-liner -------------------------------------------------
def last_12m_dividend_yield(
    ticker: str,
    today: dt.date | None = None,
    *,
    use_stockfeed: bool = True,
    xml_path: str | None = None,
) -> float:
    today = today or dt.date.today()
    window_start = today - relativedelta(months=12)

    # 1) dividends ----------------------------------------------------------
    div_cash = DividendFetcher(ticker).series(window_start, today).sum()

    # 2) prices -------------------------------------------------------------
    #    pull a *range* so we’re sure to get at least one row
    prices = get_price_series(
        ticker,
        window_start,
        today,
        use_stockfeed=use_stockfeed,
        xml_path=xml_path,
    ).dropna()

    if prices.empty:
        raise ValueError(f"No price data in {window_start:%Y-%m-%d}:{today}")

    px = prices.iloc[-1]          # last valid close *on or before* today
    return float(div_cash / px)

if __name__ == '__main__':
    xml_path = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    print(last_12m_dividend_yield(ticker="FSFL.L",use_stockfeed=False, xml_path=xml_path))
