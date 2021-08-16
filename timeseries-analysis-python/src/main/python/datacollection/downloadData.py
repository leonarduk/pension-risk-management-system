# http://localhost:8091/stock/download/ticker/REL?years=20&interpolate=false&clean=true
# date,open,high,low,close,volume
# 2005-01-04,481.75,494.50,481.75,494.25,11529668.00,Alphavantage
# 2005-01-05,490.00,493.75,490.00,492.25,9736498.00,Alphavantage
import pandas as pd


def getDataFrame(ticker, years=1 ):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(years) + '&interpolate=true&clean=true'


    df = pd.read_csv(CSV_URL, usecols=['date', 'close'], index_col='date')
    newdf = df.rename(columns={"close": ticker}, errors='raise')
    return newdf

print(pd.merge(getDataFrame("REL"), getDataFrame("AZN"), on='date') )