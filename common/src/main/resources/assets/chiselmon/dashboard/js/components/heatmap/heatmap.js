/**
 * Heatmap orchestrator — ties together grid math, rendering, interactions,
 * and the world/info bootstrap.
 */

import { api, buildUrl } from '../../core/api.js';
import { OVERSCAN_FACTOR, gridGeometry, buildGrid, visibleGridMax, countVisibleEncounters } from './grid.js';
import { paintHeatmap, paintLegend } from './render.js';
import { initHeatmapHover, initHeatmapDrag, initHeatmapZoom } from './interact.js';
import { readHeatmapInputs, updateHeatmapLabels } from './controls.js';

/** Shared mutable heatmap state — updated on every load. */
let _hm = { pokGrid: null, plyGrid: null, pokMax: 1, plyMax: 1, cells: 0, radius: 0, tileSize: 1 };

/** Getter so interaction handlers always read the latest state. */
const getHm = () => _hm;

// ── Bootstrap ─────────────────────────────────────────────────────────────────

export async function initHeatmap() {
    try {
        const info = await api('/api/info');
        const prefix = info.type === 'mp' ? '🌐 ' : '🌏 ';
        document.getElementById('world-name').textContent = prefix + info.name;

        // Populate dimension dropdown from /api/info (the only reliable source)
        const select = document.getElementById('hm-dimension');
        select.innerHTML = (info.dimensions ?? ['minecraft:overworld']).map(d =>
            `<option value="${d}">${d.replace('minecraft:', '')}</option>`
        ).join('');
        if (info.lastDimension) select.value = info.lastDimension;

        // Wire up controls (no inline handlers in the HTML)
        select.addEventListener('change', loadHeatmap);
        document.getElementById('hm-cx').addEventListener('change', loadHeatmap);
        document.getElementById('hm-cz').addEventListener('change', loadHeatmap);
        document.getElementById('hm-radius').addEventListener('change', loadHeatmap);
        document.getElementById('hm-tile-size').addEventListener('change', loadHeatmap);
        document.getElementById('hm-reset-btn').addEventListener('click', resetHeatmap);

        // Seed at last known player position
        if (info.lastX !== undefined) {
            document.getElementById('hm-cx').value = info.lastX;
            document.getElementById('hm-cz').value = info.lastZ;
            document.getElementById('hm-radius').value = 8;
        }
    } catch (_) {
        // non-fatal — header just stays empty
    }
}

// ── Load ──────────────────────────────────────────────────────────────────────

export async function loadHeatmap() {
    const { cx, cz, visibleRadius, tileSize, dimension } = readHeatmapInputs();
    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);
    const status = document.getElementById('hm-status');

    try {
        // FIX: dimension is now included in the request
        const data = await api(buildUrl('/api/heatmap', {
            cx, cz,
            radius: fetchRadius,
            dimension,
        }));
        const geom = gridGeometry(cx, cz, visibleRadius, tileSize);

        const pokGrid = buildGrid(data.pokemon, geom, tileSize);
        const plyGrid = buildGrid(data.player,  geom, tileSize);

        _hm = {
            pokGrid, plyGrid,
            pokMax: visibleGridMax(pokGrid, geom, cx, cz, visibleRadius, tileSize),
            plyMax: visibleGridMax(plyGrid, geom, cx, cz, visibleRadius, tileSize),
            cells: geom.cells,
            radius: visibleRadius,
            tileSize,
        };

        const canvas         = document.getElementById('hm-canvas');
        const encounterCount = countVisibleEncounters(data.pokemon, cx, cz, visibleRadius);

        requestAnimationFrame(() => {
            canvas.style.transition = 'none';
            canvas.style.transform  = 'translate(0, 0)';

            paintHeatmap(canvas, cx, cz, _hm);
            paintLegend(document.getElementById('hm-legend-canvas'), _hm);
            initHeatmapHover(canvas, getHm);
            initHeatmapDrag(canvas, getHm, loadHeatmap);
            initHeatmapZoom(canvas, loadHeatmap);
            updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount);
        });

    } catch (err) {
        status.textContent = 'Error: ' + err.message;
        console.error(err);
    }
}

// ── Reset ─────────────────────────────────────────────────────────────────────

async function resetHeatmap() {
    try {
        const info = await api('/api/info');
        document.getElementById('hm-cx').value = info.lastX ?? 0;
        document.getElementById('hm-cz').value = info.lastZ ?? 0;
        document.getElementById('hm-radius').value = 8;
        await loadHeatmap();
    } catch (err) {
        document.getElementById('hm-status').textContent = 'Reset failed: ' + err.message;
    }
}