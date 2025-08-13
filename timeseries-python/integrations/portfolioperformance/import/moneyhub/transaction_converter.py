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
from typing import Dict, Optional

import pandas as pd

STEVE_SIPP = "Steve SIPP"
ALEX_SIPP = "Alex SIPP"
JOE_SIPP = "Alex SIPP"

JOE_STOCKS_SHARES_ISA = "Joe Stocks & Shares ISA"
LUCY_STOCKS_SHARES_ISA = "Lucy Stocks & Shares ISA"
ALEX_STOCKS_SHARES_ISA = "Alex Stocks & Shares ISA"

STEVE_STOCKS_SHARES_ISA = "Steve Stocks & Shares ISA"


###############################################################################
#                               Helper functions                              #
###############################################################################


def _map_type(category: str, description: str) -> Optional[str]:
    """Return PP *Type* or None to drop the row."""
    category = (category or "").strip()
    description = (description or "").strip()
    if category == "Savings & investments income":
        return "Interest" if description.startswith("Interest From") else "Dividend"
    if category == "Interest earnings":
        return "Interest"
    if category == "Service fees & bank charges":
        return "Interest Charge"
    return None


def _extract_security(description: str, tx_type: str) -> str:
    if tx_type != "Dividend":
        return ""
    for kw in (
        "Dividend Payment",
        "Interest Payment",
        "Distribution",
        "Income",
        "Dividend",
    ):
        if kw in description:
            return description.split(kw, 1)[0].strip()
    return description.strip()


def _parse_dates(date_series: pd.Series) -> pd.Series:
    """Parse dates that may be in `dd/mm/YYYY` *or* ISO formats."""
    dates = pd.to_datetime(date_series, errors="coerce", format="%Y-%m-%d")
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
        [f"{currency} {a:.2f}" for a in sub["AMOUNT"]]
        if currency
        else [f"{a:.2f}" for a in sub["AMOUNT"]]
    )

    # Security for dividends
    sub["Security Name"] = [
        _extract_security(d, t) for d, t in zip(sub["DESCRIPTION"], sub["Type"])
    ]

    # only for dividends, replace with STOCK_NAME_MAP overrides
    is_div = sub["Type"] == "Dividend"
    sub.loc[is_div, "Security Name"] = (
        sub.loc[is_div, "Security Name"]
        .map(STOCK_NAME_MAP)
        .fillna(sub.loc[is_div, "Security Name"])
    )

    # Extra columns
    sub["Note"] = sub["DESCRIPTION"].astype(str).str.strip()
    sub["Cash Account"] = sub["ACCOUNT"].map(cash_account_map).fillna("")
    sub["Offset Account"] = sub["ACCOUNT"].map(offset_account_map).fillna("")

    cols = [
        "Date",
        "Type",
        "Value",
        "Balance",
        "Security Name",
        "Shares",
        "per share",
        "Cash Account",
        "Offset Account",
        "Note",
        "Source",
    ]
    return sub.reindex(columns=cols)


def convert_multiple_accounts(
    input_path: str | pathlib.Path,
    account_map: Dict[str, str],
    *,
    output_file: str | pathlib.Path,
    currency: str,
    exclude_dividends: bool,
    cash_account_map: Dict[str, str],
    offset_account_map: Dict[str, str],
) -> pathlib.Path:
    """
    Read the bank CSV at input_path, convert each account’s rows,
    concatenate them, and write a single CSV at output_file.
    """
    df = pd.read_csv(input_path)
    parts: list[pd.DataFrame] = []

    for account in account_map:
        try:
            part = _convert_account_df(
                df,
                account=account,
                currency=currency,
                exclude_dividends=exclude_dividends,
                cash_account_map=cash_account_map,
                offset_account_map=offset_account_map,
            )
        except ValueError as e:
            print(f"⚠ Skipping {account}: {e}")
            continue
        parts.append(part)

    if not parts:
        raise SystemExit("No data to write – all accounts were empty or filtered out.")

    combined = pd.concat(parts, ignore_index=True)
    combined.to_csv(output_file, index=False)
    print(f"✔ Combined CSV → {output_file}")
    return pathlib.Path(output_file)


###############################################################################
#                                     CLI                                    #
###############################################################################


def _build_arg_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(
        description="Bank CSV → PP cash CSVs (Cash & Offset accounts).",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    p.add_argument("INPUT", help="Source CSV file")
    p.add_argument(
        "-m",
        "--map",
        dest="maps",
        action="append",
        required=True,
        help="<input>=<output.csv>",
    )
    p.add_argument(
        "--cash-map",
        dest="cash_maps",
        action="append",
        required=True,
        help="<input>=<cash-account>",
    )
    p.add_argument(
        "--offset-map",
        dest="offset_maps",
        action="append",
        required=True,
        help="<input>=<offset-account>",
    )
    p.add_argument(
        "-c", "--currency", default="", help="Currency prefix for Value column"
    )
    p.add_argument("--exclude-dividends", action="store_true")
    return p


def _parse_pairs(pairs: list[str]) -> Dict[str, str]:
    mapping: Dict[str, str] = {}
    for p in pairs or []:
        if "=" not in p:
            raise argparse.ArgumentTypeError(
                f"Invalid mapping '{p}', expected key=value"
            )
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
        args.INPUT,
        account_map,
        output_file="AllAccounts.csv",
        currency=args.currency,
        exclude_dividends=args.exclude_dividends,
        cash_account_map=cash_map,
        offset_account_map=offset_map,
    )
    if not written:
        raise SystemExit("No files produced.")
    for a, p in written.items():
        print(f"✔ {a} → {p}")


