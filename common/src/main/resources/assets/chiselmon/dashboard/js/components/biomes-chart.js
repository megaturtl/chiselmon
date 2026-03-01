/**
 * Biome doughnut chart.
 */

import {CHART_PALETTE} from '../config/chart-defaults.js';
import {fmtBiome} from '../core/format.js';
import {createChartLoader} from './chart-factory.js';

export const loadBiomes = createChartLoader(
    'chart-biomes',
    '/api/biomes/',
    'biomes',
    entries => ({
        labels: entries.map(entry => fmtBiome(entry.biome)),
        counts: entries.map(entry => entry.count),
        // Pre-map the full palette rather than cycling per-entry,
        // since slices are fixed in number and map 1:1 with palette entries
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
                    labels: {boxWidth: 10, padding: 10, font: {size: 10}},
                },
            },
        },
    }),
);