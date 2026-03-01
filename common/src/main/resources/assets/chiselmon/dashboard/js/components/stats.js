/**
 * Top-level stat cards.
 */

import {api, withFrom} from '../core/api.js';
import {state} from '../core/state.js';

export async function loadStats() {
    const stats = await api(withFrom('/api/stats'));

    // Only count active minutes to avoid afk/offline skewing the rate
    let encountersPerMin = 0;
    if (stats.totalEncounters > 0) {
        if (stats.activeMinutes > 0) {
            encountersPerMin = stats.totalEncounters / stats.activeMinutes;
        } else if (state.fromMs > 0) {
            const minutes = (Date.now() - state.fromMs) / 60_000;
            if (minutes > 0) encountersPerMin = stats.totalEncounters / minutes;
        }
    }

    // Secondary value calcs
    const encountersPerMinStr = stats.totalEncounters > 0 ? `(${encountersPerMin.toFixed(2)}/min)` : '';
    const snackPct = stats.totalEncounters > 0 ? `${(stats.snackSpawns / stats.totalEncounters * 100).toFixed(2)}%` : '0.00%';
    const shinyRatioStr = stats.shinies > 0 ? `1/${Math.floor(stats.totalEncounters / stats.shinies)}` : 'N/A';
    const legendRatioStr = stats.legendaries > 0 ? `1/${Math.floor(stats.totalEncounters / stats.legendaries)}` : 'N/A';
    const sizeVarRatioStr = stats.sizeVariations > 0 ? `1/${Math.floor(stats.totalEncounters / stats.sizeVariations)}` : 'N/A';

    document.getElementById('stat-grid').innerHTML = `
        <div class="stat-card">
            <span class="label">Total Encounters</span>
            <span class="value">${stats.totalEncounters.toLocaleString()}</span>
            <span class="secondary_value">${encountersPerMinStr}</span>
        </div>
        <div class="stat-card">
            <span class="label">Snack Spawns</span>
            <span class="value">${stats.snackSpawns.toLocaleString()}</span>
            <span class="secondary_value">${snackPct}</span>
        </div>
        <div class="stat-card shiny">
            <span class="label">Shinies</span>
            <span class="value">${stats.shinies.toLocaleString()}</span>
            <span class="secondary_value">${shinyRatioStr}</span>
        </div>
        <div class="stat-card legendary">
            <span class="label">Legendaries</span>
            <span class="value">${stats.legendaries.toLocaleString()}</span>
            <span class="secondary_value">${legendRatioStr}</span>
        </div>
        <div class="stat-card size_variation">
            <span class="label">Size Variations</span>
            <span class="value">${stats.sizeVariations.toLocaleString()}</span>
            <span class="secondary_value">${sizeVarRatioStr}</span>
        </div>
    `;
}