from analysis.markov_simulation import simulate_markov, app


def test_markov_simulation_deterministic():
    prices = [100, 101, 100, 101]
    result = simulate_markov(prices, steps=2)
    assert result == [100.0, 101.0]


def test_markov_endpoint():
    client = app.test_client()
    resp = client.post("/analytics/markov", json={"prices": [100, 101, 102], "steps": 2})
    assert resp.status_code == 200
    assert resp.get_json()["simulated_prices"] == [103.0, 104.0]
