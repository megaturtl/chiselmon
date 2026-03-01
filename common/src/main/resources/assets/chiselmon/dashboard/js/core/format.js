export function fmtTime(ms) {
    return new Date(ms).toLocaleString(undefined, {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit',
    });
}

export function fmtBiome(biome) {
    return stripNamespace(biome);
}

/** Strips any namespace prefix (e.g., 'minecraft:', 'cobblemon:') */
export function stripNamespace(str) {
    return str ? str.replace(/^[^:]+:/, '') : '–';
}