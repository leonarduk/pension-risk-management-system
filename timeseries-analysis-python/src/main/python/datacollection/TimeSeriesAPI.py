import pandas as pd


def getDateAndCloseFromS3(ticker, years=10):
    JSON_URL = F"https://5qoyuh2ca1.execute-api.eu-west-1.amazonaws.com/Prod/stock?ticker={ticker}"
    print(JSON_URL)
    df = pd.read_json(JSON_URL)
    print(df)
    df_nested_list = pd.json_normalize(df, record_path=['data'])
    print(df_nested_list)
    # df1 = df[['date', 'close']]
    # new_df = df1.rename(columns={"close": ticker}, errors='raise')
    # new_df.set_index('date')
    print(JSON_URL + " " + str(df.size))

    return df_nested_list


def getDateAndClose(ticker, years=10):
    # https://5qoyuh2ca1.execute-api.eu-west-1.amazonaws.com/Prod/stock?ticker=DGE

    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(
        years) + '&interpolate=true&clean=true'

    df = pd.read_csv(CSV_URL, usecols=['date', 'close'], index_col='date')
    new_df = df.rename(columns={"close": ticker}, errors='raise')
    print(CSV_URL + " " + str(df.size))

    return new_df


def getTimeSeriesWithIncremementingKey(ticker, years=10):
    CSV_URL = 'http://localhost:8091/stock/download/ticker/' + ticker + '?years=' + str(
        years) + '&interpolate=true&clean=true'
    df = read_csv(CSV_URL, header=0)
    df["date"] = pd.to_datetime(df["date"]).dt.strftime("%Y%m%d").astype(int)
    df.insert(0, 'ID', range(1, 1 + len(df)))
    print(CSV_URL + " " + str(df.size))

    return df
