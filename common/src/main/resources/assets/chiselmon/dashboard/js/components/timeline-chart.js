/**
 * Encounters-over-time line chart.
 */

import {api, buildUrl} from '../core/api.js';
import {state} from '../core/state.js';

// Persists across calls so it can be updated in-place rather than re-creating every refresh
let chart;

export async function loadTimeline() {
    const gran = state.granularity;
    const response = await api(buildUrl('/api/timeline/', {granularity: gran}));
    const buckets = response.buckets;

    const bucketLabels = buckets.map(d => {
        const dateTime = new Date(d.bucket);
        const date = dateTime.toLocaleDateString(undefined, {month: 'short', day: 'numeric'});
        const hh = String(dateTime.getHours()).padStart(2, '0');
        const mm = String(dateTime.getMinutes()).padStart(2, '0');
        const time = gran === 'minute' ? `${hh}:${mm}` : `${hh}h`;
        return `${date} ${time}`;
    });

    const bucketCounts = buckets.map(d => d.count);

    // If the chart already exists, update its data and re-render
    if (chart) {
        chart.data.labels = bucketLabels;
        chart.data.datasets[0].data = bucketCounts;
        chart.update();
        return;
    }

    const ctx = document.getElementById('chart-timeline').getContext('2d');
    chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: bucketLabels,
            datasets: [{
                data: bucketCounts,
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
            plugins: {legend: {display: false}},
            scales: {
                x: {
                    ticks: {
                        maxTicksLimit: 12,
                        maxRotation: 0,
                        // Split date and time into two lines by breaking on the last space
                        callback(val) {
                            const raw = this.getLabelForValue(val);
                            if (!raw) return '';
                            const lastSpace = raw.lastIndexOf(' ');
                            return lastSpace === -1 ? raw : [raw.slice(0, lastSpace), raw.slice(lastSpace + 1)];
                        },
                    },
                    grid: {display: false},
                },
                y: {beginAtZero: true, ticks: {precision: 0}},
            },
        },
    });
}