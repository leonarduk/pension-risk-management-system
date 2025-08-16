import os
import sys
import runpy
import types
import pandas as pd
import pytest

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))


def _stub_sales_env(monkeypatch, df):
    positions_mod = types.ModuleType("integrations.portfolioperformance.api.positions")
    positions_mod.extract_holdings_from_transactions = lambda *args, **kwargs: df

    analyse_mod = types.ModuleType("analysis.instrument.analyse_instrument")
    calls = {}

    def fake_analyze_all_tickers(**kwargs):
        calls["tickers"] = kwargs.get("tickers")

    analyse_mod.analyze_all_tickers = fake_analyze_all_tickers

    integrations_pkg = types.ModuleType("integrations")
    integrations_pkg.__path__ = []
    portfolio_pkg = types.ModuleType("integrations.portfolioperformance")
    portfolio_pkg.__path__ = []
    api_pkg = types.ModuleType("integrations.portfolioperformance.api")
    api_pkg.__path__ = []

    monkeypatch.setitem(sys.modules, "integrations", integrations_pkg)
    monkeypatch.setitem(sys.modules, "integrations.portfolioperformance", portfolio_pkg)
    monkeypatch.setitem(sys.modules, "integrations.portfolioperformance.api", api_pkg)
    monkeypatch.setitem(
        sys.modules,
        "integrations.portfolioperformance.api.positions",
        positions_mod,
    )
    monkeypatch.setitem(
        sys.modules, "analysis.instrument.analyse_instrument", analyse_mod
    )
    return calls


@pytest.fixture
def sales_env(monkeypatch):
    df = pd.DataFrame({"ticker": ["AAA.L", "BBB.L"]})
    return _stub_sales_env(monkeypatch, df)


def test_look_for_sales_passes_holdings_to_analyze_all_tickers(sales_env):
    calls = sales_env
    runpy.run_module("analysis.look_for_sales", run_name="__main__")
    assert calls["tickers"] == ["AAA.L", "BBB.L"]


def test_look_for_sales_handles_empty_dataset(monkeypatch):
    df = pd.DataFrame({"ticker": []})
    calls = _stub_sales_env(monkeypatch, df)
    runpy.run_module("analysis.look_for_sales", run_name="__main__")
    assert calls["tickers"] == []


def test_look_for_sales_missing_ticker_column(monkeypatch):
    df = pd.DataFrame({"other": []})
    _stub_sales_env(monkeypatch, df)
    with pytest.raises(KeyError):
        runpy.run_module("analysis.look_for_sales", run_name="__main__")
