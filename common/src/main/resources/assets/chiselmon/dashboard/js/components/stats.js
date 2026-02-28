/**
 * Loads and renders the top-level stat cards.
 */

import { api, withFrom } from '../core/api.js';
import { state } from '../core/state.js';

export async function loadStats() {
    const s = await api(withFrom('/api/stats'));

    let perMin = 0;
    if (s.total > 0) {
        if (s.activeMinutes && s.activeMinutes > 0) {
            perMin = s.total / s.activeMinutes;
        } else if (state.fromMs > 0) {
            const minutes = (Date.now() - state.fromMs) / 60_000;
            if (minutes > 0) perMin = s.total / minutes;
        }
    }

    const perMinText  = s.total > 0 ? `(${perMin.toFixed(2)}/min)` : '';
    const shinyOdds   = s.shinies > 0        ? Math.floor(s.total / s.shinies)        : 0;
    const legendOdds  = s.legendaries > 0     ? Math.floor(s.total / s.legendaries)    : 0;
    const sizeVarOdds = s.size_variations > 0 ? Math.floor(s.total / s.size_variations): 0;

    document.getElementById('stat-grid').innerHTML = `
        <div class="stat-card">
            <span class="label">Total Encounters</span>
            <span class="value">${s.total.toLocaleString()}</span>
            <span class="secondary_value">${perMinText}</span>
        </div>
        <div class="stat-card">
            <span class="label">Snack Spawns</span>
            <span class="value">${s.snackSpawns.toLocaleString()}</span>
            <span class="secondary_value">${s.total > 0 ? (s.snackSpawns / s.total * 100).toFixed(2) : '0.00'}%</span>
        </div>
        <div class="stat-card shiny">
            <span class="label">Shinies</span>
            <span class="value">${s.shinies.toLocaleString()}</span>
            <span class="secondary_value">${shinyOdds}</span>
        </div>
        <div class="stat-card legendary">
            <span class="label">Legendaries</span>
            <span class="value">${s.legendaries.toLocaleString()}</span>
            <span class="secondary_value">${legendOdds}</span>
        </div>
        <div class="stat-card size_variation">
            <span class="label">Size Variations</span>
            <span class="value">${s.size_variations.toLocaleString()}</span>
            <span class="secondary_value">${sizeVarOdds}</span>
        </div>
        <div class="stat-card">
            <span class="label">Unique Species</span>
            <span class="value">${s.uniqueSpecies.toLocaleString()}</span>
        </div>
    `;
}