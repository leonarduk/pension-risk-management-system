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


def generate_signals(df: pd.DataFrame) -> pd.DataFrame:
    signals = []

    for i in range(1, len(df)):
        date = df.index[i]
        price = df.loc[date, "Price"]

        # MACD crossover
        if df.loc[df.index[i - 1], "MACD"] < df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] > df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bullish Crossover"))
        elif df.loc[df.index[i - 1], "MACD"] > df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] < df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bearish Crossover"))

        # RSI overbought/oversold
        rsi = df.loc[date, "RSI14"]
        if rsi > 70:
            signals.append((date, price, "RSI Overbought"))
        elif rsi < 30:
            signals.append((date, price, "RSI Oversold"))

        # SMA crossover
        if df.loc[df.index[i - 1], "SMA20"] < df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] > df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bullish Crossover"))
        elif df.loc[df.index[i - 1], "SMA20"] > df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] < df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bearish Crossover"))

    return pd.DataFrame(signals, columns=["Date", "Price", "Signal"])


def plot_technical_indicators(df: pd.DataFrame, ticker: str = "Stock", save_path: str = None, signals_df: pd.DataFrame = None):
    plt.figure(figsize=(14, 10))

    # Ensure datetime index
    if not pd.api.types.is_datetime64_any_dtype(df.index):
        df.index = pd.to_datetime(df.index)

    from matplotlib.ticker import FuncFormatter
    price_formatter = FuncFormatter(lambda x, _: f"{x:.2f}")

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

    # Annotate signals
    if signals_df is not None:
        for _, row in signals_df.iterrows():
            color = 'green' if 'Bullish' in row['Signal'] or 'Oversold' in row['Signal'] else 'red'
            ax1.annotate('⬆' if color == 'green' else '⬇', xy=(row['Date'], row['Price']), color=color, fontsize=8,
                         ha='center', va='bottom' if color == 'green' else 'top')

    ax2 = plt.subplot(3, 1, 2, sharex=ax1)
    df["RSI14"].plot(ax=ax2, color="purple", label="RSI (14)")
    ax2.axhline(70, color='red', linestyle='--')
    ax2.axhline(30, color='green', linestyle='--')
    ax2.set_title("RSI")
    ax2.legend()
    ax2.grid(True)

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


if __name__ == "__main__":
    df = get_time_series(ticker="IUKD.L", years=5, xml_file="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml")
    if not df.empty:
        symbol = df.columns[0]
        df = df.rename(columns={symbol: "Price"})

        print("\n📈 Time Series Summary:")
        print(f"Start Date: {df.index.min().date()} | End Date: {df.index.max().date()}")
        print(f"Start Price: {df['Price'].iloc[0]:.2f} | End Price: {df['Price'].iloc[-1]:.2f}")

        df = apply_technical_indicators(df)
        signals_df = generate_signals(df)
        print("\n📌 Buy/Sell Signals:")
        print(signals_df.tail(10))

        plot_technical_indicators(df, ticker=symbol, save_path=f"output/{symbol}_technical.png", signals_df=signals_df)

        signals_df.to_csv(f"output/{symbol}_signals.csv", index=False)
        print(f"✅ Signals saved to output/{symbol}_signals.csv")
    else:
        print("❌ No price data available.")
