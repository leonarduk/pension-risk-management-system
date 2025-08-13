import pandas as pd
import requests

DATE = "Date"


def align_series(
    source: pd.Series, target: pd.Series, method: str = "ffill"
) -> pd.Series:
    """Align one time series to another using index rebasing.

    The source series is scaled so that its value matches the target
    at the first overlapping date and then reindexed to the target's
    index.

    Args:
        source (pd.Series): Series to be aligned.
        target (pd.Series): Series providing the desired index.
        method (str, optional): Method used when reindexing. Defaults to "ffill".

    Returns:
        pd.Series: Source series rebased and indexed like the target.
    """
    intersection = source.index.intersection(target.index)
    if intersection.empty:
        raise ValueError("No overlapping dates to align series")

    start = intersection.min()
    scale = target.loc[start] / source.loc[start]
    rebased = source * scale

    return rebased.reindex(target.index, method=method)


def get_mapped_series(
    source_ticker: str, target_ticker: str, years: int = 0
) -> pd.Series:
    """Fetch a mapped series from the timeseries service.

    Args:
        source_ticker (str): Ticker to map.
        target_ticker (str): Ticker providing the target index.
        years (int, optional): Limit of historical data. Defaults to 0 (all).

    Returns:
        pd.Series: Mapped source series aligned to the target's index.
    """
    params = f"source={source_ticker}&target={target_ticker}"
    if years > 0:
        params += f"&years={years}"
    url = f"http://localhost:8091/series/map?{params}"

    response = requests.post(url=url)
    data = response.json()
    mapped_key = data.get("mapped") or data.get(source_ticker)
    if not mapped_key:
        return pd.Series(dtype=float)

    df = pd.DataFrame(mapped_key.items(), columns=[DATE, source_ticker])
    df[DATE] = pd.to_datetime(df[DATE])
    df.set_index(DATE, inplace=True)
    return df[source_ticker]
