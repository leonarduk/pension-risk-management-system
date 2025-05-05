import os
import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime, timedelta
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
    df["MACD_hist"] = macd.macd_diff()
    return df

def generate_signals(df: pd.DataFrame, group_by_date: bool = False) -> pd.DataFrame:
    signals = []

    for i in range(1, len(df)):
        date = df.index[i]
        price = df.loc[date, "Price"]

        # MACD crossovers
        if df.loc[df.index[i - 1], "MACD"] < df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] > df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bullish Crossover"))
        elif df.loc[df.index[i - 1], "MACD"] > df.loc[df.index[i - 1], "MACD_signal"] and df.loc[date, "MACD"] < df.loc[date, "MACD_signal"]:
            signals.append((date, price, "MACD Bearish Crossover"))

        # MACD histogram reversal
        if df.loc[df.index[i - 1], "MACD_hist"] < 0 and df.loc[date, "MACD_hist"] > 0:
            signals.append((date, price, "MACD Histogram Bullish Reversal"))
        elif df.loc[df.index[i - 1], "MACD_hist"] > 0 and df.loc[date, "MACD_hist"] < 0:
            signals.append((date, price, "MACD Histogram Bearish Reversal"))

        # RSI signals
        rsi = df.loc[date, "RSI14"]
        if rsi > 70:
            signals.append((date, price, "RSI Overbought"))
        elif rsi < 30:
            signals.append((date, price, "RSI Oversold"))

        # SMA crossovers
        if df.loc[df.index[i - 1], "SMA20"] < df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] > df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bullish Crossover"))
        elif df.loc[df.index[i - 1], "SMA20"] > df.loc[df.index[i - 1], "SMA50"] and df.loc[date, "SMA20"] < df.loc[date, "SMA50"]:
            signals.append((date, price, "SMA Bearish Crossover"))

        # Price crossing SMA20
        if df.loc[df.index[i - 1], "Price"] < df.loc[df.index[i - 1], "SMA20"] and df.loc[date, "Price"] > df.loc[date, "SMA20"]:
            signals.append((date, price, "Price Crossed Above SMA20"))
        elif df.loc[df.index[i - 1], "Price"] > df.loc[df.index[i - 1], "SMA20"] and df.loc[date, "Price"] < df.loc[date, "SMA20"]:
            signals.append((date, price, "Price Crossed Below SMA20"))

        # Bollinger Band Breakouts
        if df.loc[date, "Price"] > df.loc[date, "bb_high"]:
            signals.append((date, price, "Price Above Bollinger Band"))
        elif df.loc[date, "Price"] < df.loc[date, "bb_low"]:
            signals.append((date, price, "Price Below Bollinger Band"))

        # Price crossing BB middle
        if df.loc[df.index[i - 1], "Price"] < df.loc[df.index[i - 1], "bb_mavg"] and df.loc[date, "Price"] > df.loc[date, "bb_mavg"]:
            signals.append((date, price, "Price Crossed Above BB Mid"))
        elif df.loc[df.index[i - 1], "Price"] > df.loc[df.index[i - 1], "bb_mavg"] and df.loc[date, "Price"] < df.loc[date, "bb_mavg"]:
            signals.append((date, price, "Price Crossed Below BB Mid"))

    df_signals = pd.DataFrame(signals, columns=["Date", "Price", "Signal"])

    if group_by_date and not df_signals.empty:
        df_signals = df_signals.groupby("Date").agg({
            "Price": "first",
            "Signal": lambda x: "; ".join(x)
        }).reset_index()

    return df_signals

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
            signal = row['Signal']
            color = 'green' if 'Bullish' in signal or 'Oversold' in signal else 'red'
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

def colorize(signal):
    signal_lower = signal.lower()
    if any(x in signal_lower for x in ["bullish", "oversold", "crossed above"]):
        return f"[92m{signal}[0m"
    elif any(x in signal_lower for x in ["bearish", "overbought", "crossed below"]):
        return f"[91m{signal}[0m"
    return signal

def analyze_all_tickers(xml_path: str, recent_days: int = 5, group_signals: bool = True, output_dir: str = "output", override_tickers: list = None):
    tickers = get_unique_tickers(xml_file=xml_path)
    if override_tickers:
        tickers = override_tickers

    print(f"📊 Unique Tickers in XML: {len(tickers)}")

    all_summaries = []
    for ticker in tickers:
        df = get_time_series(ticker=ticker, years=5, xml_file=xml_path)
        if df.empty:
            print(f"❌ No price data for {ticker}")
            all_summaries.append(f"{ticker}: No data.")
            continue

        symbol = df.columns[0]
        df = df.rename(columns={symbol: "Price"})
        df = apply_technical_indicators(df)
        signals_df = generate_signals(df, group_by_date=group_signals)

        cutoff = datetime.now().date() - timedelta(days=recent_days)
        recent = signals_df[signals_df["Date"].dt.date >= cutoff] if not signals_df.empty else pd.DataFrame()

        plot_path = os.path.join(output_dir, f"{ticker}_technical.png")
        plot_technical_indicators(df, ticker=ticker, save_path=plot_path, signals_df=signals_df)

        if recent.empty:
            all_summaries.append(f"{ticker}: No recent signals.")
        else:
            latest = recent.tail(1).iloc[0]
            signal_colored = colorize(latest['Signal'])
            all_summaries.append(f"{ticker}: {signal_colored} at {latest['Price']:.2f} on {latest['Date'].date()}")

    print("📢 Summary of Recent Signals Across All Tickers:")
    for s in all_summaries:
        print("-", s)

if __name__ == "__main__":
    xml_path = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    recent_days = 5
    group_signals = True
    override_tickers = None # ['AAPL', 'MSFT', 'GOOGL']

    analyze_all_tickers(xml_path, recent_days, group_signals, output_dir="output", override_tickers=override_tickers)
