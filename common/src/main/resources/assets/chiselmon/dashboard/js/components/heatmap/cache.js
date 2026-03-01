/**
 * Heatmap data cache.
 *
 * The API fetches a large overscan region so panning doesn't require a reload.
 * This module holds the last fetched response and returns it directly if the
 * requested view is fully contained within the cached bounds — avoiding a
 * round-trip for every pan or minor zoom change.
 *
 * Cache is invalidated when:
 *   - The requested region extends outside the cached bounds
 *   - The dimension changes
 *   - The time range changes (detected via a cache key derived from state)
 */

import {api, buildUrl} from '../../core/api.js';
import {state} from '../../core/state.js';
import {OVERSCAN_FACTOR} from './grid.js';

/**
 * Decodes a flat interleaved int array [x1,z1,x2,z2,...] into [x,z] tuples.
 * Kept here alongside the cache so the raw API format is normalised in one place.
 */
function decodePairs(arr) {
    const out = [];
    for (let i = 0; i < arr.length; i += 2)
        out.push([arr[i], arr[i + 1]]);
    return out;
}


let _cache = null;

/**
 * Returns a string key representing the current time range.
 * Used to detect when state changes should bust the cache.
 */
function timeRangeKey() {
    return `${state.fromMs}:${state.toMs}`;
}

/**
 * Returns true if the cache covers the requested region entirely,
 * meaning no API call is needed.
 */
function isCacheHit(cx, cz, fetchRadius, dimension) {
    if (!_cache) return false;
    if (_cache.dimension !== dimension) return false;
    if (_cache.timeRangeKey !== timeRangeKey()) return false;

    const {minX, maxX, minZ, maxZ} = _cache.fetchBounds;
    return (
        cx - fetchRadius >= minX &&
        cx + fetchRadius <= maxX &&
        cz - fetchRadius >= minZ &&
        cz + fetchRadius <= maxZ
    );
}

/**
 * Gets heatmap data for the given view, using the cache if possible.
 *
 * @param {number} cx             - World X centre
 * @param {number} cz             - World Z centre
 * @param {number} visibleRadius  - Visible radius in blocks
 * @param {string} dimension      - Dimension key
 * @returns {Promise<{pokemon, player, fetchBounds}>}
 */
export async function getHeatmapData(cx, cz, visibleRadius, dimension) {
    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);

    if (isCacheHit(cx, cz, fetchRadius, dimension)) {
        return _cache;
    }

    const response = await api(buildUrl('/api/heatmap/', {cx, cz, radius: fetchRadius, dimension}));

    _cache = {
        pokemon: decodePairs(response.pokemon),
        player: decodePairs(response.player),
        fetchBounds: response.fetchBounds,
        dimension: response.dimension,
        timeRangeKey: timeRangeKey(),
    };

    return _cache;
}

/** Explicitly clears the cache — call when time range or dimension changes. */
export function invalidateHeatmapCache() {
    _cache = null;
}