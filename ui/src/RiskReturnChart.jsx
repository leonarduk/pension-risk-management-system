import {
  Chart as ChartJS,
  LinearScale,
  PointElement,
  Tooltip,
  Legend,
} from 'chart.js';
import { Scatter } from 'react-chartjs-2';

ChartJS.register(LinearScale, PointElement, Tooltip, Legend);

export default function RiskReturnChart({ data }) {
  const chartData = {
    datasets: [
      {
        label: 'Risk vs Return',
        data: data.map((d) => ({ x: d.annual_std, y: d.annual_return, label: d.ticker })),
        backgroundColor: 'rgb(75, 192, 192)',
      },
    ],
  };

  const options = {
    plugins: {
      tooltip: {
        callbacks: {
          label: (ctx) => {
            const { label, x, y } = ctx.raw;
            return `${label}: σ=${(x * 100).toFixed(2)}%, μ=${(y * 100).toFixed(2)}%`;
          },
        },
      },
    },
    scales: {
      x: { title: { display: true, text: 'Annual Std Dev' } },
      y: { title: { display: true, text: 'Annual Return' } },
    },
  };

  return <Scatter data={chartData} options={options} />;
}
