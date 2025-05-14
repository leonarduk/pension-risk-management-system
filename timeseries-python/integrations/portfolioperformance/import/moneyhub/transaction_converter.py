#!/usr/bin/env python3
"""sipp_transaction_converter.py – Cash CSV maker
================================================
Convert a bank/export CSV into **Portfolio Performance** cash CSVs (one per
account).

Columns produced
----------------
* **Date** – formatted `YYYY‑MM‑DD 00:00:00`
* **Type** – *Interest*, *Dividend*, or *Interest Charge*
* **Value** – monetary amount (optional currency prefix)
* **Cash Account** – from `CASH_ACCOUNT_MAP`
* **Offset Account** – from `OFFSET_ACCOUNT_MAP`
* **Note** – original `DESCRIPTION`
* (Balance, Security, Shares, per share, Source kept for PP compatibility)

Features
~~~~~~~~
* Dividend rows can be excluded (`--exclude-dividends` / `EXCLUDE_DIVIDENDS`).
* Robust date parsing (`dd/mm/YYYY`, ISO, etc.).
* Dual CLI/IDE usage with a working‑directory helper.
"""
from __future__ import annotations

import argparse
import os
import pathlib
import sys
import textwrap
from typing import Dict, Optional

import pandas as pd

###############################################################################
#                               Helper functions                              #
###############################################################################

def _map_type(category: str, description: str) -> Optional[str]:
    """Return PP *Type* or None to drop the row."""
    category = (category or "").strip()
    description = (description or "").strip()
    if category == "Savings & investments income":
        return "Interest" if description.startswith("Interest From") else "Dividend"
    if category == "Service fees & bank charges":
        return "Interest Charge"
    return None


def _extract_security(description: str, tx_type: str) -> str:
    if tx_type != "Dividend":
        return ""
    for kw in ("Dividend Payment", "Interest Payment", "Distribution", "Income", "Dividend"):
        if kw in description:
            return description.split(kw, 1)[0].strip()
    return description.strip()


def _parse_dates(date_series: pd.Series) -> pd.Series:
    """Parse dates that may be in `dd/mm/YYYY` *or* ISO formats."""
    dates = pd.to_datetime(date_series, errors="coerce", dayfirst=True)
    na_mask = dates.isna()
    if na_mask.any():
        dates.loc[na_mask] = pd.to_datetime(date_series[na_mask], errors="coerce", format="%Y-%m-%d")
    return dates

###############################################################################
#                               Core converter                                #
###############################################################################

def _convert_account_df(
    df: pd.DataFrame,
    *,
    account: str,
    currency: str,
    exclude_dividends: bool,
    cash_account_map: Dict[str, str],
    offset_account_map: Dict[str, str],
) -> pd.DataFrame:
    sub = df[df["ACCOUNT"] == account].copy()
    if sub.empty:
        raise ValueError(f"No rows for account '{account}'.")

    # Map PP types and filter
    sub["Type"] = [_map_type(c, d) for c, d in zip(sub["CATEGORY"], sub["DESCRIPTION"])]
    sub = sub[sub["Type"].notna()].copy()
    if exclude_dividends:
        sub = sub[sub["Type"] != "Dividend"].copy()
    if sub.empty:
        raise ValueError(f"All rows for '{account}' filtered out.")

    # Date parsing
    dates = _parse_dates(sub["DATE"])
    bad = dates.isna()
    if bad.any():
        raise ValueError(
            f"Un‑parseable DATE values for account '{account}': {sub.loc[bad, 'DATE'].unique()[:5]}"
        )
    sub["Date"] = dates.dt.strftime("%Y-%m-%d 00:00:00")

    # Value column
    sub["Value"] = (
        [f"{currency} {a:.2f}" for a in sub["AMOUNT"]] if currency else [f"{a:.2f}" for a in sub["AMOUNT"]]
    )

    # Security for dividends
    sub["Security"] = [_extract_security(d, t) for d, t in zip(sub["DESCRIPTION"], sub["Type"])]

    # Extra columns
    sub["Note"] = sub["DESCRIPTION"].astype(str).str.strip()
    sub["Cash Account"] = sub["ACCOUNT"].map(cash_account_map).fillna("")
    sub["Offset Account"] = sub["ACCOUNT"].map(offset_account_map).fillna("")

    cols = [
        "Date", "Type", "Value", "Balance", "Security", "Shares", "per share",
        "Cash Account", "Offset Account", "Note", "Source",
    ]
    return sub.reindex(columns=cols)


