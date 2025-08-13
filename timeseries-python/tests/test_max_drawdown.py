import pandas as pd
import pytest
from unittest.mock import patch

from analysis.max_drawdown import max_drawdown


def test_max_drawdown_calculates_metric():
    prices = pd.DataFrame({"AAPL": [100, 120, 90, 130]}, index=pd.date_range("2024-01-01", periods=4))
    with patch("analysis.max_drawdown.get_time_series", return_value=prices):
        result = max_drawdown("AAPL", years=1)
    assert result.loc[0, "ticker"] == "AAPL"
    assert result.loc[0, "max_drawdown"] == pytest.approx(0.25)
