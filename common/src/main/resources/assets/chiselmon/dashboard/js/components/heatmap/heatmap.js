import {api, buildUrl} from '../../core/api.js';
import {buildGrid, countVisibleEncounters, gridGeometry, OVERSCAN_FACTOR, visibleGridMax} from './grid.js';
import {paintHeatmap, paintLegend} from './render.js';
import {initHeatmapDrag, initHeatmapHover, initHeatmapZoom} from './interact.js';
import {readHeatmapInputs, updateHeatmapLabels} from './controls.js';

let _hm = {pokGrid: null, plyGrid: null, pokMax: 1, plyMax: 1, cells: 0, radius: 0, tileSize: 1};
const getHm = () => _hm;

/**
 * Normalises a HeatmapPoint from the API ({ x, z } object) to the [x, z] tuple
 * format expected by grid.js. Keeps grid.js decoupled from the API shape.
 */
const toTuple = ({x, z}) => [x, z];

export async function initHeatmap() {
    let dimensionsResponse;
    try {
        dimensionsResponse = await api('/api/dimensions/');
    } catch (e) {
        console.error('Failed to load dimensions', e);
        dimensionsResponse = {dimensions: [{dimension: 'minecraft:overworld', count: 0}]};
    }

    const select = document.getElementById('hm-dimension');
    select.innerHTML = dimensionsResponse.dimensions.map(d => {
        const shortName = d.dimension.replace('minecraft:', '');
        return `<option value="${d.dimension}">${shortName}</option>`;
    }).join('');

    // Wire controls - changes will trigger a data reload, not a full re-init
    select.addEventListener('change', loadHeatmap);
    document.getElementById('hm-cx').addEventListener('change', loadHeatmap);
    document.getElementById('hm-cz').addEventListener('change', loadHeatmap);
    document.getElementById('hm-radius').addEventListener('change', loadHeatmap);
    document.getElementById('hm-tile-size').addEventListener('change', loadHeatmap);
    document.getElementById('hm-reset-btn').addEventListener('click', resetHeatmap);

    // Interactions are bound once here - getHm() ensures they always see the fresh state
    const canvas = document.getElementById('hm-canvas');
    initHeatmapHover(canvas, getHm);
    initHeatmapDrag(canvas, getHm, loadHeatmap);
    initHeatmapZoom(canvas, loadHeatmap);

    // Seed position from player, then do the first load with correct coordinates
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
    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);
    const status = document.getElementById('hm-status');

    try {
        const data = await api(buildUrl('/api/heatmap/', {cx, cz, radius: fetchRadius, dimension}));

        // API returns { x, z } point objects — normalise to [x, z] tuples for grid.js
        const pokemonTuples = data.pokemon.map(toTuple);
        const playerTuples = data.player.map(toTuple);

        const geom = gridGeometry(cx, cz, visibleRadius, tileSize);
        const pokGrid = buildGrid(pokemonTuples, geom, tileSize);
        const plyGrid = buildGrid(playerTuples, geom, tileSize);

        _hm = {
            pokGrid, plyGrid,
            pokMax: visibleGridMax(pokGrid, geom, cx, cz, visibleRadius, tileSize),
            plyMax: visibleGridMax(plyGrid, geom, cx, cz, visibleRadius, tileSize),
            cells: geom.cells,
            radius: visibleRadius,
            tileSize,
        };

        const canvas = document.getElementById('hm-canvas');
        const encounterCount = countVisibleEncounters(pokemonTuples, cx, cz, visibleRadius);

        requestAnimationFrame(() => {
            canvas.style.transition = 'none';
            canvas.style.transform = 'translate(0, 0)';
            paintHeatmap(canvas, cx, cz, _hm);
            paintLegend(document.getElementById('hm-legend-canvas'), _hm);
            updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount);
        });

    } catch (err) {
        status.textContent = 'Error: ' + err.message;
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