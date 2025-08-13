import pandas as pd
from unittest.mock import patch

from analysis.risk_return import risk_return


def test_risk_return_calculates_metrics():
    prices = pd.DataFrame({"AAPL": [100, 110, 105, 115]}, index=pd.date_range("2024-01-01", periods=4))
    with patch("analysis.risk_return.get_time_series", return_value=prices):
        result = risk_return("AAPL", years=1)
    assert result.loc[0, "ticker"] == "AAPL"
    assert "annual_return" in result.columns
    assert "annual_std" in result.columns
