/**
 * Dashboard entry point.
 *
 * Imports all components, wires them to the reactive state,
 * runs the initial load, and sets up the 30 s auto-refresh.
 */

// Side-effect: configures Chart.js globals
import './config/chart-defaults.js';

// Core
import { state } from './core/state.js';

// Components
import { initTimeRange }                from './components/time-range.js';
import { loadStats }                    from './components/stats.js';
import { loadTimeline }                 from './components/timeline-chart.js';
import { loadSpecies }                  from './components/species-chart.js';
import { loadBiomes }                   from './components/biomes-chart.js';
import { loadEncounters }               from './components/encounters-table.js';
import { initHeatmap, loadHeatmap }     from './components/heatmap/heatmap.js';

// ── Refresh orchestration ─────────────────────────────────────────────────────

async function refresh() {
    try {
        await Promise.all([
            loadStats(),
            loadTimeline(),
            loadSpecies(),
            loadBiomes(),
            loadEncounters(),
            loadHeatmap(),
        ]);
        document.getElementById('last-update').textContent =
            'Updated ' + new Date().toLocaleTimeString();
    } catch (err) {
        console.error('Dashboard refresh failed:', err);
        document.getElementById('last-update').textContent = 'Error: ' + err.message;
    }
}

// ── Boot ──────────────────────────────────────────────────────────────────────

initTimeRange();
await initHeatmap();

// Subscribe all data-loaders to state changes
state.onChange(refresh);

// First paint
await refresh();

// Auto-refresh every 30 s
setInterval(refresh, 30_000);