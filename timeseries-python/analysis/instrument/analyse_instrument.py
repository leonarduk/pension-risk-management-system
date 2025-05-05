import os
import pandas as pd
import matplotlib.pyplot as plt
from ta.trend import SMAIndicator, MACD
from ta.momentum import RSIIndicator
from ta.volatility import BollingerBands

from integrations.portfolioperformance.api.positions import get_unique_tickers
from integrations.portfolioperformance.api.timeseries import get_time_series
from integrations.stockfeed.timeseries import get_name_map_from_csv


def apply_technical_indicators(df: pd.DataFrame, price_col: str = "Price") -> pd.DataFrame:
    df = df.copy()

    df["SMA20"] = SMAIndicator(df[price_col], window=20).sma_indicator()
    df["SMA50"] = SMAIndicator(df[price_col], window=50).sma_indicator()
    df["RSI14"] = RSIIndicator(df[price_col], window=14).rsi()
    bb = BollingerBands(close=df[price_col], window=20, window_dev=2)
    df["bb_mavg"] = bb.bollinger_mavg()
    df["bb_high"] = bb.bollinger_hband()
    df["bb_low"] = bb.bollinger_lband()
    macd = MACD(close=df[price_col])
    df["MACD"] = macd.macd()
    df["MACD_signal"] = macd.macd_signal()

    return df


def generate_signals(df: pd.DataFrame) -> pd.DataFrame:
    signals = []

    for i in range(1, len(df)):
        date = df.index[i]
        price = df.loc[date, "Price"]

        if df.loc[df.index[i - 1], "MACD"] < df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] > df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bullish Crossover"))
        elif df.loc[df.index[i - 1], "MACD"] > df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] < df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bearish Crossover"))

        rsi = df.loc[date, "RSI14"]
        if rsi > 70:
            signals.append((date, price, "RSI Overbought"))
        elif rsi < 30:
            signals.append((date, price, "RSI Oversold"))

        if df.loc[df.index[i - 1], "SMA20"] < df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] > df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bullish Crossover"))
        elif df.loc[df.index[i - 1], "SMA20"] > df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] < df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bearish Crossover"))

    return pd.DataFrame(signals, columns=["Date", "Price", "Signal"])


def plot_technical_indicators(df: pd.DataFrame, ticker: str = "Stock", save_path: str = None, signals_df: pd.DataFrame = None):
    plt.figure(figsize=(14, 10))

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


from datetime import datetime, timedelta

def summarize_recent_signals(signals_df: pd.DataFrame, n: int = 5, recent_days: int = None) -> str:
    if signals_df.empty:
        return "No signals found."

    if recent_days is not None:
        cutoff = datetime.now().date() - timedelta(days=recent_days)
        signals_df = signals_df[signals_df["Date"].dt.date >= cutoff]

    if signals_df.empty:
        return f"No signals in the last {recent_days} days."

    summary = signals_df.tail(n) if n else signals_df

    def color_signal(signal):
        if any(x in signal for x in ["Bullish", "Oversold"]):
            return f"\033[92m{signal}\033[0m"  # Green
        elif any(x in signal for x in ["Bearish", "Overbought"]):
            return f"\033[91m{signal}\033[0m"  # Red
        else:
            return signal

    return "\n".join([
        f"{row['Date'].date()} - {color_signal(row['Signal'])} at {row['Price']:.2f}"
        for _, row in summary.iterrows()
    ])


def analyze_ticker(ticker: str, xml_path: str, output_dir: str, years: int = 5, recent_days: int = None) -> str:
    df = get_time_series(ticker=ticker, years=years, xml_file=xml_path)
    if df.empty:
        print(f"❌ No price data for {ticker}")
        return f"{ticker}: No data."

    symbol = df.columns[0]
    df = df.rename(columns={symbol: "Price"})

    print(f"\n📈 {ticker} Time Series:")
    print(f"Start: {df.index.min().date()}, End: {df.index.max().date()}")
    print(f"Prices: {df['Price'].iloc[0]:.2f} → {df['Price'].iloc[-1]:.2f}")

    df = apply_technical_indicators(df)
    signals_df = generate_signals(df)
    summary = summarize_recent_signals(signals_df, recent_days=recent_days)
    print(f"\n📌 Recent Signals for {ticker}:")
    print(summary)

    os.makedirs(output_dir, exist_ok=True)
    plot_path = os.path.join(output_dir, f"{ticker}_technical.png")
    signals_path = os.path.join(output_dir, f"{ticker}_signals.csv")
    plot_technical_indicators(df, ticker=ticker, save_path=plot_path, signals_df=signals_df)
    signals_df.to_csv(signals_path, index=False)
    print(f"✅ Saved: {plot_path}, {signals_path}")

    if recent_days is not None:
        cutoff = datetime.now().date() - timedelta(days=recent_days)
        recent_signals = signals_df[signals_df["Date"].dt.date >= cutoff]
    else:
        recent_signals = signals_df

    if not recent_signals.empty:
        latest = recent_signals.tail(1).iloc[0]
        signal_text = latest['Signal']
        if any(x in signal_text for x in ["Bullish", "Oversold"]):
            signal_text = f"\033[92m{signal_text}\033[0m"  # Green
        elif any(x in signal_text for x in ["Bearish", "Overbought"]):
            signal_text = f"\033[91m{signal_text}\033[0m"  # Red
        return f"{ticker}: {signal_text} at {latest['Price']:.2f} on {latest['Date'].date()}"
    else:
        return f"{ticker}: No recent signals."


if __name__ == "__main__":
    xml_path = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    recent_days = 5

    tickers = get_unique_tickers(xml_file=xml_path)

    tickers = ['AAPL', 'MSFT', 'GOOGL']  # Example tickers for testing
    print(f"\n📊 Unique Tickers in XML: {len(tickers)}")

    output_dir = "output"

    all_summaries = []
    for ticker in tickers:
        summary = analyze_ticker(ticker, xml_path, output_dir, recent_days=recent_days)
        all_summaries.append(summary)

    print("\n📢 Summary of Recent Signals Across All Tickers:")
    for s in all_summaries:
        print("-", s)
