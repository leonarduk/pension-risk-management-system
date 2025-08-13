import numpy as np
import pandas as pd
from flask import Flask, request, jsonify

app = Flask(__name__)

SCENARIO_FACTORS = {
    "2008": 3.0,
}

def _apply_scenario(returns: pd.Series, scenario: str) -> pd.Series:
    if scenario in SCENARIO_FACTORS:
        factor = SCENARIO_FACTORS[scenario]
        return returns.apply(lambda x: x * factor if x < 0 else x)
    return returns

def calculate_var(returns, confidence_level: float = 0.95, scenario: str | None = None):
    series = pd.Series(returns)
    series = _apply_scenario(series, scenario)
    var = np.percentile(series, (1 - confidence_level) * 100)
    return var

@app.post("/var")
def var_endpoint():
    data = request.get_json(force=True) or {}
    returns = data.get("returns", [])
    confidence = float(request.args.get("confidence", 0.95))
    scenario = request.args.get("scenario")
    var = calculate_var(returns, confidence, scenario)
    return jsonify({"var": var})

if __name__ == "__main__":
    app.run()