def convert_multiple_accounts(
    input_path: str | pathlib.Path,
    account_map: Dict[str, str],
    *,
    currency: str,
    exclude_dividends: bool,
    cash_account_map: Dict[str, str],
    offset_account_map: Dict[str, str],
) -> Dict[str, pathlib.Path]:
    df = pd.read_csv(input_path)
    written: Dict[str, pathlib.Path] = {}
    for acc, out_file in account_map.items():
        try:
            out_df = _convert_account_df(
                df,
                account=acc,
                currency=currency,
                exclude_dividends=exclude_dividends,
                cash_account_map=cash_account_map,
                offset_account_map=offset_account_map,
            )
        except ValueError as e:
            print(f"⚠ {e}")
            continue
        out_df.to_csv(out_file, index=False)
        written[acc] = pathlib.Path(out_file)
    return written

###############################################################################
#                                     CLI                                    #
###############################################################################

def _build_arg_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Bank CSV → PP cash CSVs (Cash & Offset accounts).",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    p.add_argument("INPUT", help="Source CSV file")
    p.add_argument("-m", "--map", dest="maps", action="append", required=True,
                   help="<input>=<output.csv>")
    p.add_argument("--cash-map", dest="cash_maps", action="append", required=True,
                   help="<input>=<cash-account>")
    p.add_argument("--offset-map", dest="offset_maps", action="append", required=True,
                   help="<input>=<offset-account>")
    p.add_argument("-c", "--currency", default="", help="Currency prefix for Value column")
    p.add_argument("--exclude-dividends", action="store_true")
    return p


def _parse_pairs(pairs: list[str]) -> Dict[str, str]:
    mapping: Dict[str, str] = {}
    for p in pairs or []:
        if "=" not in p:
            raise argparse.ArgumentTypeError(f"Invalid mapping '{p}', expected key=value")
        k, v = (s.strip() for s in p.split("=", 1))
        if not k or not v:
            raise argparse.ArgumentTypeError(f"Invalid mapping '{p}', empty side")
        mapping[k] = v
    return mapping


def main(argv: list[str] | None = None) -> None:
    ap = _build_arg_parser()
    args = ap.parse_args(argv)

    account_map = _parse_pairs(args.maps)
    cash_map = _parse_pairs(args.cash_maps)
    offset_map = _parse_pairs(args.offset_maps)

    written = convert_multiple_accounts(
        args.INPUT, account_map,
        currency=args.currency,
        exclude_dividends=args.exclude_dividends,
        cash_account_map=cash_map,
        offset_account_map=offset_map,
    )
    if not written:
        raise SystemExit("No files produced.")
    for a, p in written.items():
        print(f"✔ {a} → {p}")

###############################################################################
#                                  IDE mode                                  #
###############################################################################

# TODO - map dividends properly - need to check name of stock to that in the mapping file

if __name__ == "__main__":
    WORKDIR = r"C:\\Users\\steph\\Downloads"

    INPUT = "Transaction Export 13-05-2025 (1).csv"
    ACCOUNT_MAP = {
        "Steve SIPP": "Transactions_Steve_SIPP.csv",
        "Stocks & Shares ISA": "Transactions_Steve_ISA.csv",
        "Lucy Stocks & Shares ISA": "Transactions_Lucy_ISA.csv",
    }
    CASH_ACCOUNT_MAP = {
        "Steve SIPP": "Steve SIPP Cash",
        "Stocks & Shares ISA": "Steve ISA Cash",
        "Lucy Stocks & Shares ISA": "Lucy ISA Cash",
    }
    OFFSET_ACCOUNT_MAP = {
        "Steve SIPP": "Steve SIPP",
        "Stocks & Shares ISA": "Steve ISA",
        "Lucy Stocks & Shares ISA": "Lucy ISA",
    }
    CURRENCY = ""  # set "GBP" to prefix values
    EXCLUDE_DIVIDENDS = False

    if WORKDIR:
        try:
            os.chdir(WORKDIR)
            print(f"Working dir → {WORKDIR}")
        except OSError as e:
            raise SystemExit(e)

    if len(sys.argv) > 1:
        main(sys.argv[1:])
    else:
        files = convert_multiple_accounts(
            INPUT, ACCOUNT_MAP,
            currency=CURRENCY,
            exclude_dividends=EXCLUDE_DIVIDENDS,
            cash_account_map=CASH_ACCOUNT_MAP,
            offset_account_map=OFFSET_ACCOUNT_MAP,
        )
        if not files:
            raise SystemExit("No files written – check mappings.")
        for a, p in files.items():
            print(f"✔ {a} → {p}")
