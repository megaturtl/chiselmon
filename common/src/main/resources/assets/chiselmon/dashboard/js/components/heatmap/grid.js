/**
 * Pure grid-math utilities for the heatmap.
 * No DOM access — just numbers in, numbers out.
 */

export const CHUNKS_TO_BLOCKS = 16;
export const OVERSCAN_FACTOR  = 2.0;

export function bestTileSize(radiusBlocks) {
    const span = radiusBlocks * 2;
    if (span >= 512) return 16;
    if (span >= 256) return 8;
    if (span >= 128) return 4;
    if (span >= 64)  return 2;
    return 1;
}

export function gridGeometry(cx, cz, visibleRadius, tileSize) {
    const span       = visibleRadius * 2 * OVERSCAN_FACTOR;
    const canvasLeft = cx - span / 2;
    const canvasTop  = cz - span / 2;
    return {
        span, canvasLeft, canvasTop,
        cells: Math.ceil(span / tileSize),
        minX:  Math.floor(canvasLeft / tileSize) * tileSize,
        minZ:  Math.floor(canvasTop  / tileSize) * tileSize,
    };
}

export function buildGrid(points, { minX, minZ, cells }, tileSize) {
    const grid = new Float32Array(cells * cells);
    for (const [x, z] of points) {
        const col = Math.floor((x - minX) / tileSize);
        const row = Math.floor((z - minZ) / tileSize);
        if (col >= 0 && col < cells && row >= 0 && row < cells)
            grid[row * cells + col]++;
    }
    return grid;
}

export function visibleGridMax(grid, geom, cx, cz, visibleRadius, tileSize) {
    let max = 0;
    const { minX, minZ, cells } = geom;
    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const worldX = minX + (col + 0.5) * tileSize;
            const worldZ = minZ + (row + 0.5) * tileSize;
            if (Math.abs(worldX - cx) > visibleRadius || Math.abs(worldZ - cz) > visibleRadius) continue;
            const v = grid[row * cells + col];
            if (v > max) max = v;
        }
    }
    return max || 1;
}

export function countVisibleEncounters(points, cx, cz, visibleRadius) {
    let count = 0;
    for (const [x, z] of points) {
        if (Math.abs(x - cx) <= visibleRadius && Math.abs(z - cz) <= visibleRadius)
            count++;
    }
    return count;
}