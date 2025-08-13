"""Markov chain based price simulation.

This module provides utilities to estimate a simple Markov chain from
historical price movements and to simulate future price paths.  It also
exposes a small Flask application so the simulation can be accessed via
an HTTP endpoint.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Iterable, List, Sequence
import random

import numpy as np
from flask import Flask, jsonify, request

# ---------------------------------------------------------------------------
# Core Markov chain logic
# ---------------------------------------------------------------------------


STATES = ["up", "down", "same"]


@dataclass
class MarkovModel:
    """Represents a simple first-order Markov chain for price movements."""

    transition_matrix: dict
    avg_moves: dict


def _price_states(prices: Sequence[float]) -> tuple[List[str], List[float]]:
    """Return states (up/down/same) and price differences from a price series."""
    diffs = np.diff(prices)
    states = []
    for d in diffs:
        if d > 0:
            states.append("up")
        elif d < 0:
            states.append("down")
        else:
            states.append("same")
    return states, diffs.tolist()


def build_markov_model(prices: Iterable[float]) -> MarkovModel:
    """Build a Markov model from a sequence of prices."""
    prices = list(prices)
    if len(prices) < 2:
        raise ValueError("At least two prices are required to build a model")

    states, diffs = _price_states(prices)

    # Transition counts
    matrix = {s: {s2: 0 for s2 in STATES} for s in STATES}
    for a, b in zip(states, states[1:]):
        matrix[a][b] += 1

    # Convert to probabilities
    for s in STATES:
        total = sum(matrix[s].values())
        if total:
            matrix[s] = {k: v / total for k, v in matrix[s].items()}
        else:
            # If a state was never observed, assume equal probabilities
            matrix[s] = {k: 1 / len(STATES) for k in STATES}

    # Average movement for each state
    avg_moves: dict[str, float] = {s: 0.0 for s in STATES}
    for state, diff in zip(states, diffs):
        avg_moves[state] += diff
    counts = {s: states.count(s) for s in STATES}
    for s in STATES:
        if counts[s]:
            avg_moves[s] /= counts[s]

    return MarkovModel(transition_matrix=matrix, avg_moves=avg_moves)


def simulate_markov(
    prices: Iterable[float], steps: int, seed: int | None = None
) -> List[float]:
    """Simulate future prices using a Markov chain.

    Parameters
    ----------
    prices:
        Historical prices used to estimate the transition matrix.
    steps:
        Number of future steps to simulate.
    seed:
        Optional seed for reproducibility.
    """
    model = build_markov_model(prices)
    rng = random.Random(seed)

    prices = list(prices)
    current_price = prices[-1]
    states, _ = _price_states(prices)
    current_state = states[-1]

    simulated: List[float] = []
    for _ in range(steps):
        probs = [model.transition_matrix[current_state][s] for s in STATES]
        next_state = rng.choices(STATES, weights=probs)[0]
        move = model.avg_moves.get(next_state, 0.0)
        current_price += move
        simulated.append(float(current_price))
        current_state = next_state
    return simulated


# ---------------------------------------------------------------------------
# Flask endpoint
# ---------------------------------------------------------------------------

app = Flask(__name__)


@app.post("/analytics/markov")
def markov_endpoint():
    data = request.get_json(force=True) or {}
    prices = data.get("prices", [])
    steps = int(data.get("steps", 10))
    seed = data.get("seed")
    try:
        simulation = simulate_markov(prices, steps, seed)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400
    return jsonify({"simulated_prices": simulation})


if __name__ == "__main__":
    app.run(debug=True)
