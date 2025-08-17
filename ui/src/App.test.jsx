import React from 'react';
import { render, screen, fireEvent, cleanup } from '@testing-library/react';
import { expect, test, vi, beforeEach, afterEach } from 'vitest';
import App from './App.jsx';

vi.mock('react-chartjs-2', () => ({
  Line: () => <div data-testid="line-chart" />,
  Scatter: () => <div data-testid="scatter-chart" />,
}));

beforeEach(() => {
  vi.restoreAllMocks();
});

afterEach(() => {
  cleanup();
});

test('loads and displays data with charts and toggles view', async () => {
  const mockTickerData = [{ Date: '2024-01-01', Close: 100 }];
  const mockRiskData = [
    { ticker: 'AAPL', annual_std: 0.1, annual_return: 0.2 },
  ];
  global.fetch = vi
    .fn()
    .mockResolvedValueOnce({ json: () => Promise.resolve(mockTickerData) })
    .mockResolvedValueOnce({ json: () => Promise.resolve(mockRiskData) });

  const { asFragment } = render(<App />);

  fireEvent.change(
    screen.getByPlaceholderText('Enter comma-separated tickers'),
    { target: { value: 'AAPL' } }
  );
  fireEvent.click(screen.getByText('Load'));

  await screen.findAllByText('AAPL');
  expect(global.fetch).toHaveBeenCalledTimes(2);
  expect(screen.getAllByText('100')[0]).toBeTruthy();
  expect(screen.getByTestId('line-chart')).toBeTruthy();
  expect(screen.getByTestId('scatter-chart')).toBeTruthy();

  fireEvent.click(screen.getByText(/View:/));
  expect(screen.getByText('1.00')).toBeTruthy();

  expect(asFragment()).toMatchSnapshot();
});

test('shows error message on fetch failure', async () => {
  global.fetch = vi.fn().mockRejectedValue(new Error('fail'));

  render(<App />);

  fireEvent.change(
    screen.getByPlaceholderText('Enter comma-separated tickers'),
    { target: { value: 'AAPL' } }
  );
  fireEvent.click(screen.getByText('Load'));

  await screen.findByText('Failed to fetch data');
  expect(screen.queryByTestId('line-chart')).toBeNull();
  expect(screen.queryByText('AAPL')).toBeNull();
});
