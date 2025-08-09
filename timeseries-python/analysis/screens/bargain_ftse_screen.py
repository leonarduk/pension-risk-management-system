# bargain_ftse_screen.py
"""Run an end‑to‑end *cheap‑but‑rising* screen.

Pipeline
--------
1. **Rebuild positions** (units) from a PortfolioPerformance XML export.
2. **Fetch latest closes** for each ticker via your own ``get_time_series``
   (either StockFeed or the PP price history).
3. **Enrich holdings** with:
   • Fundamental snap‑shots (FCF yield, ROIC via Yahoo Finance)
   • Technical metrics (RSI 14, MACD histogram, 20‑day breakout flag)
   • Liquidity guard (30‑day average £‑ADV)
   • FX conversion so USD/EUR names value correctly in GBP.
4. **Position sizing** using a half‑Kelly throttle and ATR‑based stop prices.
5. **Outputs** two CSVs in ./output/
   • *all_positions_valued.csv* – every holding with all metrics
   • *bargain_candidates.csv*  – stocks passing the cheap‑plus‑momentum filter

Run
---
```bash
python bargain_ftse_screen.py path/to/portfolio.xml --stockfeed
```
Omit ``--stockfeed`` to use PP prices.  Tune the **THRESHOLDS** dict to your
own values.
"""
from __future__ import annotations

import argparse
import sys
from collections import defaultdict
from datetime import datetime, date
from pathlib import Path
import xml.etree.ElementTree as ET

import pandas as pd
import yfinance as yf
from ta.momentum import RSIIndicator
from ta.trend import EMAIndicator, MACD, SMAIndicator
from ta.volatility import AverageTrueRange

# ── project‑local imports ----------------------------------------------------
from analysis.instrument.analyse_instrument import get_time_series
from integrations.portfolioperformance.api.instrument_details import extract_instrument

# optional plug‑ins (make sure they’re on PYTHONPATH)
from fx_rates import convert  # FX converter
import liquidity_guard as lg  # ADV filter
from size import kelly  # position‑sizer
from risk_trails import atr_stop  # stop calculation
from breakout_utils import twenty_day_breakout  # 20‑day‑high flag

###############################################################################
#  CONFIG / THRESHOLDS
###############################################################################
SHARE_SCALE = 10**8
GBP_PENCE = "GBX"
GBP_POUNDS = "GBP"

THRESHOLDS = {
    "fcf_yield_min": 0.08,  # 8 % cheap hurdle
    "roic_min": 0.12,  # 12 % quality hurdle
    "rsi_max": 40,  # not overbought
    "macd_hist_min": 0,  # momentum turning up
    "breakout_vol_ratio": 1.25,  # 25 % volume surge on breakout
    "min_adv_gbp": 10_000,  # liquidity floor (£)
}

###############################################################################
# 1)  Extract current holdings from PP XML
###############################################################################


def extract_holdings(xml_file: str, *, cutoff: date | None = None) -> pd.DataFrame:
    """Re‑create unit counts from <portfolio-transaction> records."""

    tree = ET.parse(xml_file)
    root = tree.getroot()

    # build security meta
    meta = {}
    for s in root.findall(".//securities/security"):
        sid = s.attrib.get("id") or s.findtext("uuid")
        if not sid:
            continue
        meta[sid] = {
            "name": s.findtext("name", ""),
            "isin": s.findtext("isin", ""),
            "ticker": s.findtext("tickerSymbol", ""),
            "currency": s.findtext("currencyCode", "UNKNOWN"),
        }

    ledger = defaultdict(float)
    for ptx in root.findall(".//portfolio-transaction"):
        if cutoff:
            dt = datetime.fromisoformat(ptx.findtext("date").split("T")[0]).date()
            if dt > cutoff:
                continue
        ttype = ptx.findtext("type", "").upper()
        sign = {
            "BUY": 1,
            "TRANSFER_IN": 1,
            "SELL": -1,
            "TRANSFER_OUT": -1,
            "REMOVAL": -1,
        }.get(ttype)
        if sign is None:
            continue
        raw = ptx.findtext("shares") or ptx.findtext("units")
        if not raw:
            continue
        qty = float(raw) / SHARE_SCALE * sign
        sid = ptx.find("security").attrib.get("reference")
        ledger[sid] += qty

    rows = []
    for sid, qty in ledger.items():
        if abs(qty) < 1e-9:
            continue
        m = meta.get(sid, {})
        rows.append(
            {
                "securityId": sid,
                "ticker": m.get("ticker", ""),
                "name": m.get("name", ""),
                "isin": m.get("isin", ""),
                "currency": m.get("currency", "UNKNOWN"),
                "quantity": qty,
            }
        )
    return pd.DataFrame(rows)


###############################################################################
# 2)  Latest close helper + technicals
###############################################################################


def latest_close(ticker: str, *, xml_path: str, use_stockfeed: bool) -> float | None:
    try:
        ts = get_time_series(
            ticker=ticker, use_stockfeed=use_stockfeed, xml_path=xml_path
        )
        if ts.empty:
            return None
        if isinstance(ts, pd.Series):
            close = ts.dropna()
        else:
            col = ticker if ticker in ts.columns else ts.columns[0]
            close = ts[col].dropna()
        return float(close.iloc[-1])
    except Exception:
        return None


def technical_metrics(close: pd.Series) -> dict[str, float]:
    if len(close) < 100:
        return {"rsi": float("nan"), "macd_hist": float("nan")}
    rsi = RSIIndicator(close, 14).rsi().iloc[-1]
    macd_hist = MACD(close).macd_diff().iloc[-1]
    return {"rsi": rsi, "macd_hist": macd_hist}


