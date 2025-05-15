# size.py
"""Half‑Kelly position‑sizing based on expected Sharpe and volatility."""

def kelly(expected_daily_return: float, daily_vol: float, risk_fraction: float = 0.5) -> float:
    if daily_vol <= 0:
        return 0.0
    raw = expected_daily_return / (daily_vol ** 2)
    return max(0.0, min(risk_fraction * raw, 1.0))  # capped at 100 %
