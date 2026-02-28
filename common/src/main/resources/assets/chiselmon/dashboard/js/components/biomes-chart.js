/**
 * Biome doughnut chart.
 */

import { CHART_PALETTE } from '../config/chart-defaults.js';
import { fmtBiome } from '../core/format.js';
import { createChartLoader } from './chart-factory.js';

export const loadBiomes = createChartLoader(
    'chart-biomes',
    '/api/biomes',
    data => ({
        labels: data.map(d => fmtBiome(d.biome)),
        counts: data.map(d => d.count),
        colors: CHART_PALETTE.map(c => c + 'cc'),
    }),
    (labels, counts, colors) => ({
        type: 'doughnut',
        data: {
            labels,
            datasets: [{
                data: counts,
                backgroundColor: colors,
                borderColor: '#161b22',
                borderWidth: 2,
                hoverOffset: 6,
            }],
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'right',
                    labels: { boxWidth: 10, padding: 10, font: { size: 10 } },
                },
            },
        },
    }),
);