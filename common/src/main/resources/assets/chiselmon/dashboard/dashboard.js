// ── Chart.js defaults ────────────────────────────────────────────────────────

Chart.defaults.color = '#8b949e';
Chart.defaults.borderColor = '#21262d';
Chart.defaults.font.family = "'Space Mono', monospace";
Chart.defaults.font.size = 11;

const CHART_PALETTE = [
    '#E13538', '#F9844A', '#F9C74F', '#41D73B',
    '#40E0D0', '#2D73B0', '#6C44C3', '#F46997',
];

// ── Helpers ───────────────────────────────────────────────────────────────────

async function api(path) {
    const r = await fetch(path);
    if (!r.ok) throw new Error(r.statusText);
    return r.json();
}

function fmtTime(ms) {
    return new Date(ms).toLocaleString(undefined, {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
    });
}

function fmtBiome(b) {
    return b ? b.replace(/^minecraft:/, '').replace(/_/g, ' ') : '–';
}

// ── Stats cards ───────────────────────────────────────────────────────────────

async function loadStats() {
    const s = await api('/api/stats');
    document.getElementById('stat-grid').innerHTML = `
        <div class="stat-card">
            <span class="label">Total Encounters</span>
            <span class="value">${s.total.toLocaleString()}</span>
        </div>
        <div class="stat-card shiny">
            <span class="label">Shinies</span>
            <span class="value">${s.shinies.toLocaleString()}</span>
        </div>
        <div class="stat-card legendary">
            <span class="label">Legendaries</span>
            <span class="value">${s.legendaries.toLocaleString()}</span>
        </div>
        <div class="stat-card">
            <span class="label">Unique Species</span>
            <span class="value">${s.uniqueSpecies.toLocaleString()}</span>
        </div>
        <div class="stat-card">
            <span class="label">Shiny Rate</span>
            <span class="value">${s.total > 0 ? (s.shinies / s.total * 100).toFixed(2) : '0.00'}%</span>
        </div>
        <div class="stat-card">
            <span class="label">Snack Rate</span>
            <span class="value">${s.total > 0 ? (s.snackSpawns / s.total * 100).toFixed(2) : '0.00'}%</span>
        </div>
    `;
}

// ── Timeline chart ────────────────────────────────────────────────────────────

let timelineChart;

async function loadTimeline() {
    const data = await api('/api/timeline');

    const labels = data.map(d => {
        const dt = new Date(d.bucket);
        return dt.toLocaleDateString(undefined, {month: 'short', day: 'numeric'})
            + ' ' + String(dt.getHours()).padStart(2, '0') + 'h';
    });

    const ctx = document.getElementById('chart-timeline').getContext('2d');
    if (timelineChart) timelineChart.destroy();
    timelineChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [{
                data: data.map(d => d.count),
                borderColor: '#F46997',
                backgroundColor: 'rgba(237,109,152,0.2)',
                borderWidth: 1.5,
                pointRadius: 0,
                tension: 0.3,
                fill: true,
            }]
        },
        options: {
            responsive: true,
            plugins: {legend: {display: false}},
            scales: {
                x: {ticks: {maxTicksLimit: 12, maxRotation: 0}, grid: {display: false}},
                y: {beginAtZero: true, ticks: {precision: 0}},
            }
        }
    });
}

// ── Species chart ─────────────────────────────────────────────────────────────

let speciesChart;

async function loadSpecies() {
    const data = await api('/api/species');

    const ctx = document.getElementById('chart-species').getContext('2d');
    if (speciesChart) speciesChart.destroy();
    speciesChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: data.map(d => d.species),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: data.map((_, i) => CHART_PALETTE[i % CHART_PALETTE.length] + 'cc'),
                borderWidth: 0,
                borderRadius: 3,
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            plugins: {legend: {display: false}},
            scales: {
                x: {beginAtZero: true, ticks: {precision: 0}},
                y: {ticks: {font: {size: 10}}},
            }
        }
    });
}

// ── Biomes chart ──────────────────────────────────────────────────────────────

let biomesChart;

async function loadBiomes() {
    const data = await api('/api/biomes');

    const ctx = document.getElementById('chart-biomes').getContext('2d');
    if (biomesChart) biomesChart.destroy();
    biomesChart = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.map(d => fmtBiome(d.biome)),
            datasets: [{
                data: data.map(d => d.count),
                backgroundColor: CHART_PALETTE.map(c => c + 'cc'),
                borderColor: '#161b22',
                borderWidth: 2,
                hoverOffset: 6,
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {boxWidth: 10, padding: 10, font: {size: 10}}
                }
            }
        }
    });
}

// ── Recent encounters table ───────────────────────────────────────────────────

