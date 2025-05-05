import os
import pandas as pd
import matplotlib.pyplot as plt
from ta.trend import SMAIndicator, MACD
from ta.momentum import RSIIndicator
from ta.volatility import BollingerBands
from integrations.portfolioperformance.api.timeseries import get_time_series


def apply_technical_indicators(df: pd.DataFrame, price_col: str = "Price") -> pd.DataFrame:
    df = df.copy()

    # SMA
    df["SMA20"] = SMAIndicator(df[price_col], window=20).sma_indicator()
    df["SMA50"] = SMAIndicator(df[price_col], window=50).sma_indicator()

    # RSI
    df["RSI14"] = RSIIndicator(df[price_col], window=14).rsi()

    # Bollinger Bands
    bb = BollingerBands(close=df[price_col], window=20, window_dev=2)
    df["bb_mavg"] = bb.bollinger_mavg()
    df["bb_high"] = bb.bollinger_hband()
    df["bb_low"] = bb.bollinger_lband()

    # MACD
    macd = MACD(close=df[price_col])
    df["MACD"] = macd.macd()
    df["MACD_signal"] = macd.macd_signal()

    return df


def plot_technical_indicators(df: pd.DataFrame, ticker: str = "Stock", save_path: str = None):
    plt.figure(figsize=(14, 10))

    # Ensure datetime index
    if not pd.api.types.is_datetime64_any_dtype(df.index):
        df.index = pd.to_datetime(df.index)

    # Format price to 2 decimal places and ensure y-axis formatting
    from matplotlib.ticker import FuncFormatter
    price_formatter = FuncFormatter(lambda x, _: f"{x:.2f}")

    # Price + SMAs + Bollinger Bands
    ax1 = plt.subplot(3, 1, 1)
    df["Price"].plot(ax=ax1, label="Price", color="black")
    df["SMA20"].plot(ax=ax1, label="SMA 20")
    df["SMA50"].plot(ax=ax1, label="SMA 50")
    df["bb_high"].plot(ax=ax1, label="BB High", linestyle="--", color="grey")
    df["bb_low"].plot(ax=ax1, label="BB Low", linestyle="--", color="grey")
    ax1.set_title(f"{ticker} Price + SMA + Bollinger Bands")
    ax1.legend()
    ax1.grid(True)
    ax1.yaxis.set_major_formatter(price_formatter)

    # RSI
    ax2 = plt.subplot(3, 1, 2, sharex=ax1)
    df["RSI14"].plot(ax=ax2, color="purple", label="RSI (14)")
    ax2.axhline(70, color='red', linestyle='--')
    ax2.axhline(30, color='green', linestyle='--')
    ax2.set_title("RSI")
    ax2.legend()
    ax2.grid(True)

    # MACD
    ax3 = plt.subplot(3, 1, 3, sharex=ax1)
    df["MACD"].plot(ax=ax3, label="MACD", color="blue")
    df["MACD_signal"].plot(ax=ax3, label="Signal Line", color="orange")
    ax3.set_title("MACD")
    ax3.legend()
    ax3.grid(True)

    plt.tight_layout()
    if save_path:
        os.makedirs(os.path.dirname(save_path), exist_ok=True)
        plt.savefig(save_path)
        print(f"✅ Plot saved to {save_path}")
    else:
        plt.show()


# Example usage (assumes your dataframe is loaded from XML or API):
if __name__ == "__main__":
    df = get_time_series(ticker="IUKD.L", years=5, xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml")
    if not df.empty:
        symbol = df.columns[0]  # assumes one column after index
        df = df.rename(columns={symbol: "Price"})

        # Debug: show series range and start/end prices
        print("\n📈 Time Series Summary:")
        print(f"Start Date: {df.index.min().date()} | End Date: {df.index.max().date()}")
        print(f"Start Price: {df['Price'].iloc[0]:.2f} | End Price: {df['Price'].iloc[-1]:.2f}")

        df = apply_technical_indicators(df)
        plot_technical_indicators(df, ticker=symbol, save_path=f"output/{symbol}_technical.png")
    else:
        print("❌ No price data available.")
