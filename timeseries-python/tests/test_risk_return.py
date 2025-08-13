import numpy as np
import pandas as pd
import pytest
from unittest.mock import patch

from analysis.risk_return import TRADING_DAYS, risk_return


def test_risk_return_calculates_metrics():
    prices = pd.DataFrame(
        {"AAPL": [100, 110, 105, 115]}, index=pd.date_range("2024-01-01", periods=4)
    )
    with patch("analysis.risk_return.get_time_series", return_value=prices):
        result = risk_return("AAPL", years=1)

    daily_returns = prices.pct_change().dropna()
    expected_return = (1 + daily_returns.mean()) ** TRADING_DAYS - 1
    expected_std = daily_returns.std() * np.sqrt(TRADING_DAYS)

    assert result.loc[0, "ticker"] == "AAPL"
    assert result.loc[0, "annual_return"] == pytest.approx(
        expected_return["AAPL"]
    )
    assert result.loc[0, "annual_std"] == pytest.approx(expected_std["AAPL"])
