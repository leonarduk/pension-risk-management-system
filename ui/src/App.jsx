import React, { useState } from 'react';
import PriceChart from './PriceChart.jsx';
import en from './i18n/en.json';
import es from './i18n/es.json';
import RiskReturnChart from './RiskReturnChart.jsx';
import TickerTable from './TickerTable.jsx';
import { showNavigation } from './config.js';

export default function App() {
  const params = new URLSearchParams(window.location.search);
  const lang = params.get('lang') || 'en';
  const t = lang === 'es' ? es : en;

  const [tickers, setTickers] = useState('AAPL');
  const [data, setData] = useState({});
  const [riskData, setRiskData] = useState([]);
  const [error, setError] = useState(null);
  const [viewMode, setViewMode] = useState('total');

  const fetchData = async () => {
    try {
      const tickerList = tickers.split(',').map((t) => t.trim()).filter(Boolean);
      const responses = await Promise.all(
        tickerList.map((t) => fetch(`/stock/ticker/${t}?lang=${lang}`))
      );
      const jsonData = await Promise.all(responses.map((r) => r.json()));
      const result = {};
      tickerList.forEach((t, idx) => {
        result[t] = jsonData[idx];
      });
      setData(result);
      const riskResp = await fetch(`/analytics/risk-return?tickers=${encodeURIComponent(tickers)}`);
      const riskJson = await riskResp.json();
      setRiskData(riskJson);
      setError(null);
    } catch (e) {
      setError(t.fetchError);
      setData({});
    }
  };

  const renderRows = () =>
    Object.entries(data).map(([ticker, records]) => {
      const latest = records[records.length - 1];
      const latestPrice = latest ? latest.Close : null;
      return (
        <tr key={ticker}>
          <td>{ticker}</td>
          <td>{latestPrice}</td>
        </tr>
      );
    });

  const firstTicker = Object.keys(data)[0];
  const firstRecords = firstTicker ? data[firstTicker] : null;
  const chartLabels = firstRecords ? firstRecords.map((r) => r.Date) : [];
  const chartData = firstRecords ? firstRecords.map((r) => r.Close) : [];

  return (
    <div>
      {showNavigation && (
        <nav>
          <a href="#ticker-table">Table</a>
          <a href="#price-chart">Price Chart</a>
          <a href="#risk-return">Risk/Return</a>
        </nav>
      )}
      <h1>{t.title}</h1>
      <input
        type="text"
        value={tickers}
        onChange={(e) => setTickers(e.target.value)}
        placeholder={t.placeholder}
      />
      <button onClick={fetchData}>{t.load}</button>
      <button onClick={() =>
        setViewMode(viewMode === 'total' ? 'relative' : 'total')
      }>
        View: {viewMode}
      </button>
      {error && <p>{error}</p>}
      <table id="ticker-table">
        <thead>
          <tr>
            <th>{t.tickerHeader}</th>
            <th>{t.latestCloseHeader}</th>
          </tr>
        </thead>
        <tbody>{renderRows()}</tbody>
      </table>
      {Object.keys(data).length > 0 && (
        <TickerTable data={data} viewMode={viewMode} />
      )}
      {chartLabels.length > 0 && (
        <div id="price-chart" style={{ maxWidth: '600px' }}>
          <PriceChart labels={chartLabels} data={chartData} label={t.priceLabel} />
        </div>
      )}
      {riskData.length > 0 && (
        <div
          id="risk-return"
          style={{ maxWidth: '600px', marginTop: '20px' }}
        >
          <RiskReturnChart data={riskData} />
        </div>
      )}
    </div>
  );
}
