import integrations


def get_time_series(ticker, use_stockfeed, xml_path):
    if use_stockfeed:
        df = integrations.stockfeed.timeseries.get_time_series(ticker=ticker, years=5)
    else:
        df = integrations.portfolioperformance.api.timeseries.get_time_series(
            ticker=ticker, years=5, xml_file=xml_path
        )
    print(df.tail(1))

    return df
