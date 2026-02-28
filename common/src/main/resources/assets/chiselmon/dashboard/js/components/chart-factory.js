/**
 * Factory for simple Chart.js components that follow the
 * fetch → labels/counts → create-or-update pattern.
 */

import { api, buildUrl } from '../core/api.js';

/**
 * Creates a managed chart loader.
 *
 * @param {string}   canvasId   - DOM id of the <canvas>
 * @param {string}   endpoint   - API path, e.g. '/api/species'
 * @param {Function} transform  - (data) => { labels, counts, colors? }
 * @param {Function} chartOpts  - (labels, counts, colors) => Chart.js config
 * @returns {Function} async load function
 */
export function createChartLoader(canvasId, endpoint, transform, chartOpts) {
    let chart = null;

    return async function load() {
        const raw = await api(buildUrl(endpoint));
        const { labels, counts, colors } = transform(raw);

        if (chart) {
            chart.data.labels = labels;
            chart.data.datasets[0].data = counts;
            if (colors) chart.data.datasets[0].backgroundColor = colors;
            chart.update();
            return;
        }

        const ctx = document.getElementById(canvasId).getContext('2d');
        chart = new Chart(ctx, chartOpts(labels, counts, colors));
    };
}