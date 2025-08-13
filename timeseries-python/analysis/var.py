"""Risk analysis utilities including Value at Risk (VaR)."""

from typing import Iterable

import numpy as np
import pandas as pd
from flask import Flask, jsonify, request


app = Flask(__name__)

# Optional scenarios that can be used to stress losses.  Values less than 1
# would decrease the severity of losses while values greater than 1 amplify
# them.  The dictionary is deliberately small but can be extended in the
# future if required.
SCENARIO_FACTORS = {"2008": 3.0}


def historical_var(
    returns: Iterable[float],
    confidence_level: float = 0.95,
    scenario: str | None = None,
) -> float:
    """Calculate the historical simulation Value at Risk (VaR).

    Parameters
    ----------
    returns:
        Iterable of periodic returns expressed as decimal fractions (for example
        ``0.01`` for a one percent gain).  The input may be a list, ``numpy``
        array or ``pandas.Series`` and non-numeric values are ignored.
    confidence_level:
        Confidence level for the VaR calculation. ``0.95`` means 95%%
        confidence.
    scenario:
        Optional key in ``SCENARIO_FACTORS`` to apply a stress scaling to
        negative returns before computing VaR.

    Returns
    -------
    float
        The historical simulation VaR expressed as a negative number.  This
        represents the maximum expected loss over the period at the given
        confidence level.
    """

    if isinstance(returns, pd.Series):
        data = returns.dropna().values
    else:
        data = np.asarray(list(returns))
        data = data[~np.isnan(data)]

    if data.size == 0:
        raise ValueError("returns must contain at least one numeric value")

    series = pd.Series(data)
    if scenario and scenario in SCENARIO_FACTORS:
        factor = SCENARIO_FACTORS[scenario]
        series = series.apply(lambda x: x * factor if x < 0 else x)

    sorted_returns = np.sort(series)
    index = int(np.floor((1 - confidence_level) * len(sorted_returns)))
    index = min(max(index, 0), len(sorted_returns) - 1)
    return float(sorted_returns[index])


@app.post("/risk/historic-var")
def historic_var_endpoint():
    """REST endpoint returning historical simulation VaR."""

    data = request.get_json(force=True) or {}
    returns = data.get("returns", [])
    confidence = float(request.args.get("confidenceLevel", 0.95))
    scenario = request.args.get("scenario")
    var = historical_var(returns, confidence, scenario)
    return jsonify({"var": var})


if __name__ == "__main__":
    app.run()
