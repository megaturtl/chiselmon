/**
 * Loads and updates the last known player position for resetting the heatmap.
 */

import {api} from './api.js';

let _playerPos = null;

/** Fetches player position from the server (latest position and dimension) and populates the cache. */
export async function getPlayerPos() {
    try {
        _playerPos = await api('/api/playerpos');
    } catch (_) {
        // keep stale cache
    }
    return _playerPos;
}