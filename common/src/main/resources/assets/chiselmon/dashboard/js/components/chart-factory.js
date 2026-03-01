/**
 * Factory for simple Chart.js components that follow the pattern:
 * fetch api -> unwrap response -> generate labels/counts -> create-or-update.
 */

import {api, buildUrl} from '../core/api.js';

/**
 * Creates a managed chart loader.
 *
 * @param {string}   canvasId      - DOM id of the <canvas>
 * @param {string}   endpoint      - API path, e.g. '/api/species'
 * @param {string}   responseKey   - Key to unwrap from the response object, e.g. 'species'
 * @param {Function} transform     - (entries) => { labels, counts, colors? }
 * @param {Function} chartOpts     - (labels, counts, colors) => Chart.js config
 * @returns {Function} async load function
 */
export function createChartLoader(canvasId, endpoint, responseKey, transform, chartOpts) {
    // Each loader gets its own chart instance so multiple charts don't interfere
    let chart = null;

    return async function load() {
        const response = await api(buildUrl(endpoint));
        const entries = response[responseKey];
        const {labels, counts, colors} = transform(entries);

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