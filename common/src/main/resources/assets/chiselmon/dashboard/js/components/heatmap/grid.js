/**
 * Pure grid-math utilities for the heatmap.
 * No DOM access - just numbers in, numbers out.
 *
 * Grids are sparse Maps (cellIndex -> count) rather than dense Float32Arrays.
 * Memory and iteration cost scale with actual data, not grid area — which matters
 * a lot at large radii or small tile sizes where a dense grid would be huge.
 */

export const CHUNKS_TO_BLOCKS = 16;
export const OVERSCAN_FACTOR = 2.0;

export function bestTileSize(radiusBlocks) {
    const span = radiusBlocks * 2;
    if (span >= 512) return 16;
    if (span >= 256) return 8;
    if (span >= 128) return 4;
    if (span >= 64) return 2;
    return 1;
}

export function gridGeometry(cx, cz, visibleRadius, tileSize) {
    const span = visibleRadius * 2 * OVERSCAN_FACTOR;
    const canvasLeft = cx - span / 2;
    const canvasTop = cz - span / 2;
    return {
        span, canvasLeft, canvasTop,
        cells: Math.ceil(span / tileSize),
        minX: Math.floor(canvasLeft / tileSize) * tileSize,
        minZ: Math.floor(canvasTop / tileSize) * tileSize,
    };
}

/**
 * Builds a sparse grid: Map<cellIndex, count>.
 * Points outside the grid bounds are silently dropped.
 */
export function buildGrid(points, {minX, minZ, cells}, tileSize) {
    const grid = new Map();
    for (const [x, z] of points) {
        const col = Math.floor((x - minX) / tileSize);
        const row = Math.floor((z - minZ) / tileSize);
        if (col >= 0 && col < cells && row >= 0 && row < cells) {
            const idx = row * cells + col;
            grid.set(idx, (grid.get(idx) ?? 0) + 1);
        }
    }
    return grid;
}

/**
 * Finds the max count within the visible (non-overscan) region.
 * Iterates only populated cells — O(data) not O(grid area).
 */
export function visibleGridMax(grid, geom, cx, cz, visibleRadius, tileSize) {
    let max = 0;
    const {minX, minZ, cells} = geom;
    for (const [idx, count] of grid) {
        const col = idx % cells;
        const row = (idx - col) / cells;
        const worldX = minX + (col + 0.5) * tileSize;
        const worldZ = minZ + (row + 0.5) * tileSize;
        if (Math.abs(worldX - cx) <= visibleRadius && Math.abs(worldZ - cz) <= visibleRadius) {
            if (count > max) max = count;
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