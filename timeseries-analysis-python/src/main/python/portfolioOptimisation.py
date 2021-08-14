# Imports from Python packages.
import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler

# Imports from FinanceOps.
from FinanceOps.data_keys import *
from FinanceOps.data import load_stock_data, load_index_data
from FinanceOps.portfolio import EqualWeights, FixedWeights, AdaptiveWeights
from FinanceOps.returns import daily_returns

# Ticker-names for the stock indices.
ticker_SP500 = "S&P 500"
ticker_SP400 = "S&P 400"
ticker_SP600 = "S&P 600"
tickers_indices = [ticker_SP500, ticker_SP400, ticker_SP600]

# Ticker-names for the stocks.
tickers_stocks = ['CLX', 'CPB', 'DE', 'DIS', 'GIS',
                  'HSY', 'JNJ', 'K', 'PG']

