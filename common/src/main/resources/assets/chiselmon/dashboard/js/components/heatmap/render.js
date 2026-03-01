/**
 * All canvas painting for the heatmap — data cells, chunk grid, legend.
 */

import {gridGeometry} from './grid.js';

// ── Config ────────────────────────────────────────────────────────────────────

const COLORS = {
    player: {r: 77, g: 201, b: 240},  // blue
    pokemon: {r: 249, g: 199, b: 79},   // yellow
};

const ALPHA = {
    min: 0.20,  // floor: any non-zero tile is at least this visible (0 to disable)
    playerMax: 0.70,  // ceiling for the player layer
    pokemonMax: 0.90,  // ceiling for the pokemon layer
    gamma: 0.60,  // curve: <1 lifts midtones, >1 suppresses them
};

// ── Canvas setup ──────────────────────────────────────────────────────────────

function setupCanvas(canvas, {square = true} = {}) {
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const w = Math.round(rect.width * dpr);
    const h = square ? w : Math.round(rect.height * dpr);
    canvas.width = w;
    canvas.height = h;
    return {ctx: canvas.getContext('2d'), size: w, h, dpr};
}

// ── Colour math ───────────────────────────────────────────────────────────────

function logNorm(val, max) {
    if (val <= 0 || max <= 0) return 0;
    return Math.log1p(val) / Math.log1p(max);
}

function intensityToAlpha(normalised, maxAlpha) {
    if (normalised <= 0) return 0;
    const curved = Math.pow(normalised, ALPHA.gamma);
    return ALPHA.min + curved * (maxAlpha - ALPHA.min);
}

// ── Drawing primitives ────────────────────────────────────────────────────────

function drawChunkGrid(ctx, size, canvasLeft, canvasTop, span, dpr) {
    ctx.strokeStyle = 'rgba(255,255,255,0.04)';
    ctx.lineWidth = dpr;
    const pxPerBlock = size / span;

    for (let x = Math.ceil(canvasLeft / 16) * 16; x <= canvasLeft + span; x += 16) {
        const lx = (x - canvasLeft) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(lx, 0);
        ctx.lineTo(lx, size);
        ctx.stroke();
    }
    for (let z = Math.ceil(canvasTop / 16) * 16; z <= canvasTop + span; z += 16) {
        const lz = (z - canvasTop) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(0, lz);
        ctx.lineTo(size, lz);
        ctx.stroke();
    }
}

/**
 * Draws all data cells using batched fillRect passes.
 *
 * Grids are sparse Maps so we only iterate cells that actually have data.
 * Rects sharing the same quantized alpha are batched into a single path+fill
 * to minimise compositor state changes.
 *
 * Two passes (player then pokemon) preserve the correct layer ordering.
 */
function drawDataCells(ctx, size, geom, pxPerBlock, hm) {
    const {pokGrid, plyGrid, pokMax, plyMax, tileSize} = hm;
    const {cells, minX, minZ} = geom;

    const cellPx = tileSize * pxPerBlock;
    const BUCKETS = 64;  // alpha quantization — 64 steps is imperceptible at normal sizes

    function paintLayer(grid, max, {r, g, b}, maxAlpha) {
        if (grid.size === 0) return;

        // Bucket rects by quantized alpha — avoids a fillStyle change per cell
        const buckets = new Array(BUCKETS);

        const originX = geom.canvasLeft * pxPerBlock;
        const originZ = geom.canvasTop * pxPerBlock;

        for (const [idx, count] of grid) {
            const col = idx % cells;
            const row = (idx - col) / cells;
            const x0 = (minX + col * tileSize) * pxPerBlock - originX;
            const y0 = (minZ + row * tileSize) * pxPerBlock - originZ;

            // Cull cells entirely outside the canvas
            if (x0 + cellPx < 0 || x0 >= size || y0 + cellPx < 0 || y0 >= size) continue;

            const norm = logNorm(count, max);
            const alpha = intensityToAlpha(norm, maxAlpha);
            const bucketIdx = Math.min(BUCKETS - 1, alpha * BUCKETS | 0);

            if (!buckets[bucketIdx]) buckets[bucketIdx] = [];
            buckets[bucketIdx].push(x0, y0);
        }

        for (let i = 0; i < BUCKETS; i++) {
            if (!buckets[i]) continue;
            const a = ((i + 0.5) / BUCKETS).toFixed(3);
            ctx.fillStyle = `rgba(${r},${g},${b},${a})`;
            ctx.beginPath();
            const rects = buckets[i];
            for (let j = 0; j < rects.length; j += 2)
                ctx.rect(rects[j], rects[j + 1], cellPx, cellPx);
            ctx.fill();
        }
    }

    paintLayer(plyGrid, plyMax, COLORS.player, ALPHA.playerMax);
    paintLayer(pokGrid, pokMax, COLORS.pokemon, ALPHA.pokemonMax);
}

function drawLegendBar(ctx, barX, barW, {r, g, b}, maxAlpha, label, labelX, midY) {
    const grad = ctx.createLinearGradient(barX, 0, barX + barW, 0);
    grad.addColorStop(0, `rgba(${r},${g},${b},0)`);
    grad.addColorStop(1, `rgba(${r},${g},${b},${maxAlpha})`);
    ctx.fillStyle = grad;
    ctx.fillRect(barX, midY - 5, barW, 10);
    ctx.fillStyle = '#8b949e';
    ctx.fillText(label, labelX, midY);
}

// ── Exports ───────────────────────────────────────────────────────────────────

export function paintHeatmap(canvas, cx, cz, hm) {
    const {ctx, size, dpr} = setupCanvas(canvas);
    const geom = gridGeometry(cx, cz, hm.radius, hm.tileSize);
    const pxPerBlock = size / geom.span;

    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);
    drawDataCells(ctx, size, geom, pxPerBlock, hm);
    drawChunkGrid(ctx, size, geom.canvasLeft, geom.canvasTop, geom.span, dpr);
}

export function paintLegend(canvas, hm) {
    const {ctx, size: w, h, dpr} = setupCanvas(canvas, {square: false});
    const midY = h / 2;
    const halfW = w / 2;
    const gap = 12 * dpr;
    const labelGap = 6 * dpr;

    ctx.font = `${9 * dpr | 0}px 'Space Mono', monospace`;
    ctx.textBaseline = 'middle';

    const pokLabel = String(hm.pokMax);
    const pokLabelW = ctx.measureText(pokLabel).width;
    const pokBarW = (halfW - gap / 2) - pokLabelW - labelGap;
    ctx.textAlign = 'left';
    drawLegendBar(ctx, 0, pokBarW, COLORS.pokemon, ALPHA.pokemonMax, pokLabel, pokBarW + labelGap, midY);

    const plyLabel = String(hm.plyMax);
    const plyLabelW = ctx.measureText(plyLabel).width;
    const plyBarW = (halfW - gap / 2) - plyLabelW - labelGap;
    const plyStart = halfW + gap / 2;
    ctx.textAlign = 'left';
    drawLegendBar(ctx, plyStart, plyBarW, COLORS.player, ALPHA.playerMax, plyLabel, plyStart + plyBarW + labelGap, midY);
}