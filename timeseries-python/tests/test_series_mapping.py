import os
import sys
import unittest
from unittest.mock import patch, MagicMock
import pandas as pd

sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from analysis.series_mapping import align_series, get_mapped_series  # noqa: E402


class TestSeriesMapping(unittest.TestCase):
    def test_align_series_rebases_and_reindexes(self):
        source = pd.Series(
            [100, 110, 120],
            index=pd.to_datetime(["2023-01-01", "2023-01-02", "2023-01-04"]),
        )
        target = pd.Series(
            [50, 55, 53, 58],
            index=pd.to_datetime(
                ["2023-01-01", "2023-01-02", "2023-01-03", "2023-01-04"]
            ),
        )

        aligned = align_series(source, target)

        expected = pd.Series(
            [50, 55, 55, 60],
            index=pd.to_datetime(
                ["2023-01-01", "2023-01-02", "2023-01-03", "2023-01-04"]
            ),
            dtype=float,
        )

        pd.testing.assert_series_equal(aligned, expected)

    @patch("analysis.series_mapping.requests.post")
    def test_get_mapped_series_parses_response(self, mock_post):
        mock_response = MagicMock()
        mock_response.json.return_value = {
            "mapped": {
                "2023-01-01": 50,
                "2023-01-02": 55,
            }
        }
        mock_post.return_value = mock_response

        result = get_mapped_series("AAPL", "MSFT", years=1)

        self.assertEqual(list(result.index.astype(str)), ["2023-01-01", "2023-01-02"])
        self.assertEqual(result.iloc[0], 50)
        mock_post.assert_called_once()


if __name__ == "__main__":
    unittest.main()
