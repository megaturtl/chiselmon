/**
 * Shared formatting helpers.
 */

export function fmtTime(ms) {
    return new Date(ms).toLocaleString(undefined, {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
    });
}

export function fmtBiome(b) {
    return b ? b.replace(/^minecraft:/, '').replace(/_/g, ' ') : '–';
}

/** Strip the minecraft: namespace prefix (dimensions, blocks, etc.) */
export function stripNamespace(s) {
    return s ? s.replace(/^minecraft:/, '') : '–';
}