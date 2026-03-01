/**
 * Loads world/server context from /api/info.
 * Populates the header world name and footer version once at boot.
 */

import {api} from './api.js';

let _context = null;

/**
 * Initial context load once when the page is initialised.
 * Sets world info and mod version, this should not change with a simple data refresh.
 */
export async function loadContext() {
    try {
        _context = await api('/api/context/');

        // Set world/server name in header
        const prefix = _context.worldType === 'mp' ? '🌐 ' : '🌏 ';
        document.getElementById('world-name').textContent = prefix + _context.worldName;

        // Set mod version in footer
        if (_context.modVersion) {
            document.getElementById('footer-version').textContent = `chiselmon v${_context.modVersion}`;
        }
    } catch (_) {
        // non-fatal
    }
}