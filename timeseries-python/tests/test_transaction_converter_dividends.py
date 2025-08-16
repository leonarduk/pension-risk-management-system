import os
import sys

import pandas as pd
import importlib

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
module = importlib.import_module(
    "integrations.portfolioperformance.import.moneyhub.transaction_converter"
)
_convert_account_df = module._convert_account_df


def _make_df(rows):
    return pd.DataFrame(rows)


def test_dividend_mapping_mapped():
    df = _make_df(
        {
            "ACCOUNT": ["Steve SIPP"],
            "CATEGORY": ["Savings & investments income"],
            "DESCRIPTION": ["AstraZeneca plc Ordinary US$0.25 Dividend Payment"],
            "DATE": ["2024-01-01"],
            "AMOUNT": [10.0],
        }
    )
    result = _convert_account_df(
        df,
        account="Steve SIPP",
        currency="",
        exclude_dividends=False,
        cash_account_map={"Steve SIPP": "Steve SIPP Cash"},
        offset_account_map={"Steve SIPP": "Steve SIPP Shares"},
    )
    assert result.iloc[0]["Security Name"] == "AstraZeneca PLC"
    assert result.iloc[0]["Cash Account"] == "Steve SIPP Cash"
    assert result.iloc[0]["Offset Account"] == "Steve SIPP Shares"


def test_dividend_mapping_unmapped_dropped():
    df = _make_df(
        {
            "ACCOUNT": ["Steve SIPP", "Steve SIPP"],
            "CATEGORY": [
                "Savings & investments income",
                "Savings & investments income",
            ],
            "DESCRIPTION": [
                "AstraZeneca plc Ordinary US$0.25 Dividend Payment",
                "Unknown Corp Dividend Payment",
            ],
            "DATE": ["2024-01-01", "2024-02-01"],
            "AMOUNT": [10.0, 5.0],
        }
    )
    result = _convert_account_df(
        df,
        account="Steve SIPP",
        currency="",
        exclude_dividends=False,
        cash_account_map={"Steve SIPP": "Steve SIPP Cash"},
        offset_account_map={"Steve SIPP": "Steve SIPP Shares"},
    )
    # Only the mapped dividend should remain
    assert list(result["Security Name"]) == ["AstraZeneca PLC"]
