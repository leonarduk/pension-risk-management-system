import React from 'react';
import { render, screen, cleanup } from '@testing-library/react';
import { expect, test, vi, afterEach } from 'vitest';
import PriceChart from './PriceChart.jsx';

afterEach(() => {
  cleanup();
});

vi.mock('react-chartjs-2', () => ({
  Line: (props) => <pre data-testid="line-chart">{JSON.stringify(props)}</pre>,
}));

test('renders line chart with provided data', () => {
  const labels = ['Jan', 'Feb'];
  const data = [1, 2];
  render(<PriceChart labels={labels} data={data} label="Test Label" />);
  const chart = screen.getByTestId('line-chart');
  expect(chart.textContent).toContain('"labels":["Jan","Feb"]');
  expect(chart.textContent).toContain('"label":"Test Label"');
  expect(chart).toMatchSnapshot();
});

test('handles empty datasets gracefully', () => {
  render(<PriceChart labels={[]} data={[]} label="Empty" />);
  expect(screen.getByTestId('line-chart')).toBeTruthy();
});
