from __future__ import annotations

import datetime
import os

import pandas as pd
import matplotlib.pyplot as plt
from ta.trend import SMAIndicator, MACD
from ta.momentum import RSIIndicator
from ta.volatility import BollingerBands

import integrations.stockfeed.timeseries
import integrations.portfolioperformance.api.timeseries
from integrations.portfolioperformance.api.instrument_details import extract_instrument

from integrations.portfolioperformance.api.positions import get_unique_tickers, get_name_map_from_xml

from scipy.stats import linregress

import datetime as dt
from dataclasses import dataclass
from typing import Optional

import yfinance as yf                 # public domain, BSD-style licence
from dateutil.relativedelta import relativedelta

__all__ = ["DividendFetcher", "last_12m_dividend_yield"]

from pandas.core.interchange.dataframe_protocol import DataFrame


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


_currency_cache: dict[tuple[str, str], str] = {}      #  (xml_path, ticker) -> "GBP"/"GBX"/…

def currency_for_ticker(xml_path: str, ticker: str) -> str:
    """
    Return the <currencyCode> for *ticker* as stored in the PP XML.
    Falls back to 'UNKNOWN' if the security cannot be found.
    Caches results so the XML is only parsed once per run.
    """
    key = (xml_path, ticker.upper())
    if key in _currency_cache:
        return _currency_cache[key]

    try:
        data = extract_instrument(xml_path, ticker, format="json")
        ccy  = (data or {}).get("currencyCode", "UNKNOWN") or "UNKNOWN"
    except Exception:
        ccy = "UNKNOWN"

    _currency_cache[key] = ccy
    print(f"Currency for {ticker}: {ccy}")

    return ccy

def analyze_all_tickers(xml_path: str, recent_days: int = 5, group_signals: bool = True, output_dir: str = "output",
                        tickers: list = None, use_stockfeed=False):
    name_map = get_name_map_from_xml(xml_file=xml_path)

    bullish, bearish, predictions = [], [], []

    for ticker in tickers:
        name = name_map.get(ticker, ticker)
        df = get_time_series(ticker, use_stockfeed, xml_path)
        ccy = currency_for_ticker(xml_path, ticker)

        annual_yield, div_series = last_12m_dividend_yield(ticker=ticker, timeseries=None, use_stockfeed=False,
                                                           xml_path=xml_path, ccy=ccy)

        print(f"{name}: {annual_yield:.2f}% annual yield : {div_series}")

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
            summary = f"{name} ({ticker}): {latest['Signal']} at {latest['Price']:.2f} on {latest['Date'].date()} - {annual_yield:.2f}% annual yield"
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


def get_time_series(ticker, use_stockfeed, xml_path):
    if use_stockfeed:
        df = integrations.stockfeed.timeseries.get_time_series(ticker=ticker, years=5)
    else:
        df = integrations.portfolioperformance.api.timeseries.get_time_series(ticker=ticker, years=5, xml_file=xml_path)
    print(df.tail(1))

    return df


# analysis/instrument/analyse_instrument.py
def get_price_series(
    ticker: str,
    start: datetime.date,
    end: datetime.date,
    timeseries: pd.DataFrame | None = None,
    *,
    use_stockfeed: bool = True,
    xml_path: str | None = None,
) -> pd.Series:
    """
    Return a *Series of closing prices* between `start` and `end`.
    This is exactly the callable shape that dividends.last_12m_dividend_yield
    asks for:  (ticker, start_date, end_date) -> Series
    """
    if timeseries is None:
        # if no timeseries is passed, we fetch it from the API
        # this is the default behavior for the last_12m_dividend_yield function
        # but you can pass a timeseries DataFrame to avoid fetching it again
        # (e.g. if you already have it in memory)
        # this is useful for performance reasons
        # and avoids hitting the API too many times
        timeseries = get_time_series(ticker=ticker, use_stockfeed=use_stockfeed, xml_path=xml_path)

    # ▸ Case A: the index is already dates and the ticker is a column
    if ticker in timeseries.columns:
        price = timeseries[ticker]

    # ▸ Case B: fall back to whatever other shapes you support
    else:
        #-- existing StockFeed logic here (rename columns, set_index, …)
        price = (
            timeseries.rename(columns=str.lower)
              .set_index("date")["close"]
        )

    # ensure we’re working with datetime index for slicing
    if not isinstance(price.index, pd.DatetimeIndex):
        price.index = pd.to_datetime(price.index)

    return price.loc[start:end]


