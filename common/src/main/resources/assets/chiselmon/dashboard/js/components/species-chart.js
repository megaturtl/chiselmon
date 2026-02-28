/**
 * Top-species horizontal bar chart.
 */

import { CHART_PALETTE } from '../config/chart-defaults.js';
import { createChartLoader } from './chart-factory.js';

export const loadSpecies = createChartLoader(
    'chart-species',
    '/api/species',
    data => ({
        labels: data.map(d => d.species),
        counts: data.map(d => d.count),
        colors: data.map((_, i) => CHART_PALETTE[i % CHART_PALETTE.length] + 'cc'),
    }),
    (labels, counts, colors) => ({
        type: 'bar',
        data: {
            labels,
            datasets: [{
                data: counts,
                backgroundColor: colors,
                borderWidth: 0,
                borderRadius: 3,
            }],
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                x: { beginAtZero: true, ticks: { precision: 0 } },
                y: { ticks: { font: { size: 10 } } },
            },
        },
    }),
);