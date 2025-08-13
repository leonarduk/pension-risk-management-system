import React, { useState } from 'react';
import PriceChart from './PriceChart.jsx';

export default function App() {
  const [tickers, setTickers] = useState('AAPL');
  const [data, setData] = useState({});
  const [error, setError] = useState(null);

  const fetchData = async () => {
    try {
      const params = new URLSearchParams();
      params.append('ticker', tickers);
      const response = await fetch('/stock/ticker', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString(),
      });
      const json = await response.json();
      setData(json);
      setError(null);
    } catch (e) {
      setError('Failed to fetch data');
    }
  };

  const renderRows = () =>
    Object.entries(data).map(([ticker, prices]) => {
      const dates = Object.keys(prices);
      const latestDate = dates[dates.length - 1];
      const latestPrice = prices[latestDate];
      return (
        <tr key={ticker}>
          <td>{ticker}</td>
          <td>{latestPrice}</td>
        </tr>
      );
    });

  const firstTicker = Object.keys(data)[0];
  const firstPrices = firstTicker ? data[firstTicker] : null;
  const chartLabels = firstPrices ? Object.keys(firstPrices) : [];
  const chartData = firstPrices ? Object.values(firstPrices) : [];

  return (
    <div>
      <h1>Pension Risk Management</h1>
      <input
        type="text"
        value={tickers}
        onChange={(e) => setTickers(e.target.value)}
        placeholder="Enter comma-separated tickers"
      />
      <button onClick={fetchData}>Load</button>
      {error && <p>{error}</p>}
      <table>
        <thead>
          <tr>
            <th>Ticker</th>
            <th>Latest Close</th>
          </tr>
        </thead>
        <tbody>{renderRows()}</tbody>
      </table>
      {chartLabels.length > 0 && (
        <div style={{ maxWidth: '600px' }}>
          <PriceChart labels={chartLabels} data={chartData} />
        </div>
      )}
    </div>
  );
}
