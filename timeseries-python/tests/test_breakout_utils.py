import pandas as pd
import pytest
from analysis.screens.breakout_utils import twenty_day_breakout


def test_breakout_short_series_returns_defaults():
    price = pd.Series(range(10))
    result = twenty_day_breakout(price)
    assert result["is_20d_high"] is False
    assert pd.isna(result["close_vs_20d_high"])
    assert result["above_sma50"] is False
    assert pd.isna(result["vol_vs_avg20"])


def test_breakout_detects_high_and_volume():
    price = pd.Series(range(1, 60))
    volume = pd.Series([100] * 59)
    volume.iloc[-1] = 300
    result = twenty_day_breakout(price, volume)
    assert result["is_20d_high"] is True
    assert result["close_vs_20d_high"] == pytest.approx(0)
    assert result["above_sma50"] is True
    assert result["vol_vs_avg20"] == pytest.approx(300 / 110)
