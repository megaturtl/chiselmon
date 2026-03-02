/**
 * Species bar chart.
 */

import {CHART_PALETTE} from '../config/chart-defaults.js';
import {createChartLoader} from './chart-factory.js';

export const loadSpecies = createChartLoader(
    'chart-species',
    '/api/species/',
    'species',
    entries => ({
        labels: entries.map(entry => entry.species),
        counts: entries.map(entry => entry.count),
        // 'cc' adds a bit of transparency
        colors: entries.map((_, i) => CHART_PALETTE[i % CHART_PALETTE.length] + 'cc'),
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
            plugins: {legend: {display: false}},
            scales: {
                x: {beginAtZero: true, ticks: {precision: 0}},
                y: {ticks: {font: {size: 10}}},
            },
        },
    }),
);