from __future__ import annotations

from flask import Flask, jsonify, render_template, request

from .risk_return import risk_return

app = Flask(__name__, template_folder="templates")


@app.route("/analytics/risk-return")
def risk_return_endpoint():
    tickers = request.args.get("tickers")
    if not tickers:
        return jsonify([])
    ticker_list = [t.strip() for t in tickers.split(",") if t.strip()]
    confidence = float(request.args.get("confidenceLevel", 0.95))
    data = risk_return(ticker_list, confidence_level=confidence)
    return jsonify(data.to_dict(orient="records"))


@app.route("/analytics/risk-return/ui")
def risk_return_ui():
    return render_template("risk_return.html")


if __name__ == "__main__":
    app.run(debug=True)
