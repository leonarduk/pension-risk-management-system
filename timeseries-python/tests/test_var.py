import pandas as pd
from analysis import var


def test_calculate_var_scenario():
    returns = pd.Series([0.01, -0.02, 0.015, -0.03])
    base = var.calculate_var(returns)
    crash = var.calculate_var(returns, scenario="2008")
    assert crash < base


def test_rest_scenario_selection():
    client = var.app.test_client()
    data = {"returns": [0.01, -0.02, 0.015, -0.03]}
    base_resp = client.post("/var", json=data)
    crash_resp = client.post("/var?scenario=2008", json=data)
    assert crash_resp.get_json()["var"] < base_resp.get_json()["var"]
