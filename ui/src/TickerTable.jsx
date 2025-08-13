import React from 'react';

export default function TickerTable({ data }) {
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
