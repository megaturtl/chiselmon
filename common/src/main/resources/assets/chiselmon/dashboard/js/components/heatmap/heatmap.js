import {api} from '../../core/api.js';
import {state} from '../../core/state.js';
import {buildGrid, countVisibleEncounters, gridGeometry, visibleGridMax} from './grid.js';
import {getHeatmapData, invalidateHeatmapCache} from './cache.js';
import {paintHeatmap, paintLegend} from './render.js';
import {initHeatmapDrag, initHeatmapHover, initHeatmapZoom} from './interact.js';
import {readHeatmapInputs, updateHeatmapLabels} from './controls.js';

let _hm = {pokGrid: new Map(), plyGrid: new Map(), pokMax: 1, plyMax: 1, cells: 0, radius: 0, tileSize: 1};

export async function initHeatmap() {
    let dimensionsResponse;
    try {
        dimensionsResponse = await api('/api/dimensions/');
    } catch (e) {
        console.error('Failed to load dimensions', e);
        dimensionsResponse = {dimensions: [{dimension: 'minecraft:overworld', count: 0}]};
    }

    const dimSelect = document.getElementById('hm-dimension');
    dimSelect.innerHTML = dimensionsResponse.dimensions.map(d => {
        const label = d.dimension.replace('minecraft:', '');
        return `<option value="${d.dimension}">${label}</option>`;
    }).join('');

    dimSelect.addEventListener('change', () => {
        invalidateHeatmapCache();
        loadHeatmap();
    });
    document.getElementById('hm-cx').addEventListener('change', loadHeatmap);
    document.getElementById('hm-cz').addEventListener('change', loadHeatmap);
    document.getElementById('hm-radius').addEventListener('change', loadHeatmap);
    document.getElementById('hm-tile-size').addEventListener('change', loadHeatmap);
    document.getElementById('hm-reset-btn').addEventListener('click', resetHeatmap);

    state.onChange(() => {
        invalidateHeatmapCache();
        loadHeatmap();
    });

    const canvas = document.getElementById('hm-canvas');
    initHeatmapHover(canvas, () => _hm);
    initHeatmapDrag(canvas, () => _hm, loadHeatmap);
    initHeatmapZoom(canvas, loadHeatmap);

    const playerPos = await api('/api/playerpos/');
    if (playerPos?.lastX !== undefined) {
        document.getElementById('hm-cx').value = playerPos.lastX;
        document.getElementById('hm-cz').value = playerPos.lastZ;
        document.getElementById('hm-radius').value = 8;
        document.getElementById('hm-dimension').value = playerPos.lastDimension ?? 'minecraft:overworld';
    }

    await loadHeatmap();
}

export async function loadHeatmap() {
    const {cx, cz, visibleRadius, tileSize, dimension} = readHeatmapInputs();

    try {
        const data = await getHeatmapData(cx, cz, visibleRadius, dimension);
        const geom = gridGeometry(cx, cz, visibleRadius, tileSize);
        const pokGrid = buildGrid(data.pokemon, geom, tileSize);
        const plyGrid = buildGrid(data.player, geom, tileSize);

        _hm = {
            pokGrid, plyGrid,
            pokMax: visibleGridMax(pokGrid, geom, cx, cz, visibleRadius, tileSize),
            plyMax: visibleGridMax(plyGrid, geom, cx, cz, visibleRadius, tileSize),
            cells: geom.cells,
            radius: visibleRadius,
            tileSize,
        };

        const canvas = document.getElementById('hm-canvas');
        const encounterCount = countVisibleEncounters(data.pokemon, cx, cz, visibleRadius);

        requestAnimationFrame(() => {
            canvas.style.transition = 'none';
            canvas.style.transform = 'translate(0, 0)';
            paintHeatmap(canvas, cx, cz, _hm);
            paintLegend(document.getElementById('hm-legend-canvas'), _hm);
            updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount);
        });

    } catch (err) {
        document.getElementById('hm-status').textContent = 'Error: ' + err.message;
        console.error(err);
    }
}

async function resetHeatmap() {
    const playerPos = await api('/api/playerpos/');
    document.getElementById('hm-cx').value = playerPos?.lastX ?? 0;
    document.getElementById('hm-cz').value = playerPos?.lastZ ?? 0;
    document.getElementById('hm-radius').value = 8;
    document.getElementById('hm-dimension').value = playerPos?.lastDimension ?? 'minecraft:overworld';
    await loadHeatmap();
}