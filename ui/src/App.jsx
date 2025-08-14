import React, { useState } from 'react';
import PriceChart from './PriceChart.jsx';
import en from './i18n/en.json';
import es from './i18n/es.json';
import RiskReturnChart from './RiskReturnChart.jsx';
import TickerTable from './TickerTable.jsx';

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
      const params = new URLSearchParams();
      params.append('ticker', tickers);
      const response = await fetch('/stock/ticker?lang=' + lang, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
          'Accept-Language': lang,
        },
        body: params.toString(),
      });
      const json = await response.json();
      setData(json);
      const riskResp = await fetch(`/analytics/risk-return?tickers=${encodeURIComponent(tickers)}`);
      const riskJson = await riskResp.json();
      setRiskData(riskJson);
      setError(null);
    } catch (e) {
      setError(t.fetchError);
      setData({});
    }
  };

  const firstTicker = Object.keys(data)[0];
  const firstPrices = firstTicker ? data[firstTicker] : null;
  const chartLabels = firstPrices ? Object.keys(firstPrices) : [];
  const chartData = firstPrices ? Object.values(firstPrices) : [];

  return (
    <div>
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
      {Object.keys(data).length > 0 && (
        <TickerTable data={data} viewMode={viewMode} />
      )}
      {chartLabels.length > 0 && (
        <div style={{ maxWidth: '600px' }}>
          <PriceChart labels={chartLabels} data={chartData} label={t.priceLabel} />
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
