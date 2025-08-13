"""Risk analysis utilities."""

from typing import Iterable

import numpy as np
import pandas as pd


def historical_var(returns: Iterable[float], confidence_level: float = 0.95) -> float:
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
        data = returns.dropna().values
    else:
        data = np.asarray(list(returns))
        data = data[~np.isnan(data)]

    if data.size == 0:
        raise ValueError("returns must contain at least one numeric value")

    sorted_returns = np.sort(data)
    index = int(np.floor((1 - confidence_level) * len(sorted_returns)))
    index = min(max(index, 0), len(sorted_returns) - 1)
    return float(sorted_returns[index])
