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
 * Build a URL with query parameters, automatically including the `from` time param
 * in the current state.
 *
 *   buildUrl('/api/heatmap', { cx: 100, dimension: 'minecraft:the_nether' })
 *   -> '/api/heatmap?from=17091…&cx=100&dimension=minecraft%3Athe_nether'
 */
export function buildUrl(path, params = {}) {
    const from = state.fromMs;
    const all = from > 0 ? {from, ...params} : {...params};

    const entries = Object.entries(all);
    if (entries.length === 0) return path;

    const queryParams = entries
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
    return `${path}?${queryParams}`;
}

/**
 * Just appends the `from` time param.
 */
export function withFrom(path) {
    return buildUrl(path);
}