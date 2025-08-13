import numpy as np
import pandas as pd
from flask import Flask, request, jsonify

app = Flask(__name__)

SCENARIO_FACTORS = {
    "2008": 3.0,
}
"""Risk analysis utilities."""

from typing import Iterable



def historical_var(
    returns: Iterable[float],
    confidence_level: float = 0.95,
    scenario: str | None = None,
) -> float:
    """Calculate the historical simulation Value at Risk (VaR).

    Parameters
    ----------
    returns:
        Iterable of periodic returns expressed as decimal fractions (e.g. 0.01 for 1%).
        Can be a list, ``numpy.ndarray`` or ``pandas.Series``.
    confidence_level:
        Confidence level for the VaR calculation. 0.95 means 95%% confidence.

    Returns
    -------
    float
        The historical simulation VaR expressed as a negative number, representing
        the maximum expected loss over the period at the given confidence level.
    """
    if isinstance(returns, pd.Series):
        series = returns.dropna()
    else:
        series = pd.Series(list(returns)).dropna()

    if series.empty:
        raise ValueError("returns must contain at least one numeric value")

    series = _apply_scenario(series, scenario)

    sorted_returns = np.sort(series.values)
    index = int(np.floor((1 - confidence_level) * len(sorted_returns)))
    index = min(max(index, 0), len(sorted_returns) - 1)
    return float(sorted_returns[index])

def _apply_scenario(returns: pd.Series, scenario: str | None) -> pd.Series:
    if scenario in SCENARIO_FACTORS:
        factor = SCENARIO_FACTORS[scenario]
        return returns.apply(lambda x: x * factor if x < 0 else x)
    return returns

@app.post("/var")
def var_endpoint():
    data = request.get_json(force=True) or {}
    returns = data.get("returns", [])
    confidence = float(request.args.get("confidence", 0.95))
    scenario = request.args.get("scenario")
    var = historical_var(returns, confidence, scenario)
    return jsonify({"var": var})

if __name__ == "__main__":
    app.run()
