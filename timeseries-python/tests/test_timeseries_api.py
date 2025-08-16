import unittest
from unittest.mock import patch, MagicMock

import sys
import os
import pytest

pytest.importorskip("pypfopt")

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))
from analysis.portfolio.timeseries_api import get_time_series  # noqa: E402


class TestGetTimeSeries(unittest.TestCase):
    @patch("analysis.portfolio.timeseries_api.requests.post")
    def test_fetches_data_for_single_ticker(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "AAPL": {"2023-01-01": 150, "2023-01-02": 155}
        }
        mock_post.return_value = mock_response

        result = get_time_series(ticker="AAPL", years=1)

        self.assertEqual(result.index.name, "Date")
        self.assertIn("AAPL", result.columns)
        self.assertEqual(result.loc["2023-01-01", "AAPL"], 150)

    @patch("analysis.portfolio.timeseries_api.requests.post")
    def test_fetches_data_for_multiple_tickers(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "AAPL": {"2023-01-01": 150, "2023-01-02": 155},
            "MSFT": {"2023-01-01": 250, "2023-01-02": 255},
        }
        mock_post.return_value = mock_response

        result = get_time_series(ticker=["AAPL", "MSFT"], years=1)

        self.assertIn("AAPL", result.columns)
        self.assertIn("MSFT", result.columns)
        self.assertEqual(result.loc["2023-01-01", "AAPL"], 150)
        self.assertEqual(result.loc["2023-01-01", "MSFT"], 250)

    @patch("analysis.portfolio.timeseries_api.requests.post")
    def test_returns_empty_dataframe_when_no_data(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.return_value = {}
        mock_post.return_value = mock_response

        result = get_time_series(ticker="INVALID", years=1)

        self.assertTrue(result.empty)

    @patch("analysis.portfolio.timeseries_api.requests.post")
    def test_skips_tickers_with_no_data(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "AAPL": {"2023-01-01": 150, "2023-01-02": 155}
        }
        mock_post.return_value = mock_response

        result = get_time_series(ticker=["AAPL", "INVALID"], years=1)

        self.assertIn("AAPL", result.columns)
        self.assertNotIn("INVALID", result.columns)

    @patch("analysis.portfolio.timeseries_api.requests.post")
    def test_handles_invalid_json_response(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.side_effect = ValueError("Invalid JSON")
        mock_post.return_value = mock_response

        with self.assertRaises(ValueError):
            get_time_series(ticker="AAPL", years=1)