async function loadEncounters() {
    const data = await api('/api/encounters');
    const tbody = document.getElementById('enc-tbody');

    if (!data.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No encounters recorded yet.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(e => {
        const star = e.shiny
            ? ' <span style="color:var(--shiny)" title="Shiny">★</span>'
            : e.legendary
                ? ' <span style="color:var(--legendary)" title="Legendary">★</span>'
                : '';
        const cake = e.snack
            ? '<span title="From snack" style="margin-right:6px">🎂</span>'
            : '';

        return `<tr>
            <td>${cake}<strong>${e.species}</strong>${star}</td>
            <td style="color:var(--muted)">${e.form || '–'}</td>
            <td>${e.level}</td>
            <td style="color:var(--muted)">${e.gender}</td>
            <td style="color:var(--muted)">${fmtBiome(e.biome)}</td>
            <td style="color:var(--muted)">${e.dimension.replace('minecraft:', '')}</td>
            <td style="color:var(--muted)">${fmtTime(e.ms)}</td>
        </tr>`;
    }).join('');
}

// ── Heatmap ───────────────────────────────────────────────────────────────────

const HEATMAP_RESOLUTION = 80;   // grid cells per axis
const HEATMAP_BLUR = 1.8;  // gaussian blur radius on the canvas (px scaled)

function buildGrid(points, cx, cz, radius, cells) {
    const grid = new Float32Array(cells * cells);
    for (const [x, z] of points) {
        const col = Math.floor(((x - (cx - radius)) / (radius * 2)) * cells);
        const row = Math.floor(((z - (cz - radius)) / (radius * 2)) * cells);
        if (col >= 0 && col < cells && row >= 0 && row < cells) {
            grid[row * cells + col]++;
        }
    }
    return grid;
}

function paintHeatmap(canvas, pokemonGrid, playerGrid, cells) {
    // Match canvas buffer to its CSS display size so it's sharp on HiDPI screens
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.width * dpr);   // keep it square

    const size = canvas.width;
    const cell = size / cells;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, size, size);

    // Dark background
    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);

    // Grid lines (subtle)
    ctx.strokeStyle = 'rgba(255,255,255,0.04)';
    ctx.lineWidth = 0.5;
    const step = size / 8;
    for (let i = step; i < size; i += step) {
        ctx.beginPath();
        ctx.moveTo(i, 0);
        ctx.lineTo(i, size);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(0, i);
        ctx.lineTo(size, i);
        ctx.stroke();
    }

    const pokMax = Math.max(1, ...pokemonGrid);
    const plyMax = Math.max(1, ...playerGrid);

    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const idx = row * cells + col;
            const pokVal = pokemonGrid[idx] / pokMax;
            const plyVal = playerGrid[idx] / plyMax;

            if (pokVal <= 0 && plyVal <= 0) continue;

            const x = col * cell;
            const y = row * cell;

            // Player layer: teal/cyan
            if (plyVal > 0) {
                ctx.fillStyle = `rgba(77,201,240,${Math.pow(plyVal, 0.5) * 0.6})`;
                ctx.fillRect(x, y, cell + 0.5, cell + 0.5);
            }

            // Pokemon layer: amber/gold — drawn on top, additive feel
            if (pokVal > 0) {
                ctx.fillStyle = `rgba(249,199,79,${Math.pow(pokVal, 0.5) * 0.75})`;
                ctx.fillRect(x, y, cell + 0.5, cell + 0.5);
            }
        }
    }

    // Crosshair at centre
    const mid = size / 2;
    ctx.strokeStyle = 'rgba(255,255,255,0.25)';
    ctx.lineWidth = 1;
    ctx.setLineDash([4, 4]);
    ctx.beginPath();
    ctx.moveTo(mid, 0);
    ctx.lineTo(mid, size);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(0, mid);
    ctx.lineTo(size, mid);
    ctx.stroke();
    ctx.setLineDash([]);
}

async function loadHeatmap() {
    const cx = parseInt(document.getElementById('hm-cx').value) || 0;
    const cz = parseInt(document.getElementById('hm-cz').value) || 0;
    const radius = parseInt(document.getElementById('hm-radius').value) || 256;

    const status = document.getElementById('hm-status');
    status.textContent = 'Loading…';

    let data;
    try {
        data = await api(`/api/heatmap?cx=${cx}&cz=${cz}&radius=${radius}`);
    } catch (err) {
        status.textContent = 'Error: ' + err.message;
        return;
    }

    const cells = HEATMAP_RESOLUTION;
    const pokGrid = buildGrid(data.pokemon, cx, cz, radius, cells);
    const plyGrid = buildGrid(data.player, cx, cz, radius, cells);

    const canvas = document.getElementById('hm-canvas');
    paintHeatmap(canvas, pokGrid, plyGrid, cells);

    // Axis labels
    document.getElementById('hm-label-tl').textContent = `${cx - radius}, ${cz - radius}`;
    document.getElementById('hm-label-br').textContent = `${cx + radius}, ${cz + radius}`;

    const total = data.pokemon.length;
    status.textContent = `${total.toLocaleString()} encounter${total !== 1 ? 's' : ''} in range`;
}

// ── Boot & auto-refresh ───────────────────────────────────────────────────────

async function refresh() {
    try {
        await Promise.all([
            loadStats(),
            loadTimeline(),
            loadSpecies(),
            loadBiomes(),
            loadEncounters(),
        ]);
        document.getElementById('last-update').textContent =
            'Updated ' + new Date().toLocaleTimeString();
    } catch (err) {
        console.error('Dashboard refresh failed:', err);
        document.getElementById('last-update').textContent = 'Error: ' + err.message;
    }
}

refresh();
setInterval(refresh, 30_000);