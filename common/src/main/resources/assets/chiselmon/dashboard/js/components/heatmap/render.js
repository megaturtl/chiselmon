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
    gamma: 0.60,  // curve applied after log-norm: <1 lifts midtones, >1 suppresses them
};

// ── Canvas setup ──────────────────────────────────────────────────────────────

function setupCanvas(canvas, {square = true, willReadFrequently = false} = {}) {
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const w = Math.round(rect.width * dpr);
    const h = square ? w : Math.round(rect.height * dpr);
    canvas.width = w;
    canvas.height = h;
    return {ctx: canvas.getContext('2d', {willReadFrequently}), size: w, h, dpr};
}

// ── Colour math ───────────────────────────────────────────────────────────────

// log1p(x) = log(1+x). Dividing by log1p(max) maps [0..max] → [0..1] on a log
// scale, so low-count tiles aren't crushed to near-zero opacity.
function logNorm(val, max) {
    if (val <= 0 || max <= 0) return 0;
    return Math.log1p(val) / Math.log1p(max);
}

// Maps a normalised intensity to a paint alpha within [ALPHA.min, maxAlpha].
function intensityToAlpha(normalised, maxAlpha) {
    if (normalised <= 0) return 0;
    const curved = Math.pow(normalised, ALPHA.gamma);
    return ALPHA.min + curved * (maxAlpha - ALPHA.min);
}

// Returns an [R, G, B, A] byte tuple for a cell.
// Pokemon (yellow) is composited on top of player (blue) via Porter-Duff "over":
//   outA   = srcA + dstA·(1−srcA)
//   outRGB = (srcRGB·srcA + dstRGB·dstA·(1−srcA)) / outA
function blendCellColor(pokVal, plyVal) {
    let r = 0, g = 0, b = 0, a = 0;

    const plyAlpha = intensityToAlpha(plyVal, ALPHA.playerMax);
    if (plyAlpha > 0) {
        ({r, g, b} = COLORS.player);
        [r, g, b, a] = [r * plyAlpha, g * plyAlpha, b * plyAlpha, plyAlpha];
    }

    const pokAlpha = intensityToAlpha(pokVal, ALPHA.pokemonMax);
    if (pokAlpha > 0) {
        const {r: pr, g: pg, b: pb} = COLORS.pokemon;
        const carry = a * (1 - pokAlpha);   // surviving fraction of the player layer
        const outA = pokAlpha + carry;
        if (outA > 0) {
            r = (pr * pokAlpha + r * carry) / outA;
            g = (pg * pokAlpha + g * carry) / outA;
            b = (pb * pokAlpha + b * carry) / outA;
            a = outA;
        }
    }

    return [r + 0.5 | 0, g + 0.5 | 0, b + 0.5 | 0, Math.min(255, a * 255 + 0.5 | 0)];
}

// ── Drawing primitives ────────────────────────────────────────────────────────

function stampCell(px, size, x0, y0, cellPx, R, G, B, A) {
    const x1 = Math.min(size, x0 + Math.ceil(cellPx));
    const y1 = Math.min(size, y0 + Math.ceil(cellPx));
    for (let py = Math.max(0, y0); py < y1; py++) {
        for (let qx = Math.max(0, x0); qx < x1; qx++) {
            const i = (py * size + qx) * 4;
            px[i] = R;
            px[i + 1] = G;
            px[i + 2] = B;
            px[i + 3] = A;
        }
    }
}

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

function drawDataCells(ctx, size, geom, pxPerBlock, hm) {
    const {pokGrid, plyGrid, pokMax, plyMax, tileSize} = hm;
    const {cells, minX, minZ, canvasLeft, canvasTop} = geom;
    const cellPx = tileSize * pxPerBlock;
    const imgData = ctx.getImageData(0, 0, size, size);

    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const idx = row * cells + col;
            const pokVal = logNorm(pokGrid[idx], pokMax);
            const plyVal = logNorm(plyGrid[idx], plyMax);
            if (!pokVal && !plyVal) continue;

            const x0 = Math.floor((minX + col * tileSize - canvasLeft) * pxPerBlock);
            const y0 = Math.floor((minZ + row * tileSize - canvasTop) * pxPerBlock);
            stampCell(imgData.data, size, x0, y0, cellPx, ...blendCellColor(pokVal, plyVal));
        }
    }

    ctx.putImageData(imgData, 0, 0);
}

function drawLegendBar(ctx, barX, barW, {r, g, b}, maxAlpha, label, labelX, midY) {
    // Draw the gradient bar
    const grad = ctx.createLinearGradient(barX, 0, barX + barW, 0);
    grad.addColorStop(0, `rgba(${r},${g},${b},0)`);
    grad.addColorStop(1, `rgba(${r},${g},${b},${maxAlpha})`);

    ctx.fillStyle = grad;
    ctx.fillRect(barX, midY - 5, barW, 10);

    // Draw the label (using the labelX provided)
    ctx.fillStyle = '#8b949e';
    ctx.fillText(label, labelX, midY);
}

// ── Exports ───────────────────────────────────────────────────────────────────

export function paintHeatmap(canvas, cx, cz, hm) {
    const {ctx, size, dpr} = setupCanvas(canvas, {willReadFrequently: true});
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
    const gap = 12 * dpr;      // Gap between the two legend groups
    const labelGap = 6 * dpr;  // Gap between a bar and its number

    ctx.font = `${9 * dpr | 0}px 'Space Mono', monospace`;
    ctx.textBaseline = 'middle';

    // Pokemon Legend
    const pokLabel = String(hm.pokMax);
    const pokLabelW = ctx.measureText(pokLabel).width;
    // Bar takes up remaining space in the left half minus gaps
    const pokBarW = (halfW - (gap / 2)) - pokLabelW - labelGap;

    ctx.textAlign = 'left';
    drawLegendBar(ctx, 0, pokBarW, COLORS.pokemon, ALPHA.pokemonMax, pokLabel, pokBarW + labelGap, midY);

    // Player Legend
    const plyLabel = String(hm.plyMax);
    const plyLabelW = ctx.measureText(plyLabel).width;
    const plyBarW = (halfW - (gap / 2)) - plyLabelW - labelGap;
    const plyStart = halfW + (gap / 2);

    ctx.textAlign = 'left';
    drawLegendBar(ctx, plyStart, plyBarW, COLORS.player, ALPHA.playerMax, plyLabel, plyStart + plyBarW + labelGap, midY);
}