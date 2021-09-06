import pandas as pd
from pandas import read_csv

def getDateAndClose(ticker, years=10 ):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(years) + '&interpolate=true&clean=true'

    df = pd.read_csv(CSV_URL, usecols=['date', 'close'], index_col='date')
    newdf = df.rename(columns={"close": ticker}, errors='raise')
    print(CSV_URL + " " + str(df.size))

    return newdf

def getTimeSeriesWithIncremementingKey(ticker, years=10 ):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(years) + '&interpolate=true&clean=true'
    df = read_csv(CSV_URL, header=0 )
    df["date"] = pd.to_datetime(df["date"]).dt.strftime("%Y%m%d").astype(int)
    df.insert(0, 'ID', range(1, 1 + len(df)))
    print(CSV_URL + " " + str(df.size))

    return df
