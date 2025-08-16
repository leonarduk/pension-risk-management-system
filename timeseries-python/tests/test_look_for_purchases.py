import os
import sys
import runpy
import types
import pandas as pd
import pytest

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))


def _stub_environment(monkeypatch, df):
    """Create minimal stub modules so the script can run without heavy deps."""
    positions_mod = types.ModuleType("integrations.portfolioperformance.api.positions")
    positions_mod.extract_holdings_from_transactions = lambda *args, **kwargs: df

    analyse_mod = types.ModuleType("analysis.instrument.analyse_instrument")
    calls = {}

    def fake_analyze_all_tickers(**kwargs):
        calls["tickers"] = kwargs.get("tickers")

    analyse_mod.analyze_all_tickers = fake_analyze_all_tickers

    ftse_mod = types.ModuleType(
        "integrations.portfolioperformance.api.static.ftse_all_share_dict"
    )
    ftse_mod.ftse_all_share = {"AAA": "AAA", "BBB": "BBB"}

    sentiment_mod = types.ModuleType("analysis.sentiment.sentiment_timeseries")
    sentiment_mod.analyse_sentiment = lambda *args, **kwargs: None

    integrations_pkg = types.ModuleType("integrations")
    integrations_pkg.__path__ = []
    portfolio_pkg = types.ModuleType("integrations.portfolioperformance")
    portfolio_pkg.__path__ = []
    api_pkg = types.ModuleType("integrations.portfolioperformance.api")
    api_pkg.__path__ = []
    static_pkg = types.ModuleType("integrations.portfolioperformance.api.static")
    static_pkg.__path__ = []
    sentiment_pkg = types.ModuleType("analysis.sentiment")
    sentiment_pkg.__path__ = []

    monkeypatch.setitem(sys.modules, "integrations", integrations_pkg)
    monkeypatch.setitem(sys.modules, "integrations.portfolioperformance", portfolio_pkg)
    monkeypatch.setitem(sys.modules, "integrations.portfolioperformance.api", api_pkg)
    monkeypatch.setitem(
        sys.modules,
        "integrations.portfolioperformance.api.positions",
        positions_mod,
    )
    monkeypatch.setitem(
        sys.modules, "integrations.portfolioperformance.api.static", static_pkg
    )
    monkeypatch.setitem(
        sys.modules,
        "integrations.portfolioperformance.api.static.ftse_all_share_dict",
        ftse_mod,
    )
    monkeypatch.setitem(
        sys.modules, "analysis.instrument.analyse_instrument", analyse_mod
    )
    monkeypatch.setitem(sys.modules, "analysis.sentiment", sentiment_pkg)
    monkeypatch.setitem(
        sys.modules, "analysis.sentiment.sentiment_timeseries", sentiment_mod
    )
    return calls


@pytest.fixture
def purchase_env(monkeypatch):
    df = pd.DataFrame({"ticker": ["AAA.L"]})
    return _stub_environment(monkeypatch, df)


def test_look_for_purchases_calls_analyze_all_tickers(purchase_env):
    calls = purchase_env
    runpy.run_module("analysis.look_for_purchases", run_name="__main__")
    assert calls["tickers"] == [
        "CARD.L",
        "HFEL.L",
        "TFIF.L",
        "ASLI.L",
        "ERNS.L",
        "GAW.L",
        "HICL.L",
    ]


def test_look_for_purchases_handles_empty_dataset(monkeypatch):
    df = pd.DataFrame({"ticker": []})
    calls = _stub_environment(monkeypatch, df)
    runpy.run_module("analysis.look_for_purchases", run_name="__main__")
    assert "tickers" in calls


def test_look_for_purchases_missing_ticker_column(monkeypatch):
    df = pd.DataFrame({"not_ticker": []})
    _stub_environment(monkeypatch, df)
    with pytest.raises(KeyError):
        runpy.run_module("analysis.look_for_purchases", run_name="__main__")
