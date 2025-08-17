import React from 'react';

export default function TickerTable({ data, viewMode = 'total' }) {
  const total = Object.values(data).reduce((sum, records) => {
    const latest = records[records.length - 1];
    const latestPrice = latest ? latest.Close : 0;
    return sum + latestPrice;
  }, 0);

  const renderRows = () =>
    Object.entries(data).map(([ticker, records]) => {
      const latest = records[records.length - 1];
      const latestPrice = latest ? latest.Close : 0;
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
