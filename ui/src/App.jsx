import React, { useState } from 'react';
import PriceChart from './PriceChart.jsx';
import RiskReturnChart from './RiskReturnChart.jsx';
import TickerTable from './TickerTable.jsx';

export default function App() {
  const [tickers, setTickers] = useState('AAPL');
  const [data, setData] = useState({});
  const [riskData, setRiskData] = useState([]);
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
      const riskResp = await fetch(`/analytics/risk-return?tickers=${encodeURIComponent(tickers)}`);
      const riskJson = await riskResp.json();
      setRiskData(riskJson);
      setError(null);
    } catch (e) {
      setError('Failed to fetch data');
      setData({});
    }
  };

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
      {Object.keys(data).length > 0 && <TickerTable data={data} />}
      {chartLabels.length > 0 && (
        <div style={{ maxWidth: '600px' }}>
          <PriceChart labels={chartLabels} data={chartData} />
        </div>
      )}
      {riskData.length > 0 && (
        <div style={{ maxWidth: '600px', marginTop: '20px' }}>
          <RiskReturnChart data={riskData} />
        </div>
      )}
    </div>
  );
}
