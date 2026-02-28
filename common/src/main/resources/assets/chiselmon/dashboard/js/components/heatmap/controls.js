/**
 * Reads heatmap input values and updates status labels.
 * Also exports getCxCz / setCxCz used by the interaction handlers.
 */

import { CHUNKS_TO_BLOCKS, bestTileSize } from './grid.js';

export function getCxCz() {
    return {
        cx: parseInt(document.getElementById('hm-cx').value) || 0,
        cz: parseInt(document.getElementById('hm-cz').value) || 0,
    };
}

export function setCxCz(cx, cz) {
    document.getElementById('hm-cx').value = cx;
    document.getElementById('hm-cz').value = cz;
}

export function readHeatmapInputs() {
    const { cx, cz } = getCxCz();
    const chunkRadius   = Math.min(32, Math.max(2, parseInt(document.getElementById('hm-radius').value) || 8));
    const visibleRadius = chunkRadius * CHUNKS_TO_BLOCKS;
    const tileSetting   = document.getElementById('hm-tile-size').value;
    const tileSize      = tileSetting === 'auto' ? bestTileSize(visibleRadius) : parseInt(tileSetting);
    const dimension     = document.getElementById('hm-dimension').value || 'minecraft:overworld';
    return { cx, cz, visibleRadius, tileSize, dimension };
}

export function updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount) {
    document.getElementById('hm-label-tl').textContent = `${cx - visibleRadius}, ${cz - visibleRadius}`;
    document.getElementById('hm-label-br').textContent = `${cx + visibleRadius}, ${cz + visibleRadius}`;
    document.getElementById('hm-status').textContent   = `Showing ${encounterCount.toLocaleString()} encounters`;
}