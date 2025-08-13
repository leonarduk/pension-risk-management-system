import React, { useState } from 'react';
import PriceChart from './PriceChart.jsx';
import en from './i18n/en.json';
import es from './i18n/es.json';

export default function App() {
  const params = new URLSearchParams(window.location.search);
  const lang = params.get('lang') || 'en';
  const t = lang === 'es' ? es : en;

  const [tickers, setTickers] = useState('AAPL');
  const [data, setData] = useState({});
  const [error, setError] = useState(null);

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
      setError(null);
    } catch (e) {
      setError(t.fetchError);
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
      <h1>{t.title}</h1>
      <input
        type="text"
        value={tickers}
        onChange={(e) => setTickers(e.target.value)}
        placeholder={t.placeholder}
      />
      <button onClick={fetchData}>{t.load}</button>
      {error && <p>{error}</p>}
      <table>
        <thead>
          <tr>
            <th>{t.tickerHeader}</th>
            <th>{t.latestCloseHeader}</th>
          </tr>
        </thead>
        <tbody>{renderRows()}</tbody>
      </table>
      {chartLabels.length > 0 && (
        <div style={{ maxWidth: '600px' }}>
          <PriceChart labels={chartLabels} data={chartData} label={t.priceLabel} />
        </div>
      )}
    </div>
  );
}
