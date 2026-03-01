/**
 * Loads world/server info from /api/info.
 * Populates the header world name and footer version once at boot.
 * Exposes a refresh-able getter for live data (last position, dimensions).
 */

import {api} from './api.js';

let _info = null;

/**
 * Initial load — called once at boot.
 * Sets header and footer, caches the info object.
 */
export async function loadWorldInfo() {
    try {
        _info = await api('/api/info');

        // Header: world name
        const prefix = _info.type === 'mp' ? '🌐 ' : '🌏 ';
        document.getElementById('world-name').textContent = prefix + _info.name;

        // Footer: version
        if (_info.version) {
            document.getElementById('footer-version').textContent = `chiselmon v${_info.version}`;
        }
    } catch (_) {
        // non-fatal
    }
}

/**
 * Fetches fresh info from the server (latest position, dimensions).
 * Updates the cache but does NOT re-render header/footer.
 */
export async function refreshWorldInfo() {
    try {
        _info = await api('/api/info');
    } catch (_) {
        // keep stale cache
    }
    return _info;
}

/** Returns the cached info object, or null if the load failed. */
export function getWorldInfo() {
    return _info;
}