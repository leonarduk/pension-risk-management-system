# breakout_utils.py
"""Flag a breakout when a stock closes at a **20‑day high** *and* trades above
its 50‑day simple moving average.  Optionally confirm with a volume surge.

Returned columns (pd.Series)
---------------------------
* ``is_20d_high``        True/False
* ``close_vs_20d_high``  % distance to the 20‑day high (0 ⇒ exact)
* ``above_sma50``        True/False
* ``vol_vs_avg20``       volume ÷ 20‑day average (NaN if volume not passed)

Typical use inside your screener::

    extra = twenty_day_breakout(price_series, volume_series)
    df = pd.concat([df, extra], axis=1)
"""
from __future__ import annotations

import pandas as pd
from ta.trend import SMAIndicator
from typing import Optional


def twenty_day_breakout(
    price: pd.Series, volume: Optional[pd.Series] = None
) -> pd.Series:
    """Compute breakout diagnostics for the *last bar* of ``price`` Series."""

    price = price.dropna()
    if len(price) < 50:
        return pd.Series(
            {
                "is_20d_high": False,
                "close_vs_20d_high": float("nan"),
                "above_sma50": False,
                "vol_vs_avg20": float("nan"),
            }
        )

    last_close = price.iloc[-1]
    high_20 = price.rolling(20).max().iloc[-1]
    sma50 = SMAIndicator(price, window=50).sma_indicator().iloc[-1]
    is_20d_high = last_close >= high_20 - 1e-9  # tolerate float fuzz
    close_gap = last_close / high_20 - 1
    above_sma50 = last_close > sma50

    vol_ratio = float("nan")
    if volume is not None and not volume.empty and len(volume) >= 20:
        vol_series = volume.reindex_like(price).fillna(method="ffill").dropna()
        vol_ratio = vol_series.iloc[-1] / vol_series.rolling(20).mean().iloc[-1]

    return pd.Series(
        {
            "is_20d_high": bool(is_20d_high),
            "close_vs_20d_high": close_gap,
            "above_sma50": bool(above_sma50),
            "vol_vs_avg20": vol_ratio,
        }
    )
