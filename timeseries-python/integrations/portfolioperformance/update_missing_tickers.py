"""Utility to update FTSE All-Share mapping with tickers missing from a PortfolioPerformance XML file.

This script:
1. Uses ``ftse_tickers_missing_from_file`` to identify FTSE tickers not present in the given
   PortfolioPerformance export.
2. For each ticker not already in ``ftse_all_share_dict``, queries an LLM (OpenAI) to retrieve the
   company's official name.
3. Appends the validated results to ``api/static/ftse_all_share_dict.py``.

Example usage:
    python -m integrations.portfolioperformance.update_missing_tickers path/to/file.xml

The script expects an ``OPENAI_API_KEY`` environment variable to be set.
"""

from __future__ import annotations

import argparse
import os
from importlib import import_module
from pathlib import Path
from typing import Dict, Iterable

from openai import OpenAI

from .api.instrument_details import ftse_tickers_missing_from_file


def _fetch_company_name(client: OpenAI, ticker: str) -> str:
    """Return company name for ``ticker`` using OpenAI."""
    prompt = (
        f"Provide the official company name for the London Stock Exchange ticker '{ticker}'. "
        "Answer with only the company name."
    )
    response = client.chat.completions.create(
        model="gpt-4o-mini",
        messages=[{"role": "user", "content": prompt}],
    )
    return response.choices[0].message.content.strip()


def _write_ftse_dict(data: Dict[str, str], path: Path) -> None:
    """Write mapping ``data`` to ``path`` keeping alphabetical order."""
    with path.open("w", encoding="utf-8") as fh:
        fh.write("ftse_all_share = {\n")
        for ticker in sorted(data):
            fh.write(f'    "{ticker}": "{data[ticker]}",\n')
        fh.write("}\n")


def update_missing(xml_file: str, ftse_module_path: str) -> Dict[str, str]:
    """Identify missing tickers and append them to the FTSE dictionary."""
    missing = ftse_tickers_missing_from_file(xml_file)

    ftse_mod = import_module(ftse_module_path)
    mapping: Dict[str, str] = dict(ftse_mod.ftse_all_share)

    client = OpenAI(api_key=os.environ.get("OPENAI_API_KEY"))
    updates: Dict[str, str] = {}

    for ticker_with_suffix in missing:
        ticker = ticker_with_suffix.rstrip(".L")
        if ticker in mapping:
            continue
        try:
            name = _fetch_company_name(client, ticker)
            mapping[ticker] = name
            updates[ticker] = name
        except Exception as exc:  # pragma: no cover - network failures
            print(f"Failed to retrieve data for {ticker}: {exc}")

    if updates:
        _write_ftse_dict(mapping, Path(ftse_mod.__file__))
    return updates


def main(argv: Iterable[str] | None = None) -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("xml_file", help="PortfolioPerformance XML file")
    parser.add_argument(
        "--ftse-module",
        default="integrations.portfolioperformance.api.static.ftse_all_share_dict",
        help="Python module path of the FTSE mapping",
    )
    args = parser.parse_args(argv)

    updates = update_missing(args.xml_file, args.ftse_module)
    if updates:
        print(f"Appended {len(updates)} tickers to FTSE mapping.")
    else:
        print("No new tickers found.")


if __name__ == "__main__":  # pragma: no cover - script entry point
    main()
