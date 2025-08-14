import React from 'react';

export default function TickerTable({ data, viewMode = 'total' }) {
  const total = Object.values(data).reduce((sum, prices) => {
    const dates = Object.keys(prices);
    const latestDate = dates[dates.length - 1];
    const latestPrice = prices[latestDate];
    return sum + latestPrice;
  }, 0);

  const renderRows = () =>
    Object.entries(data).map(([ticker, prices]) => {
      const dates = Object.keys(prices);
      const latestDate = dates[dates.length - 1];
      const latestPrice = prices[latestDate];
      const value =
        viewMode === 'relative' && total > 0
          ? (latestPrice / total).toFixed(2)
          : latestPrice;
      return (
        <tr key={ticker}>
          <td>{ticker}</td>
          <td>{value}</td>
        </tr>
      );
    });

  return (
    <table>
      <thead>
        <tr>
          <th>Ticker</th>
          <th>Latest Close</th>
        </tr>
      </thead>
      <tbody>{renderRows()}</tbody>
    </table>
  );
}
