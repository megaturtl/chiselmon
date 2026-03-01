import {CHUNKS_TO_BLOCKS, gridGeometry} from './grid.js';

// ── Config ────────────────────────────────────────────────────────────────────

const COLORS = {
    player: {r: 77, g: 201, b: 240},
    pokemon: {r: 249, g: 199, b: 79},
};

const BUCKETS = 8;
const MIN_ALPHA = 0.15;
const MAX_ALPHA = 0.8;

// ── Helpers ───────────────────────────────────────────────────────────────────

function setupCanvas(canvas, square = true) {
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    const w = Math.round(rect.width * dpr);
    const h = square ? w : Math.round(rect.height * dpr);
    canvas.width = w;
    canvas.height = h;
    return {ctx: canvas.getContext('2d'), w, h, dpr};
}

/**
 * Logarithmic normalization formula:
 * $norm = \frac{\ln(1 + \text{count})}{\ln(1 + \text{max})}$
 */
function getNorm(count, max) {
    if (count <= 0 || max <= 0) return 0;
    return Math.log1p(count) / Math.log1p(max);
}

// ── Drawing primitives ────────────────────────────────────────────────────────

function paintLayer(ctx, canvasSize, geom, pxPerBlock, grid, max, {r, g, b}) {
    if (grid.size === 0 || max <= 0) return;

    const {cells, minX, minZ, canvasLeft, canvasTop} = geom;
    const cellPx = (geom.span / cells) * pxPerBlock;
    const buckets = Array.from({length: BUCKETS}, () => []);

    for (const [idx, count] of grid) {
        const col = idx % cells;
        const row = Math.floor(idx / cells);
        const x0 = (minX + col * (geom.span / cells) - canvasLeft) * pxPerBlock;
        const y0 = (minZ + row * (geom.span / cells) - canvasTop) * pxPerBlock;

        if (x0 + cellPx < 0 || x0 >= canvasSize || y0 + cellPx < 0 || y0 >= canvasSize) continue;

        // Clamp the norm between 0 and 1 so bucketIdx never exceeds 15
        const norm = Math.min(1, getNorm(count, max));
        const bucketIdx = Math.floor(norm * (BUCKETS - 1));

        buckets[bucketIdx].push(x0, y0);
    }

    for (let i = 0; i < BUCKETS; i++) {
        if (buckets[i].length === 0) continue;

        const alpha = MIN_ALPHA + (i / (BUCKETS - 1)) * (MAX_ALPHA - MIN_ALPHA);
        ctx.fillStyle = `rgba(${r},${g},${b},${alpha.toFixed(2)})`;
        ctx.beginPath();
        for (let j = 0; j < buckets[i].length; j += 2) {
            ctx.rect(buckets[i][j], buckets[i][j + 1], cellPx, cellPx);
        }
        ctx.fill();
    }
}

function drawLegendBar(ctx, x, w, color, label, labelX, midY) {
    const {r, g, b} = color;
    const grad = ctx.createLinearGradient(x, 0, x + w, 0);

    // Legend follows the same 0.2 to 0.8 spread
    grad.addColorStop(0, `rgba(${r},${g},${b},${MIN_ALPHA})`);
    grad.addColorStop(1, `rgba(${r},${g},${b},${MAX_ALPHA})`);

    ctx.fillStyle = grad;
    ctx.fillRect(x, midY - 4, w, 8);

    ctx.fillStyle = '#8b949e';
    ctx.fillText(label, labelX, midY);
}

// ── Chunk grid ────────────────────────────────────────────────────────────────

function drawChunkGrid(ctx, canvasSize, geom, pxPerBlock) {
    const {canvasLeft, canvasTop, span} = geom;

    // Snap to the nearest chunk boundary left/above the canvas origin
    const startX = Math.floor(canvasLeft / CHUNKS_TO_BLOCKS) * CHUNKS_TO_BLOCKS;
    const startZ = Math.floor(canvasTop / CHUNKS_TO_BLOCKS) * CHUNKS_TO_BLOCKS;

    ctx.strokeStyle = 'rgba(255,255,255,0.08)';
    ctx.lineWidth = 1;
    ctx.beginPath();

    for (let wx = startX; wx <= canvasLeft + span; wx += CHUNKS_TO_BLOCKS) {
        const px = (wx - canvasLeft) * pxPerBlock;
        ctx.moveTo(px, 0);
        ctx.lineTo(px, canvasSize);
    }
    for (let wz = startZ; wz <= canvasTop + span; wz += CHUNKS_TO_BLOCKS) {
        const pz = (wz - canvasTop) * pxPerBlock;
        ctx.moveTo(0, pz);
        ctx.lineTo(canvasSize, pz);
    }

    ctx.stroke();
}

// ── Exports ───────────────────────────────────────────────────────────────────

export function paintHeatmap(canvas, cx, cz, hm) {
    const {ctx, w} = setupCanvas(canvas);
    const geom = gridGeometry(cx, cz, hm.radius, hm.tileSize);
    const pxPerBlock = w / geom.span;

    ctx.fillStyle = '#0d1117'; // Dark background
    ctx.fillRect(0, 0, w, w);

    drawChunkGrid(ctx, w, geom, pxPerBlock);

    paintLayer(ctx, w, geom, pxPerBlock, hm.plyGrid, hm.plyMax, COLORS.player);
    paintLayer(ctx, w, geom, pxPerBlock, hm.pokGrid, hm.pokMax, COLORS.pokemon);
}

export function paintLegend(canvas, hm) {
    const {ctx, w, h, dpr} = setupCanvas(canvas, false);
    const midY = h / 2;
    const halfW = w / 2;
    const gap = 16 * dpr;

    ctx.font = `${10 * dpr | 0}px monospace`;
    ctx.textBaseline = 'middle';

    // Pokemon Legend (Left)
    const pokLabel = `Max: ${hm.pokMax}`;
    const pokBarW = halfW - ctx.measureText(pokLabel).width - gap;
    drawLegendBar(ctx, 0, pokBarW, COLORS.pokemon, pokLabel, pokBarW + 8 * dpr, midY);

    // Player Legend (Right)
    const plyLabel = `Max: ${hm.plyMax}`;
    const plyBarW = halfW - ctx.measureText(plyLabel).width - gap;
    const plyStartX = halfW + (gap / 2);
    drawLegendBar(ctx, plyStartX, plyBarW, COLORS.player, plyLabel, plyStartX + plyBarW + 8 * dpr, midY);
}