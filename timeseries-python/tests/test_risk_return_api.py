import os
import sys
import pandas as pd
import pytest

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

import analysis.risk_return_api as api  # noqa: E402


@pytest.fixture
def client(monkeypatch):
    def fake_risk_return(tickers, confidence_level=0.95):
        return pd.DataFrame(
            {
                "ticker": tickers,
                "annual_return": [0.1] * len(tickers),
                "annual_std": [0.2] * len(tickers),
            }
        )

    def fake_max_drawdown(tickers):
        return pd.DataFrame({"ticker": tickers, "max_drawdown": [-0.3] * len(tickers)})

    monkeypatch.setattr(api, "risk_return", fake_risk_return)
    monkeypatch.setattr(api, "max_drawdown", fake_max_drawdown)
    api.app.config["TESTING"] = False
    return api.app.test_client()


def test_risk_return_endpoint_returns_data(client):
    resp = client.get("/analytics/risk-return?tickers=AAA,BBB&confidenceLevel=0.9")
    assert resp.status_code == 200
    assert resp.get_json() == [
        {"ticker": "AAA", "annual_return": 0.1, "annual_std": 0.2},
        {"ticker": "BBB", "annual_return": 0.1, "annual_std": 0.2},
    ]


def test_risk_return_endpoint_handles_missing_tickers(client):
    resp = client.get("/analytics/risk-return")
    assert resp.status_code == 200
    assert resp.get_json() == []


def test_risk_return_endpoint_invalid_confidence(client):
    resp = client.get("/analytics/risk-return?tickers=AAA&confidenceLevel=bad")
    assert resp.status_code == 500


def test_max_drawdown_endpoint(client):
    resp = client.get("/analytics/max-drawdown?tickers=AAA,BBB")
    assert resp.status_code == 200
    assert resp.get_json() == [
        {"ticker": "AAA", "max_drawdown": -0.3},
        {"ticker": "BBB", "max_drawdown": -0.3},
    ]


def test_max_drawdown_endpoint_handles_missing_tickers(client):
    resp = client.get("/analytics/max-drawdown")
    assert resp.status_code == 200
    assert resp.get_json() == []
