import os
import pandas as pd
import matplotlib.pyplot as plt
from datetime import datetime, timedelta
from ta.trend import SMAIndicator, MACD
from ta.momentum import RSIIndicator
from ta.volatility import BollingerBands

from integrations.portfolioperformance.api.positions import get_unique_tickers, get_name_map_from_xml
from integrations.portfolioperformance.api.timeseries import get_time_series

from scipy.stats import linregress


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


def generate_signals(df: pd.DataFrame, group_by_date: bool = False, lookahead_days: int = 3) -> pd.DataFrame:
    signals = []

    for i in range(1, len(df)):
        date = df.index[i]
        price = df.loc[date, "Price"]

        rsi = df.loc[date, "RSI14"]
        macd = df.loc[date, "MACD"]
        macd_signal = df.loc[date, "MACD_signal"]
        macd_hist = df.loc[date, "MACD_hist"]
        sma20 = df.loc[date, "SMA20"]
        sma50 = df.loc[date, "SMA50"]
        prev_macd = df.loc[df.index[i - 1], "MACD"]
        prev_macd_signal = df.loc[df.index[i - 1], "MACD_signal"]
        prev_macd_hist = df.loc[df.index[i - 1], "MACD_hist"]
        prev_price = df.loc[df.index[i - 1], "Price"]
        prev_sma20 = df.loc[df.index[i - 1], "SMA20"]
        prev_sma50 = df.loc[df.index[i - 1], "SMA50"]
        prev_bb_mavg = df.loc[df.index[i - 1], "bb_mavg"]

        if prev_macd < prev_macd_signal and macd > macd_signal:
            signals.append((date, price, "MACD Bullish Crossover"))
        elif prev_macd > prev_macd_signal and macd < macd_signal:
            signals.append((date, price, "MACD Bearish Crossover"))

        if prev_macd_hist < 0 and macd_hist > 0:
            signals.append((date, price, "MACD Histogram Bullish Reversal"))
        elif prev_macd_hist > 0 and macd_hist < 0:
            signals.append((date, price, "MACD Histogram Bearish Reversal"))

        if rsi > 70:
            signals.append((date, price, "RSI Overbought"))
        elif rsi < 30:
            signals.append((date, price, "RSI Oversold"))

        if prev_sma20 < prev_sma50 and sma20 > sma50:
            signals.append((date, price, "SMA Bullish Crossover"))
        elif prev_sma20 > prev_sma50 and sma20 < sma50:
            signals.append((date, price, "SMA Bearish Crossover"))

        if prev_price < prev_sma20 and price > sma20:
            signals.append((date, price, "Price Crossed Above SMA20"))
        elif prev_price > prev_sma20 and price < sma20:
            signals.append((date, price, "Price Crossed Below SMA20"))

        if price > df.loc[date, "bb_high"]:
            signals.append((date, price, "Price Above Bollinger Band"))
        elif price < df.loc[date, "bb_low"]:
            signals.append((date, price, "Price Below Bollinger Band"))

        if prev_price < prev_bb_mavg and price > df.loc[date, "bb_mavg"]:
            signals.append((date, price, "Price Crossed Above BB Mid"))
        elif prev_price > prev_bb_mavg and price < df.loc[date, "bb_mavg"]:
            signals.append((date, price, "Price Crossed Below BB Mid"))

        if rsi < 50 and macd > macd_signal and price > sma20 > sma50:
            signals.append((date, price, "🔥 Ideal Buy Setup"))
        elif rsi > 50 and macd < macd_signal and price < sma20 < sma50:
            signals.append((date, price, "⚠️ Better Sell Setup"))

    recent = df[['MACD', 'MACD_signal']].dropna().tail(5)
    if len(recent) >= 3:
        x = list(range(len(recent)))
        macd_slope = linregress(x, recent['MACD'])[0]
        signal_slope = linregress(x, recent['MACD_signal'])[0]
        current_gap = recent['MACD'].iloc[-1] - recent['MACD_signal'].iloc[-1]
        future_gap = current_gap + lookahead_days * (macd_slope - signal_slope)
        if current_gap < 0 and future_gap > 0:
            signals.append(
                (df.index[-1], df.iloc[-1]['Price'], f"🔮 MACD Bullish Crossover Likely in {lookahead_days}d"))
        elif current_gap > 0 and future_gap < 0:
            signals.append(
                (df.index[-1], df.iloc[-1]['Price'], f"🔮 MACD Bearish Crossover Likely in {lookahead_days}d"))

    df_signals = pd.DataFrame(signals, columns=["Date", "Price", "Signal"])

    if group_by_date and not df_signals.empty:
        df_signals = df_signals.groupby("Date").agg({
            "Price": "first",
            "Signal": lambda x: "; ".join(sorted(x))
        }).reset_index()

    return df_signals


def plot_technical_indicators(df: pd.DataFrame, ticker: str = "Stock", save_path: str = None,
                              signals_df: pd.DataFrame = None):
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

    plt.close()


def colorize(signal):
    signal_lower = signal.lower()
    if "ideal buy" in signal_lower or "bullish" in signal_lower or "oversold" in signal_lower or "crossed above" in signal_lower:
        return f"\033[92m{signal}\033[0m"
    elif "sell" in signal_lower or "bearish" in signal_lower or "overbought" in signal_lower or "crossed below" in signal_lower:
        return f"\033[91m{signal}\033[0m"
    return signal


def analyze_all_tickers(xml_path: str, recent_days: int = 5, group_signals: bool = True, output_dir: str = "output",
                        override_tickers: list = None):
    name_map = get_name_map_from_xml(xml_file=xml_path)
    tickers = override_tickers or get_unique_tickers(xml_file=xml_path)

    bullish, bearish, predictions = [], [], []

    for ticker in tickers:
        name = name_map.get(ticker, ticker)
        df = get_time_series(ticker=ticker, years=5, xml_file=xml_path)
        if df.empty:
            print(f"❌ No data for {ticker}")
            continue

        df.rename(columns={df.columns[0]: "Price"}, inplace=True)
        df = apply_technical_indicators(df)
        signals_df = generate_signals(df, group_by_date=group_signals)

        signal_path = os.path.join(output_dir, f"{ticker}_signals.csv")
        signals_df.to_csv(signal_path, index=False)

        # Save chart image
        plot_path = os.path.join(output_dir, f"{ticker}_technical.png")
        plot_technical_indicators(df, ticker=ticker, save_path=plot_path, signals_df=signals_df)

        if not signals_df.empty:
            latest = signals_df.tail(1).iloc[0]
            summary = f"{name} ({ticker}): {latest['Signal']} at {latest['Price']:.2f} on {latest['Date'].date()}"
            if "likely" in latest['Signal'].lower():
                predictions.append(summary)
            elif "sell" in latest['Signal'].lower() or "bearish" in latest['Signal'].lower() or "overbought" in latest[
                'Signal'].lower():
                bearish.append(summary)
            else:
                bullish.append(summary)

    print("\n📢 Bullish Signals:")
    for b in sorted(bullish):
        print("-", colorize(b))

    print("\n📉 Bearish Signals:")
    for b in sorted(bearish):
        print("-", colorize(b))

    if predictions:
        print("\n🔮 Predictions:")
        for p in sorted(predictions):
            print("-", colorize(p))


if __name__ == "__main__":
    analyze_all_tickers(
        xml_path="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        recent_days=5,
        group_signals=True,
        output_dir="output",
        override_tickers=[]
    )
