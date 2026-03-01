/**
 * Lightweight fetch wrapper for the dashboard API.
 */

import {state} from './state.js';

export async function api(path) {
    const r = await fetch(path);
    if (!r.ok) throw new Error(r.statusText);
    return r.json();
}

/**
 * Build a URL with query parameters, automatically including `from`
 * from the current state when it's non-zero.
 *
 *   buildUrl('/api/heatmap', { cx: 100, dimension: 'minecraft:the_nether' })
 *   → '/api/heatmap?from=17091…&cx=100&dimension=minecraft%3Athe_nether'
 */
export function buildUrl(path, params = {}) {
    const from = state.fromMs;
    const all = from > 0 ? { from, ...params } : { ...params };

    const entries = Object.entries(all);
    if (entries.length === 0) return path;

    const qs = entries
        .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
        .join('&');
    return `${path}?${qs}`;
}

/**
 * Simple version that only appends `from`. Kept for components
 * that don't need additional params.
 */
export function withFrom(path) {
    return buildUrl(path);
}