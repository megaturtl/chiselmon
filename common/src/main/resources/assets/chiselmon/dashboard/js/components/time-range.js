/**
 * Renders the time-range and granularity pill buttons
 * inside #time-range, and wires them to state changes.
 */

import { state } from '../core/state.js';

const TIME_RANGES = [
    { label: '30m', ms: 1_800_000 },
    { label: '1h',  ms: 3_600_000 },
    { label: '3h',  ms: 10_800_000 },
    { label: '6h',  ms: 21_600_000 },
    { label: '24h', ms: 86_400_000 },
    { label: '7d',  ms: 604_800_000 },
    { label: '30d', ms: 2_592_000_000 },
    { label: 'All', ms: 0 },
];

export function initTimeRange() {
    const bar = document.getElementById('time-range');

    // ── Time-range pills ──
    TIME_RANGES.forEach(({ label, ms }, i) => {
        const btn = document.createElement('button');
        btn.className = 'tr-btn' + (i === 4 ? ' active' : '');
        btn.textContent = label;
        btn.dataset.ms = ms;
        bar.appendChild(btn);
    });

    // ── Separator ──
    const sep = document.createElement('div');
    sep.className = 'tr-sep';
    bar.appendChild(sep);

    // ── Granularity pills ──
    ['hour', 'minute'].forEach(gran => {
        const btn = document.createElement('button');
        btn.className = 'tr-btn gran-btn' + (gran === state.granularity ? ' active' : '');
        btn.textContent = gran === 'hour' ? '1H' : '1m';
        btn.title = gran === 'hour' ? 'Hourly buckets' : 'Minutely buckets';
        btn.dataset.gran = gran;
        bar.appendChild(btn);
    });

    // Single delegated listener for the whole bar
    bar.addEventListener('click', e => {
        const granBtn = e.target.closest('.gran-btn');
        if (granBtn) {
            state.setGranularity(granBtn.dataset.gran);
            syncGranButtons();
            return;
        }

        const trBtn = e.target.closest('.tr-btn');
        if (!trBtn) return;

        bar.querySelectorAll('.tr-btn:not(.gran-btn)').forEach(b => b.classList.remove('active'));
        trBtn.classList.add('active');

        const ms = parseInt(trBtn.dataset.ms);
        const fromMs = ms === 0 ? 0 : Date.now() - ms;
        const autoGran = ms > 0 && ms <= 21_600_000 ? 'minute' : 'hour';

        // Batched update — one notification, one refresh
        state.update(fromMs, autoGran);
        syncGranButtons();
    });
}

function syncGranButtons() {
    document.querySelectorAll('.gran-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.gran === state.granularity);
    });
}