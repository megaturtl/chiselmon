import './config/chart-defaults.js';

import {state} from './core/state.js';
import {loadContext} from './core/context.js';
import {initTimeRange} from './components/time-range.js';
import {loadStats} from './components/stats.js';
import {loadTimeline} from './components/timeline-chart.js';
import {loadSpecies} from './components/species-chart.js';
import {loadBiomes} from './components/biomes-chart.js';
import {loadRecentEncounters} from './components/encounters-table.js';
import {initHeatmap, loadHeatmap} from './components/heatmap/heatmap.js';

const lastUpdate = document.getElementById('last-update');

async function refresh() {
    const results = await Promise.allSettled([
        loadStats(),
        loadTimeline(),
        loadSpecies(),
        loadBiomes(),
        loadRecentEncounters(),
        loadHeatmap(),
    ]);

    const failures = results.filter(r => r.status === 'rejected');
    if (failures.length) {
        console.error('Some components failed to refresh:', failures.map(r => r.reason));
    }

    lastUpdate.textContent = failures.length
        ? `Partial update at ${new Date().toLocaleTimeString()} (${failures.length} errors)`
        : `Updated ${new Date().toLocaleTimeString()}`;
}

// Entrypoint to boot the dashboard
async function start() {
    await loadContext();
    initTimeRange();
    await initHeatmap(); // seeds coordinates, binds interactions, does an initial load

    state.onChange(refresh);
    await refresh();
    setInterval(refresh, 30_000);
}

start().catch(err => {
    console.error('Error booting dashboard:', err);
    document.getElementById('last-update').textContent = 'Failed to initialise dashboard';
});