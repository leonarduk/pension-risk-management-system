import os
import sys
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import requests
from pypfopt import EfficientFrontier, risk_models, expected_returns

from integrations.portfolioperformance.api.positions import (
    get_name_map_from_xml,
    get_unique_tickers,
)
from integrations.stockfeed.timeseries import fetch_prices_for_tickers

DATE = "Date"
PRICE = "Price"

OUTPUT_DIR = "output"
os.makedirs(OUTPUT_DIR, exist_ok=True)


API_URL = "http://localhost:8090/timeseries"
sys.modules.setdefault("timeseries_api", sys.modules[__name__])


def get_time_series(ticker, years=1):
    """Fetch time series data for one or multiple tickers.

    The function posts to a timeseries API expecting JSON in the form
    ``{ticker: {date: price}}``. Missing or empty ticker data is skipped.
    """

    payload = {"ticker": ticker, "years": years}
    response = requests.post(API_URL, json=payload)
    data = response.json()
    if not data:
        return pd.DataFrame()

    frames = []
    for tck, series in data.items():
        if not series:
            continue
        df = pd.DataFrame(series.items(), columns=[DATE, tck])
        df[DATE] = pd.to_datetime(df[DATE])
        frames.append(df.set_index(DATE))

    if not frames:
        return pd.DataFrame()
    return pd.concat(frames, axis=1)


def calculate_var(portfolio_returns, confidence_level=0.95):
    var = np.percentile(portfolio_returns, (1 - confidence_level) * 100)
    print(f"1-day VaR at {confidence_level * 100:.0f}% confidence: {var:.2%}")
    return var


def optimize_portfolio(prices):
    mu = expected_returns.mean_historical_return(prices)
    S = risk_models.sample_cov(prices)

    ef = EfficientFrontier(mu, S)
    ef.max_sharpe()
    cleaned_weights = ef.clean_weights()

    print("\nOptimal weights:")
    print(cleaned_weights)
    ef.portfolio_performance(verbose=True)

    returns = prices.pct_change().dropna()
    weighted_returns = returns.dot(pd.Series(cleaned_weights))

    return cleaned_weights, weighted_returns


def plot_prices(prices, filename=f"{OUTPUT_DIR}/prices.png"):
    prices.plot(title="Price Timeseries", figsize=(10, 5))
    plt.ylabel("Price")
    plt.xlabel("Date")
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()


def plot_weights(weights, name_map=None, filename=f"{OUTPUT_DIR}/weights.png"):
    """
    Plots a horizontal bar chart of the portfolio's optimized weights,
    sorted by weight (largest first), with labels in 'Name (Symbol)' format.

    Args:
        weights (dict): Optimized weights keyed by ticker symbol.
        name_map (dict, optional): Mapping of asset names to ticker symbols.
        filename (str): Output file path.

    Returns:
        None
    """
    # Filter and map symbols to labels and weights
    labeled_weights = []
    for symbol, weight in weights.items():
        if weight > 0:
            name = (
                next((n for n, s in name_map.items() if s == symbol), symbol)
                if name_map
                else symbol
            )
            label = f"{name} ({symbol})"
            labeled_weights.append((label, weight))

    # Sort by weight descending
    labeled_weights.sort(key=lambda x: x[1], reverse=True)

    labels = [lw[0] for lw in labeled_weights]
    values = [lw[1] for lw in labeled_weights]

    fig, ax = plt.subplots(figsize=(10, 0.4 * len(labels) + 1))
    ax.barh(labels, values, color="skyblue")
    ax.set_xlabel("Weight")
    ax.set_title("Optimized Portfolio Allocation (Sorted)")
    ax.set_xlim(0, 1)
    ax.invert_yaxis()  # Most weight at the top
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()


def plot_cumulative_returns(
    weighted_returns, filename=f"{OUTPUT_DIR}/cumulative_returns.png"
):
    cumulative = (1 + weighted_returns).cumprod()
    cumulative.plot(title="Cumulative Portfolio Returns", figsize=(10, 5))
    plt.ylabel("Growth")
    plt.xlabel("Date")
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()


def plot_var_distribution(
    portfolio_returns, filename=f"{OUTPUT_DIR}/return_distribution.png"
):
    plt.hist(portfolio_returns, bins=50, alpha=0.7, color="blue")
    plt.axvline(
        np.percentile(portfolio_returns, 5),
        color="red",
        linestyle="dashed",
        linewidth=2,
    )
    plt.title("Distribution of Daily Portfolio Returns (VaR)")
    plt.xlabel("Daily Return")
    plt.ylabel("Frequency")
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(filename)
    plt.close()


# 🚀 Main execution
if __name__ == "__main__":
    xml_path = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"

    name_map = get_name_map_from_xml(xml_file=xml_path)
    tickers = get_unique_tickers(xml_file=xml_path)

    prices = fetch_prices_for_tickers(tickers, years=10)

    if not prices.empty:
        plot_prices(prices)

        print("\n🔍 Optimizing portfolio...")
        weights, port_returns = optimize_portfolio(prices)

        plot_weights(weights, name_map)
        plot_cumulative_returns(port_returns)
        plot_var_distribution(port_returns)

        print("\n📉 Calculating historical VaR...")
        calculate_var(port_returns, confidence_level=0.95)

        print(f"\n✅ Plots saved in: {os.path.abspath(OUTPUT_DIR)}")
    else:
        print("❌ No data fetched.")
