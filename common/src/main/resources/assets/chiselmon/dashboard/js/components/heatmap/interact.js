/**
 * Drag, zoom, and hover handlers for the heatmap canvas.
 * Each init* function is idempotent — safe to call on every repaint.
 *
 * All handlers accept a `getHm` getter so they always read the
 * latest heatmap state, even though they're only bound once.
 */

import { gridGeometry, OVERSCAN_FACTOR } from './grid.js';
import { getCxCz, setCxCz } from './controls.js';

// ── Internal helper ───────────────────────────────────────────────────────────

function worldCoordsAtMouse(e, canvas, hm) {
    const { cx, cz } = getCxCz();
    const geom = gridGeometry(cx, cz, hm.radius, hm.tileSize);
    const rect = canvas.getBoundingClientRect();
    const pxPerBlock = rect.width / geom.span;
    return {
        worldX: geom.canvasLeft + (e.clientX - rect.left) / pxPerBlock,
        worldZ: geom.canvasTop  + (e.clientY - rect.top)  / pxPerBlock,
        geom,
    };
}

// ── Hover tooltip ─────────────────────────────────────────────────────────────

export function initHeatmapHover(canvas, getHm) {
    if (canvas._hoverInit) return;
    canvas._hoverInit = true;

    const tooltip = document.getElementById('hm-tooltip');

    canvas.addEventListener('mousemove', e => {
        const hm = getHm();
        if (!hm.pokGrid || canvas._dragging) {
            tooltip.style.display = 'none';
            return;
        }

        const { worldX, worldZ, geom } = worldCoordsAtMouse(e, canvas, hm);
        const col = Math.floor((worldX - geom.minX) / hm.tileSize);
        const row = Math.floor((worldZ - geom.minZ) / hm.tileSize);

        if (col < 0 || col >= geom.cells || row < 0 || row >= geom.cells) {
            tooltip.style.display = 'none';
            return;
        }

        const idx = row * geom.cells + col;
        const pok = hm.pokGrid[idx];
        const ply = hm.plyGrid[idx];

        if (!pok && !ply) { tooltip.style.display = 'none'; return; }

        const dispX = Math.floor(geom.minX + (col + 0.5) * hm.tileSize);
        const dispZ = Math.floor(geom.minZ + (row + 0.5) * hm.tileSize);
        tooltip.innerHTML = `
            <span style="color:#8b949e">${dispX}, ${dispZ}</span><br>
            ${pok ? `🟡 ${pok} spawns<br>` : ''}
            ${ply ? `🔵 ${ply} player pos<br>` : ''}
            <small>tile size: ${hm.tileSize}x${hm.tileSize}</small>
        `;
        tooltip.style.display = 'block';
        const wrap = canvas.closest('.hm-wrap').getBoundingClientRect();
        tooltip.style.left = (e.clientX - wrap.left + 15) + 'px';
        tooltip.style.top  = (e.clientY - wrap.top  - 15) + 'px';
    });

    canvas.addEventListener('mouseleave', () => tooltip.style.display = 'none');
}

// ── Drag to pan ───────────────────────────────────────────────────────────────

export function initHeatmapDrag(canvas, getHm, reloadFn) {
    if (canvas._dragInit) return;
    canvas._dragInit = true;

    let dragStart = null;

    canvas.addEventListener('mousedown', e => {
        if (e.button !== 0) return;
        e.preventDefault();
        dragStart = { x: e.clientX, y: e.clientY };
        canvas.style.transition = 'none';
        canvas._dragging = false;
    });

    window.addEventListener('mousemove', e => {
        if (!dragStart) return;
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;
        if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
            canvas._dragging = true;
            canvas.style.transform = `translate(${dx}px, ${dy}px)`;
        }
    });

    window.addEventListener('mouseup', async e => {
        if (!dragStart) return;
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;
        const wasDragging = canvas._dragging;
        dragStart = null;

        if (wasDragging) {
            const hm = getHm();
            const { cx, cz } = getCxCz();
            const blocksPerPixel =
                (hm.radius * 2) / (canvas.getBoundingClientRect().width / OVERSCAN_FACTOR);
            setCxCz(
                cx + Math.round(-dx * blocksPerPixel),
                cz + Math.round(-dy * blocksPerPixel),
            );
            await reloadFn();
            canvas._dragging = false;
        }
    });
}

// ── Scroll to zoom ────────────────────────────────────────────────────────────

export function initHeatmapZoom(canvas, reloadFn) {
    if (canvas._zoomInit) return;
    canvas._zoomInit = true;

    canvas.addEventListener('wheel', e => {
        e.preventDefault();
        const input   = document.getElementById('hm-radius');
        const current = parseInt(input.value) || 8;
        const delta   = e.deltaY > 0 ? 1 : -1;
        input.value   = Math.min(32, Math.max(2, current + delta));
        reloadFn();
    }, { passive: false });
}