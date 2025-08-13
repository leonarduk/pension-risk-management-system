import pandas as pd
import requests

DATE = "Date"


def align_series(
    source: pd.Series, target: pd.Series, method: str = "ffill"
) -> pd.Series:
    """Align one time series to another using index rebasing.

    The function scales ``source`` so that the first common date matches
    the value of ``target`` on that date. The rebased series is then
    reindexed to ``target`` using the provided fill method.

    Args:
        source: Series that will be rebased.
        target: Series providing the desired index.
        method: Reindex fill method. Defaults to ``"ffill"``.

    Returns:
        pd.Series: ``source`` rebased and aligned to ``target``.
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
