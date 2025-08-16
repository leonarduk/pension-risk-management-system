import React from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import { expect, test, vi, afterEach } from 'vitest';
import RiskReturnChart from './RiskReturnChart.jsx';

afterEach(() => {
  cleanup();
});

vi.mock('react-chartjs-2', () => ({
  Scatter: (props) => <pre data-testid="scatter-chart">{JSON.stringify(props)}</pre>,
}));

test('renders scatter chart with provided data', () => {
  const data = [{ ticker: 'AAPL', annual_std: 0.1, annual_return: 0.2 }];
  render(<RiskReturnChart data={data} />);
  const chart = screen.getByTestId('scatter-chart');
  expect(chart.textContent).toContain('"x":0.1');
  expect(chart.textContent).toContain('"y":0.2');
  expect(chart).toMatchSnapshot();
});

test('handles empty data', () => {
  render(<RiskReturnChart data={[]} />);
  expect(screen.getByTestId('scatter-chart')).toBeTruthy();
});
