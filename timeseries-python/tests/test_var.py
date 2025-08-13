import pandas as pd

from analysis.var import historical_var


def test_historical_var_basic():
    returns = pd.Series([0.01, -0.02, 0.03, -0.04, 0.02])
    var = historical_var(returns, confidence_level=0.95)
    # 5% percentile of sorted returns [-0.04, -0.02, 0.01, 0.02, 0.03] is -0.04
    assert abs(var + 0.04) < 1e-9


def test_historical_var_handles_lists():
    returns = [0.05, -0.01, 0.02, -0.03]
    var = historical_var(returns, confidence_level=0.75)
    # 25% percentile => sorted [-0.03, -0.01, 0.02, 0.05] index 1 => -0.01
    assert abs(var + 0.01) < 1e-9


def test_historical_var_raises_on_empty():
    try:
        historical_var([])
    except ValueError:
        assert True
    else:
        assert False
