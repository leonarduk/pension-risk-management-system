import pytest

from analysis.var import historical_var


def test_historical_var_basic():
    returns = [0.01, -0.02, 0.03, -0.04, 0.05]
    var = historical_var(returns, 0.95)
    assert var == pytest.approx(-0.04)
