/**
 * All canvas painting for the heatmap — data cells, chunk grid, legend.
 */

import {gridGeometry} from './grid.js';

// ── Canvas setup ──────────────────────────────────────────────────────────────

function setupCanvas(canvas, {square = true, willReadFrequently = false} = {}) {
    const dpr  = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const w = Math.round(rect.width * dpr);
    const h = square ? w : Math.round(rect.height * dpr);
    canvas.width  = w;
    canvas.height = h;
    return {ctx: canvas.getContext('2d', {willReadFrequently}), size: w, h, dpr};
}

// ── Colour blending ───────────────────────────────────────────────────────────

function blendCellColor(pokVal, plyVal) {
    let r = 0, g = 0, b = 0, a = 0;

    if (plyVal > 0) {
        // Math: pa is the "paint alpha" — how strongly this color stamps onto the canvas.
        //
        // plyVal is already log-normalised (0..1), so we raise it to the power 0.6
        // to apply a mild additional curve. Lower exponents (< 1) brighten the midrange
        // further; higher exponents (> 1) would push values back toward zero.
        //
        // Without any curve:  pa = plyVal        → linear, dim low values
        // Old code used 0.4:  pa = plyVal^0.4    → aggressive boost, but log wasn't applied first
        // New code uses 0.6:  pa = plyVal^0.6    → gentler boost on top of the log scale
        //
        // The * 0.8 caps the maximum alpha so even the hottest pokemon tile doesn't
        // completely overpower the player layer if both are present.
        const pa = Math.pow(plyVal, 0.6) * 0.8;
        [r, g, b, a] = [77 * pa, 201 * pa, 240 * pa, pa];
    }
    if (pokVal > 0) {
        // Same curve logic as above, capped at 0.7 instead of 0.8 so player positions
        // read slightly brighter than pokemon spawns at equal intensity.
        const pa = Math.pow(pokVal, 0.6) * 0.7;

        // Standard "painter's algorithm" alpha compositing (Porter-Duff "over"):
        //   outA  = srcA + dstA * (1 - srcA)
        //   outRGB = (srcRGB * srcA + dstRGB * dstA * (1 - srcA)) / outA
        //
        // This lets the pokemon (yellow) layer blend on top of any player (blue)
        // colour that was already accumulated above, producing a green-ish mix
        // in tiles where both were active.
        const outA = pa + a * (1 - pa);
        if (outA > 0) {
            r = (249 * pa + r * a * (1 - pa)) / outA;
            g = (199 * pa + g * a * (1 - pa)) / outA;
            b = (79 * pa + b * a * (1 - pa)) / outA;
            a = outA;
        }
    }

    // Convert float channels (0..1 range for alpha, 0..255 range for RGB) to
    // integer bytes. The `| 0` truncates; the + 0.5 before it rounds correctly.
    // Alpha is additionally clamped to 255 in case compositing nudged it over.
    return [r + 0.5 | 0, g + 0.5 | 0, b + 0.5 | 0, Math.min(255, a * 255 + 0.5 | 0)];
}

function stampCell(px, size, x0, y0, cellPx, R, G, B, A) {
    const x1 = Math.min(size, x0 + Math.ceil(cellPx));
    const y1 = Math.min(size, y0 + Math.ceil(cellPx));
    for (let py = Math.max(0, y0); py < y1; py++) {
        const base = py * size * 4;
        for (let qx = Math.max(0, x0); qx < x1; qx++) {
            const i = base + qx * 4;
            px[i] = R;
            px[i + 1] = G;
            px[i + 2] = B;
            px[i + 3] = A;
        }
    }
}

// ── Grid lines ────────────────────────────────────────────────────────────────

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

// ── Normalisation ─────────────────────────────────────────────────────────────