# #############################################################################
#                                  IDE mode                                  #
# ###############################################1################################

# TODO - map dividends properly - need to check name of stock to that in the mapping file

if __name__ == "__main__":
    WORKDIR = r"C:\\Users\\steph\\Downloads"

    INPUT = "Transaction Export 26-05-2025.csv"
    ACCOUNT_MAP = {
        STEVE_SIPP: "Transactions_Steve_SIPP.csv",
        STEVE_STOCKS_SHARES_ISA: "Transactions_Steve_ISA.csv",
        LUCY_STOCKS_SHARES_ISA: "Transactions_Lucy_ISA.csv",
    }
    CASH_ACCOUNT_MAP = {
        STEVE_SIPP: "Steve SIPP Cash",
        STEVE_STOCKS_SHARES_ISA: "Steve ISA Cash",
        LUCY_STOCKS_SHARES_ISA: "Lucy ISA Cash",
        ALEX_SIPP: "Alex SIPP Cash",
        ALEX_STOCKS_SHARES_ISA: "Alex ISA Cash",
        JOE_SIPP: "Joe SIPP Cash",
        JOE_STOCKS_SHARES_ISA: "Joe ISA Cash",
    }
    OFFSET_ACCOUNT_MAP = {
        ALEX_SIPP: ALEX_SIPP + " Shares",
        JOE_SIPP: JOE_SIPP + " Shares",
        ALEX_STOCKS_SHARES_ISA: "Alex ISA Shares",
        JOE_STOCKS_SHARES_ISA: "Joe ISA Shares",
        STEVE_SIPP: STEVE_SIPP + " Shares",
        STEVE_STOCKS_SHARES_ISA: "Steve ISA Shares",
        LUCY_STOCKS_SHARES_ISA: "Lucy ISA Shares",
    }
    STOCK_NAME_MAP = {
        "AstraZeneca plc Ordinary US$0.25": "AstraZeneca PLC",
        "BioPharma Credit plc ORD USD0.01": "BioPharma Credit plc",
        "Foresight Solar Fund Ltd Ordinary NPV Overseas": "Foresight Solar Fund Ltd",
        "Global X ETFs Nasdaq 100 Covered Call UCITS ETF Dis Overseas": "Global X Nasdaq 100 Covered Call UCITS ETF D",
        "Greencoat UK Wind plc Ordinary 1p": "Greencoat UK Wind plc",
        "GSK plc ORD GBP0.3125": "GSK plc",
        "iShares III plc iShares GBP Corporate Bond Ex-Financials UCITS ETF Overseas": "iShares £ Corp Bond ex-Financials UCITS ETF GBP (Dist)",
        "Invesco Markets II Plc US Treasury Bond 7-10 Year UCITS ETF Hedge - Dist Overseas": "Invesco Markets II Plc US Treasury Bond 7-10 Year UCITS ETF Hedge - Dist",
        "iShares III Plc Global Inflatin Linked Govt Bonds UCITS ETF GBP D Overseas": "iShares Global Inflation Linked Government Bond UCITS ETF GBP Hedged (Dist)",
        "JPMorgan Emerging Europe, Middle East & Africa Sec ORD GBP0.01": "JPMorgan Emerging Europe, Middle East & Africa Sec",
        "National Grid Ord 12, 204/473p": "National Grid",
        "Unilever plc Ord 3.11p": "The Unilever Group",
        "Vanguard Funds Plc FTSE All World High Dividend Yield UCITS ETF Overseas": "Vanguard FTSE AllWld HiDivYld ETF",
    }

    CURRENCY = ""  # set "GBP" to prefix values
    EXCLUDE_DIVIDENDS = False

    if WORKDIR:
        try:
            os.chdir(WORKDIR)
            print(f"Working dir → {WORKDIR}")
        except OSError as e:
            raise SystemExit(e)

    files = convert_multiple_accounts(
        INPUT,
        OFFSET_ACCOUNT_MAP,
        currency=CURRENCY,
        output_file="AllAccounts.csv",
        exclude_dividends=EXCLUDE_DIVIDENDS,
        cash_account_map=CASH_ACCOUNT_MAP,
        offset_account_map=OFFSET_ACCOUNT_MAP,
    )
    if not files:
        raise SystemExit("No files written – check mappings.")
