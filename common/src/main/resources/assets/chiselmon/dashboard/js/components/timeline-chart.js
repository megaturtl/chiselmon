/**
 * Encounters-over-time line chart.
 */

import { api, buildUrl } from '../core/api.js';
import { state } from '../core/state.js';

let chart;

export async function loadTimeline() {
    const gran = state.granularity;
    const data = await api(buildUrl('/api/timeline', { granularity: gran }));

    const isMinute = gran === 'minute';

    const labels = data.map(d => {
        const dt = new Date(d.bucket);
        const date = dt.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
        if (isMinute) {
            return date + ' '
                + String(dt.getHours()).padStart(2, '0') + ':'
                + String(dt.getMinutes()).padStart(2, '0');
        }
        return date + ' ' + String(dt.getHours()).padStart(2, '0') + 'h';
    });

    const counts = data.map(d => d.count);

    if (chart) {
        chart.data.labels = labels;
        chart.data.datasets[0].data = counts;
        chart.update();
        return;
    }

    const ctx = document.getElementById('chart-timeline').getContext('2d');
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                data: counts,
                borderColor: '#F46997',
                backgroundColor: 'rgba(237,109,152,0.2)',
                borderWidth: 1.5,
                pointRadius: 0,
                pointHoverRadius: 5,
                pointHitRadius: 10,
                tension: 0.3,
                fill: true,
            }],
        },
        options: {
            responsive: true,
            plugins: { legend: { display: false } },
            scales: {
                x: {
                    ticks: {
                        maxTicksLimit: 12,
                        maxRotation: 0,
                        callback(val) {
                            const raw = this.getLabelForValue(val);
                            if (!raw) return '';
                            const lastSpace = raw.lastIndexOf(' ');
                            if (lastSpace === -1) return raw;
                            return [raw.slice(0, lastSpace), raw.slice(lastSpace + 1)];
                        },
                    },
                    grid: { display: false },
                },
                y: { beginAtZero: true, ticks: { precision: 0 } },
            },
        },
    });
}