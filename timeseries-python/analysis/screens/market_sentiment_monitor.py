# market_sentiment_monitor.py
"""Daily macro‑sentiment sampler

Collects a small dashboard of risk‑on / risk‑off metrics and appends one row
per run to ``output/market_sentiment_history.csv``.

Captured metrics
----------------
* S&P‑500 close and 1‑/4‑day returns
* Distance to 50‑ and 200‑day EMA
* RSI‑14 and MACD histogram
* VXST (9‑day VIX) – VIX spread
* CBOE put/call ratios (total PCR & index‑only PCR)

Run this once per trading day (e.g. cron at 08:00 local) before the U.S. cash
open.  The CSV history doubles as a back‑testable time‑series.
"""

from __future__ import annotations

import io
import sys
from pathlib import Path

import pandas as pd
import requests
import yfinance as yf
from ta.momentum import RSIIndicator
from ta.trend import EMAIndicator, MACD

###############################################################################
#  CONFIG
###############################################################################
SYMBOL_SPX   = "^GSPC"
SYMBOL_VIX   = "^VIX"
SYMBOL_VXST  = "^VIX9D"   # 9‑day VIX (VXST)
PUTCALL_ALL  = "https://cdn.cboe.com/api/global/us_indices/daily_prices/PCR_ALL.csv"
PUTCALL_IDX  = "https://cdn.cboe.com/api/global/us_indices/daily_prices/PCR_INDEX.csv"
OUT_DIR      = Path("output")
OUT_DIR.mkdir(exist_ok=True)

###############################################################################
#  HELPERS — PRICE & TECHNICALS
###############################################################################

def _download_yf_close(ticker: str, days: int = 260) -> pd.Series:
    df = yf.download(ticker, period=f"{days}d", interval="1d", progress=False)
    return df["Close"].dropna()


def spx_metrics() -> pd.Series:
    """Return a Series with SPX price & technical stats (last daily bar)."""
    px = _download_yf_close(SYMBOL_SPX)
    today = px.index[-1]

    ema50  = EMAIndicator(px, 50).ema_indicator()
    ema200 = EMAIndicator(px, 200).ema_indicator()
    rsi    = RSIIndicator(px, 14).rsi()
    macd_h = MACD(px).macd_diff()

    return pd.Series(
        {
            "spx_close":        float(px.iloc[-1]),
            "spx_ret_1d":       px.pct_change(1).iloc[-1],
            "spx_ret_4d":       px.pct_change(4).iloc[-1],
            "dist_ema50":       px.iloc[-1] / ema50.iloc[-1]  - 1,
            "dist_ema200":      px.iloc[-1] / ema200.iloc[-1] - 1,
            "rsi14":            float(rsi.iloc[-1]),
            "macd_hist":        float(macd_h.iloc[-1]),
            "as_of":            today.date(),
        }
    )

###############################################################################
#  HELPERS — VOLATILITY
###############################################################################

def vix_term_structure() -> pd.Series:
    vix  = _download_yf_close(SYMBOL_VIX, 10)
    vxst = _download_yf_close(SYMBOL_VXST, 10)
    return pd.Series(
        {
            "vix":        float(vix.iloc[-1]),
            "vxst":       float(vxst.iloc[-1]),
            "vxst_minus_vix": float(vxst.iloc[-1] - vix.iloc[-1]),
        }
    )

###############################################################################
#  HELPERS — PUT/CALL RATIO
###############################################################################

def _dl_cboe_csv(url: str) -> pd.Series:
    csv = requests.get(url, timeout=10).text
    df  = pd.read_csv(io.StringIO(csv))
    df["date"] = pd.to_datetime(df["DATE"])
    df.set_index("date", inplace=True)
    return df.iloc[-1]


def cboe_putcall() -> pd.Series:
    try:
        total_last = _dl_cboe_csv(PUTCALL_ALL)["TOTAL_PC"]
        index_last = _dl_cboe_csv(PUTCALL_IDX)["INDEX_PC"]
        return pd.Series({"putcall_total": total_last, "putcall_index": index_last})
    except Exception as exc:
        print(f"⚠️  put/call fetch failed: {exc}", file=sys.stderr)
        return pd.Series({"putcall_total": float("nan"), "putcall_index": float("nan")})

###############################################################################
#  MAIN
###############################################################################

def collect_row() -> pd.Series:
    spx = spx_metrics()
    vol = vix_term_structure()
    pcr = cboe_putcall()
    return pd.concat([spx, vol, pcr])


def run() -> None:
    row = collect_row().to_frame().T  # single‑row DataFrame

    # pretty print -------------------------------------------------------
    fmt = {
        "spx_close":        "{:.2f}".format,
        "spx_ret_1d":      "{:+.2%}".format,
        "spx_ret_4d":      "{:+.2%}".format,
        "dist_ema50":      "{:+.2%}".format,
        "dist_ema200":     "{:+.2%}".format,
        "rsi14":           "{:.1f}".format,
        "macd_hist":       "{:+.3f}".format,
        "vix":             "{:.2f}".format,
        "vxst":            "{:.2f}".format,
        "vxst_minus_vix":  "{:+.2f}".format,
        "putcall_total":   "{:.2f}".format,
        "putcall_index":   "{:.2f}".format,
    }
    print(row.to_string(index=False, formatters=fmt))

    # append to CSV ------------------------------------------------------
    csv_path = OUT_DIR / "market_sentiment_history.csv"
    header = not csv_path.exists()
    row.to_csv(csv_path, mode="a", header=header, index=False)
    print(f"✅  row appended ➜ {csv_path}")

###############################################################################
#  ENTRY‑POINT
###############################################################################
if __name__ == "__main__":
    run()
