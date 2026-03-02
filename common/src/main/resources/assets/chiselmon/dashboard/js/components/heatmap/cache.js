/**
 * Heatmap data cache.
 *
 * Fetches a large overscan region so panning doesn't require a reload.
 * Returns cached data directly when the requested view is fully contained
 * within the cached bounds. Cache is invalidated when the region extends
 * outside the bounds, the dimension changes, or the time range changes.
 */

import {api, buildUrl} from '../../core/api.js';
import {state} from '../../core/state.js';
import {OVERSCAN_FACTOR} from './grid.js';

let _cache = null;

function timeRangeKey() {
    return `${state.fromMs}:${state.toMs}`;
}

/** Decodes a flat interleaved array [x1,z1,x2,z2,...] into [x,z] tuples. */
function decodePairs(arr) {
    const out = [];
    for (let i = 0; i < arr.length; i += 2)
        out.push([arr[i], arr[i + 1]]);
    return out;
}

function isCacheHit(cx, cz, fetchRadius, dimension) {
    if (!_cache) return false;
    if (_cache.dimension !== dimension) return false;
    if (_cache.timeRangeKey !== timeRangeKey()) return false;
    const {minX, maxX, minZ, maxZ} = _cache.fetchBounds;
    return cx - fetchRadius >= minX && cx + fetchRadius <= maxX &&
        cz - fetchRadius >= minZ && cz + fetchRadius <= maxZ;
}

/**
 * Returns heatmap data for the given view, using the cache when possible.
 * @returns {Promise<{pokemon, player, fetchBounds}>}
 */
export async function getHeatmapData(cx, cz, visibleRadius, dimension) {
    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);

    if (isCacheHit(cx, cz, fetchRadius, dimension)) return _cache;

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

/** Clears the cache — call when the time range or dimension changes. */
export function invalidateHeatmapCache() {
    _cache = null;
}