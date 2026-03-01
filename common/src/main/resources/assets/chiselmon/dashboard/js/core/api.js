/**
 * Lightweight fetch wrapper for the dashboard API.
 */

import {state} from './state.js';

export async function api(path) {
    const response = await fetch(path);
    if (!response.ok) throw new Error(response.statusText);
    return response.json();
}

/**
 * Builds a URL with query parameters, automatically including the current
 * time range (from/to) from state. Any extra params are merged in after.
 *
 *   buildUrl('/api/heatmap', { cx: 100, dimension: 'minecraft:the_nether' })
 *   -> '/api/heatmap?from=17091…&to=17092…&cx=100&dimension=minecraft%3Athe_nether'
 */
export function buildUrl(path, params = {}) {
    const timeParams = {};
    if (state.fromMs > 0) timeParams.from = state.fromMs;
    if (state.toMs > 0) timeParams.to = state.toMs;

    const all = {...timeParams, ...params};
    const entries = Object.entries(all);
    if (entries.length === 0) return path;

    const query = entries
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
    return `${path}?${query}`;
}