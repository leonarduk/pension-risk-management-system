import pandas as pd

from integrations.stockfeed import timeseries


def test_get_name_map_from_csv(tmp_path):
    csv_path = tmp_path / "positions.csv"
    csv_path.write_text("Name,Symbol\nApple,AAPL\nTesla,TSLA\n")

    mapping = timeseries.get_name_map_from_csv(csv_path)

    assert mapping == {"Apple": "AAPL", "Tesla": "TSLA"}


def test_fetch_prices_for_tickers(monkeypatch):
    sample_data = {
        "ABC": {"2020-01-01": 1.0, "2020-01-02": 1.5},
        "XYZ": {"2020-01-01": 2.0, "2020-01-02": 2.5},
    }

    class DummyResponse:
        def __init__(self, payload):
            self._payload = payload

        def json(self):
            return self._payload

    def fake_post(url, *args, **kwargs):
        ticker = url.split("ticker=")[1].split("&")[0]
        return DummyResponse({ticker: sample_data[ticker]})

    monkeypatch.setattr(timeseries.requests, "post", fake_post)

    df = timeseries.fetch_prices_for_tickers({"ABC", "XYZ"}, years=1)

    assert set(df.columns) == {"ABC", "XYZ"}
    assert list(df.index) == [pd.Timestamp("2020-01-01"), pd.Timestamp("2020-01-02")]


def test_get_time_series_with_missing_data(monkeypatch):
    class DummyResponse:
        def __init__(self, payload):
            self._payload = payload

        def json(self):
            return self._payload

    def fake_post(url, *args, **kwargs):
        return DummyResponse({})

    monkeypatch.setattr(timeseries.requests, "post", fake_post)

    df = timeseries.get_time_series(["ABC"])

    assert df.empty