@dataclass(slots=True, frozen=True)
class DividendFetcher:
    """
    Thin wrapper around yfinance that returns a clean pandas Series of cash
    dividends (GBp for LSE symbols).  Nothing else is pulled – price lives in
    the existing price utilities already inside *timeseries-python*.
    """
    ticker: str

    def series(
        self,
        start: Optional[dt.date] = None,
        end: Optional[dt.date] = None,
    ) -> pd.Series:
        today = dt.date.today()
        start = start or today - relativedelta(years=5)
        end = end or today

        yf_ticker = yf.Ticker(self.ticker)
        # 1) get the dividends from yfinance
        try:
            divs = yf_ticker.dividends
        except Exception as e:
            print(f"Error fetching dividends for {self.ticker}: {e}")
            return pd.Series()

        # ① strip the timezone so the index becomes tz-naïve
        if divs.index.tz is not None:
            divs.index = divs.index.tz_localize(None)

        # ② now the slice works
        return divs.loc[start:end].rename("div_cash")

def _harmonise_units(div_cash: float, px: float, ccy: str) -> tuple[float, float]:
    """
    Ensure both dividend cash and price are expressed in the SAME unit.
    Strategy:
      ▸ If |px| > 100  ⇒ px is probably pence → convert to pounds.
      ▸ If |px| <= 100 and |div_cash| > 10    ⇒ dividends are pence → convert to pounds.
      (Tighten the thresholds if you like.)

    Args:
        ccy:
    """
    if  ccy == "GBP":      # GBP penny (p) is the default for LSE symbols
        px /= 100          # 3018 p → £30.18
    # elif div_cash > 10:    # dividends look like pence
    #     div_cash /= 100    # 22.45 p → £0.2245
    return div_cash, px

# ---------- public one-liner -------------------------------------------------
def last_12m_dividend_yield(ticker: str, today: dt.date | None = None, *, timeseries: DataFrame = None,
                            use_stockfeed: bool = True, xml_path: str | None = None, ccy: str = "GBX") -> tuple[float, pd.Series]:
    """
    Trailing-twelve-month cash dividends ÷ latest closing price.

    This function calculates the dividend yield for the given ticker symbol.
    The dividend yield is the ratio of the total dividends paid out in the
    last 12 months to the current price of the stock.

    Parameters
    ----------
    ticker         LSE symbol **with** ".L" suffix or any Yahoo-Finance ticker
    today          optional 'as-of' date (defaults to dt.date.today())
    timeseries     optional callable(ticker, start, end)->pd.Series; by default
                   we reuse the existing timeseries-python price utilities.
    use_stockfeed  optional boolean (defaults to True); if True, load prices
                   from stockfeed file; if False, download from Yahoo Finance
    xml_path       optional path to stockfeed file (defaults to None); only
                   used if use_stockfeed is True

    Returns
    -------
    float    yield expressed as a decimal (0.045 == 4.5 %)

    Args:
        ccy:
    """
    today = today or dt.date.today()
    window_start = today - relativedelta(months=12)

    # 1) dividends ----------------------------------------------------------
    # Pull the cash dividends for the given ticker in the last 12 months
    div_series = DividendFetcher(ticker).series(window_start, today)
    div_cash = div_series.sum()

    # 2) prices -------------------------------------------------------------
    # Pull the prices for the given ticker in the last 12 months.
    # We pull a range so we’re sure to get at least one row.
    prices = get_price_series(
        ticker,
        window_start,
        today,
        timeseries=timeseries,
        use_stockfeed=use_stockfeed,
        xml_path=xml_path,
    ).dropna()

    if prices.empty:
        raise ValueError(f"No price data in {window_start:%Y-%m-%d}:{today}")

    # Take the last valid close price
    px = prices.iloc[-1]

    div_cash, px = _harmonise_units(div_cash, px, ccy)

    # Calculate the dividend yield
    annual_yield = float(div_cash / px) * 100

    if annual_yield > 50:
        print(f"⚠️  {ticker} has an unusually high yield of {annual_yield:.2f}% so dividing by 100.")
        annual_yield /= 100


    return annual_yield, div_series

if __name__ == '__main__':
    xml_path = "C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml"
    override_tickers = []
    tickers = override_tickers or get_unique_tickers(xml_file=xml_path)

    analyze_all_tickers(
        xml_path="C:/Users/steph/workspaces/luk/data/portfolio/investments-with-id.xml",
        recent_days=5,
        group_signals=True,
        output_dir="output",
        tickers=tickers,
        use_stockfeed=False
    )

    # print(last_12m_dividend_yield(ticker="FSFL.L", timeseries=None, use_stockfeed=False, xml_path=xml_path))