// Math: why log scale?
//
// Linear normalisation:  val / max
//   A tile with 3 visits out of a max of 400 maps to 3/400 = 0.0075 — nearly invisible.
//
// Log normalisation:  log(1 + val) / log(1 + max)
//   The same tile maps to log(4) / log(401) ≈ 1.386 / 5.994 ≈ 0.23 — clearly visible.
//
// log1p(x) is just log(1 + x). The +1 serves two purposes:
//   1. It keeps the function defined and returning 0 when val = 0 (log(0) is -∞).
//   2. It ensures the denominator is always > 0 even when max = 0.
//
// The key insight is that logarithms compress large ranges: the jump from 1→10
// looks the same size as 10→100 or 100→1000. That means sparse tiles are lifted
// out of near-invisibility while the hottest tiles are gently pulled back,
// preserving the relative ordering without letting one outlier dominate everything.
function logNorm(val, max) {
    if (val <= 0 || max <= 0) return 0;
    return Math.log1p(val) / Math.log1p(max);
}

// ── Data cells ────────────────────────────────────────────────────────────────

function drawDataCells(ctx, size, geom, pxPerBlock, hm) {
    const {pokGrid, plyGrid, pokMax, plyMax, tileSize} = hm;
    const {cells, minX, minZ, canvasLeft, canvasTop} = geom;
    const cellPx = tileSize * pxPerBlock;
    const imgData = ctx.getImageData(0, 0, size, size);

    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const idx = row * cells + col;

            // Log-normalise each raw count to a 0..1 value.
            // Previously this was a plain linear division (pokGrid[idx] / pokMax)
            // which crushed low-count tiles to near-zero opacity.
            const pokVal = logNorm(pokGrid[idx], pokMax);
            const plyVal = logNorm(plyGrid[idx], plyMax);
            if (!pokVal && !plyVal) continue;

            const x0 = Math.floor((minX + col * tileSize - canvasLeft) * pxPerBlock);
            const y0 = Math.floor((minZ + row * tileSize - canvasTop) * pxPerBlock);
            const [R, G, B, A] = blendCellColor(pokVal, plyVal);
            stampCell(imgData.data, size, x0, y0, cellPx, R, G, B, A);
        }
    }

    ctx.putImageData(imgData, 0, 0);
}

// ── Public paint functions ────────────────────────────────────────────────────

export function paintHeatmap(canvas, cx, cz, hm) {
    const {ctx, size, dpr} = setupCanvas(canvas, {willReadFrequently: true});
    const geom = gridGeometry(cx, cz, hm.radius, hm.tileSize);
    const pxPerBlock = size / geom.span;

    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);

    drawDataCells(ctx, size, geom, pxPerBlock, hm);
    drawChunkGrid(ctx, size, geom.canvasLeft, geom.canvasTop, geom.span, dpr);
}

function drawLegendBar(ctx, x, width, color, label, labelX, midY) {
    const grad = ctx.createLinearGradient(x, 0, x + width, 0);
    grad.addColorStop(0, color.replace('rgb', 'rgba').replace(')', ',0)'));
    grad.addColorStop(1, color.replace('rgb', 'rgba').replace(')', ',0.85)'));
    ctx.fillStyle = grad;
    ctx.fillRect(x, midY - 5, width, 10);
    ctx.fillStyle = '#8b949e';
    ctx.fillText(label, labelX, midY);
}

export function paintLegend(canvas, hm) {
    const {ctx, size: w, h, dpr} = setupCanvas(canvas, {square: false});
    const midY = h / 2;
    const gap = 8 * dpr;
    const labelGap = 6 * dpr;

    ctx.font = `${9 * dpr | 0}px 'Space Mono', monospace`;
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'right';

    const {pokMax, plyMax} = hm;
    const pokLabelW = ctx.measureText(pokMax).width;
    const plyLabelW = ctx.measureText(plyMax).width;

    drawLegendBar(ctx, 0, w / 2 - gap - pokLabelW - labelGap, 'rgb(249,199,79)', pokMax, w / 2 - gap, midY);
    drawLegendBar(ctx, w / 2 + gap, w - (w / 2 + gap) - plyLabelW - labelGap, 'rgb(77,201,240)', plyMax, w, midY);
}