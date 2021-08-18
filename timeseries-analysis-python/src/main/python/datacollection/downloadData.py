# http://localhost:8091/stock/download/ticker/REL?years=20&interpolate=false&clean=true
# date,open,high,low,close,volume
# 2005-01-04,481.75,494.50,481.75,494.25,11529668.00,Alphavantage
# 2005-01-05,490.00,493.75,490.00,492.25,9736498.00,Alphavantage

import pandas as pd
from functools import reduce
from pypfopt import EfficientFrontier
from pypfopt import risk_models
from pypfopt import expected_returns


def getDataFrame(ticker, years=10 ):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(years) + '&interpolate=true&clean=true'

    df = pd.read_csv(CSV_URL, usecols=['date', 'close'], index_col='date')
    newdf = df.rename(columns={"close": ticker}, errors='raise')
    print(CSV_URL + " " + str(df.size))

    return newdf


# compile the list of dataframes you want to merge
data_frames = [getDataFrame("REL.L"), getDataFrame("AZN.L"), getDataFrame("CTY.L"),
               getDataFrame("PHGP.L"), getDataFrame("RDSA.L"), getDataFrame("IMB.L"),
               getDataFrame("ULVR.L"), getDataFrame("GSK.L"), getDataFrame("DGE.L"),
               getDataFrame("EXPN.L"), getDataFrame("LGEN.L"), getDataFrame("GRG.L"),
               getDataFrame("ISXF.L"), getDataFrame("JRS.L"), getDataFrame("XMCX.L"),
              getDataFrame("IGUS.L"), getDataFrame("CGT.L"), getDataFrame("GILI.L"), getDataFrame("XLGS.L"), getDataFrame("WATL.L"), getDataFrame("XESW.L")]

df = reduce(lambda  left,right: pd.merge(left,right,on=['date'], how='outer'), data_frames)

# Calculate expected returns and sample covariance
mu = expected_returns.mean_historical_return(df)
S = risk_models.sample_cov(df)

# Optimize for maximal Sharpe ratio
ef = EfficientFrontier(mu, S)
raw_weights = ef.max_sharpe()
cleaned_weights = ef.clean_weights()
ef.save_weights_to_file("weights.csv")  # saves to file
print(cleaned_weights)
ef.portfolio_performance(verbose=True)

from pypfopt.discrete_allocation import DiscreteAllocation, get_latest_prices

latest_prices = get_latest_prices(df)

da = DiscreteAllocation(cleaned_weights, latest_prices, total_portfolio_value=400000)
allocation, leftover = da.greedy_portfolio()
print("Discrete allocation:", allocation)
print("Funds remaining: ${:.2f}".format(leftover))