###############################################################################
# 3)  Quick‑n‑dirty fundamentals from Yahoo
###############################################################################


def fundamentals(ticker: str) -> dict[str, float]:
    try:
        info = yf.Ticker(ticker).get_financials_fundamentals()
        fcf = info.get("FreeCashFlowTTM")
        mcap = info.get("MarketCapitalisation")
        roic = info.get("ReturnOnInvestedCapitalTTM")
        return {
            "fcf": fcf or float("nan"),
            "market_cap": mcap or float("nan"),
            "fcf_yield": (fcf / mcap) if fcf and mcap else float("nan"),
            "roic": roic or float("nan"),
        }
    except Exception:
        return {
            "fcf": float("nan"),
            "market_cap": float("nan"),
            "fcf_yield": float("nan"),
            "roic": float("nan"),
        }


###############################################################################
# 4)  Main pipeline
###############################################################################


def run(xml_path: str, *, cutoff: date | None, use_stockfeed: bool):
    holdings = extract_holdings(xml_path, cutoff=cutoff)

    # ----- enrich loop ---------------------------------------------------
    extra_rows = []
    for _, row in holdings.iterrows():
        tkr = row["ticker"]
        price = (
            latest_close(tkr, xml_path=xml_path, use_stockfeed=use_stockfeed)
            if tkr
            else float("nan")
        )

        # FX convert to GBP
        ccy = row["currency"] or "GBP"

        if ccy.upper() in ("GBX", "GBP", "GBXP"):
            price_gbp = price / 100  # convert pence → pounds
        else:
            price_gbp = (
                convert(price, from_ccy=ccy, to_ccy="GBP")
                if price == price
                else float("nan")
            )

        # Technicals + breakout
        ts = (
            get_time_series(tkr, use_stockfeed, xml_path)
            if tkr
            else pd.Series(dtype=float)
        )
        if not ts.empty and not isinstance(ts, pd.Series):
            ts = ts[tkr if tkr in ts.columns else ts.columns[0]]
        tech = (
            technical_metrics(ts)
            if not ts.empty
            else {"rsi": float("nan"), "macd_hist": float("nan")}
        )
        breakout = twenty_day_breakout(ts) if not ts.empty else pd.Series()

        # Fundamentals
        fund = fundamentals(tkr) if tkr else {}

        # Volatility for sizing & stop
        vol20 = ts.pct_change().std() * (252**0.5) if not ts.empty else float("nan")
        stop = (
            atr_stop(tkr, price_gbp, pd.Timestamp.today())
            if price == price
            else float("nan")
        )
        weight = (
            kelly(fund.get("fcf_yield", float("nan")) / 252, vol20)
            if vol20 == vol20
            else float("nan")
        )

        extra_rows.append(
            {
                "price": price_gbp,
                "marketValue": price_gbp * row["quantity"],
                "rsi": tech["rsi"],
                "macd_hist": tech["macd_hist"],
                **breakout.to_dict(),
                **fund,
                "vol_20d": vol20,
                "stop_price": stop,
                "target_w": weight,
            }
        )

    df = pd.concat([holdings.reset_index(drop=True), pd.DataFrame(extra_rows)], axis=1)

    # ----- liquidity filter ----------------------------------------------
    df = lg.apply_liquidity(df, THRESHOLDS["min_adv_gbp"])

    # ----- bargain screen -------------------------------------------------
    bargain = df[
        (df["fcf_yield"] > THRESHOLDS["fcf_yield_min"])
        & (df["roic"] > THRESHOLDS["roic_min"])
        & (df["rsi"] < THRESHOLDS["rsi_max"])
        & (df["macd_hist"] > THRESHOLDS["macd_hist_min"])
        & (df["is_20d_high"])
        & (df["vol_vs_avg20"] > THRESHOLDS["breakout_vol_ratio"])
    ].sort_values("fcf_yield", ascending=False)

    out = Path("output")
    out.mkdir(exist_ok=True)
    df.to_csv(out / "all_positions_valued.csv", index=False)
    bargain.to_csv(out / "bargain_candidates.csv", index=False)

    print(f"✅ wrote {len(df)} full rows ➜ output/all_positions_valued.csv")
    print(f"✅ wrote {len(bargain)} bargain rows ➜ output/bargain_candidates.csv")


###############################################################################
#  CLI
###############################################################################
if __name__ == "__main__":
    # ───── Direct‑launch defaults (edit to taste) ─────
    DEFAULT_XML = (
        "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id-updated.xml"
    )

    DEFAULT_STOCKFDB = False  # set False to use PP prices instead
    DEFAULT_CUTOFF = None  # e.g. date(2025, 5, 1)

    if len(sys.argv) == 1:
        # no CLI args ➜ run with defaults
        run(DEFAULT_XML, cutoff=DEFAULT_CUTOFF, use_stockfeed=DEFAULT_STOCKFDB)
    else:
        import argparse

        p = argparse.ArgumentParser(description="Value+Momentum screener")
        p.add_argument("xml", type=Path)
        p.add_argument("--stockfeed", action="store_true")
        p.add_argument("--cutoff", type=str)
        a = p.parse_args()
        cutoff_dt = datetime.strptime(a.cutoff, "%Y-%m-%d").date() if a.cutoff else None
        run(str(a.xml), cutoff=cutoff_dt, use_stockfeed=a.stockfeed)
