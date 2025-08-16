import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { expect, test } from 'vitest';
import TickerTable from './TickerTable.jsx';

const sampleData = {
  AAA: { '2024-01-01': 1 },
  BBB: { '2024-01-01': 3 },
};

function Wrapper() {
  const [viewMode, setViewMode] = React.useState('total');
  return (
    <div>
      <button onClick={() => setViewMode(viewMode === 'total' ? 'relative' : 'total')}>
        toggle
      </button>
      <TickerTable data={sampleData} viewMode={viewMode} />
    </div>
  );
}

test('toggles between total and relative values', () => {
  render(<Wrapper />);
  expect(screen.getByText('1')).toBeTruthy();
  fireEvent.click(screen.getByText('toggle'));
  expect(screen.queryByText('1')).toBeNull();
  expect(screen.getByText('0.25')).toBeTruthy();
});
