# http://localhost:8091/stock/download/ticker/REL?years=20&interpolate=false&clean=true
# date,open,high,low,close,volume
# 2005-01-04,481.75,494.50,481.75,494.25,11529668.00,Alphavantage
# 2005-01-05,490.00,493.75,490.00,492.25,9736498.00,Alphavantage

import pandas as pd
from functools import reduce
from pypfopt import EfficientFrontier
from pypfopt import risk_models
from pypfopt import expected_returns
from TimeSeriesAPI import getDateAndClose as getDataFrame


if __name__ == "__main__":
    # compile the list of dataframes you want to merge
    data_frames = [getDataFrame("TRY.L"), getDataFrame("GB00B1TRHX07"), getDataFrame("SGLN.L"),  getDataFrame("SWDA.L")]

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


