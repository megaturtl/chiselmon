// ── Chart.js defaults ────────────────────────────────────────────────────────

Chart.defaults.color = '#8b949e';
Chart.defaults.borderColor = '#21262d';
Chart.defaults.font.family = "'Space Mono', monospace";
Chart.defaults.font.size = 11;

const CHART_PALETTE = [
    '#E13538', '#F9844A', '#F9C74F', '#41D73B',
    '#40E0D0', '#2D73B0', '#6C44C3', '#F46997',
];

// ── World/server info ─────────────────────────────────────────────────────────

async function loadInfo() {
    try {
        const info = await api('/api/info');
        const prefix = info.type === 'mp' ? '🌐 ' : '🌏 ';
        document.getElementById('world-name').textContent = prefix + info.name;

        // Seed heatmap with last player position + 8 chunk default radius
        if (info.lastX !== undefined) {
            document.getElementById('hm-cx').value = info.lastX;
            document.getElementById('hm-cz').value = info.lastZ;
            document.getElementById('hm-radius').value = 8;
            loadHeatmap();
        }
    } catch (_) {
        // non-fatal, header just stays empty if we can't get it
    }
}

// ── Time range state ──────────────────────────────────────────────────────────

const TIME_RANGES = [
    {label: '1h', ms: 3_600_000},
    {label: '24h', ms: 86_400_000},
    {label: '7d', ms: 604_800_000},
    {label: '30d', ms: 2_592_000_000},
    {label: 'All', ms: 0},
];

let currentFromMs = Date.now() - 86_400_000;   // default: 24h
let heatmapLoaded = false;

function getFrom() {
    return currentFromMs;
}

function initTimeRange() {
    const bar = document.getElementById('time-range');
    TIME_RANGES.forEach(({label, ms}, i) => {
        const btn = document.createElement('button');
        btn.className = 'tr-btn' + (i === 1 ? ' active' : '');
        btn.textContent = label;
        btn.dataset.ms = ms;
        bar.appendChild(btn);
    });

    bar.addEventListener('click', e => {
        const btn = e.target.closest('.tr-btn');
        if (!btn) return;
        bar.querySelectorAll('.tr-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const ms = parseInt(btn.dataset.ms);
        currentFromMs = ms === 0 ? 0 : Date.now() - ms;
        refresh();
        if (heatmapLoaded) loadHeatmap();
    });
}

// ── Helpers ───────────────────────────────────────────────────────────────────

async function api(path) {
    const r = await fetch(path);
    if (!r.ok) throw new Error(r.statusText);
    return r.json();
}

// Appends ?from= or &from= depending on whether the path already has params
function withFrom(path) {
    const from = getFrom();
    if (from <= 0) return path;
    return path + (path.includes('?') ? '&' : '?') + 'from=' + from;
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
    const s = await api(withFrom('/api/stats'));

    // --- Compute encounters per minute ---
    let perMin = 0;

    if (s.total > 0 && getFrom() > 0) {
        const minutes = (Date.now() - getFrom()) / 60000;
        if (minutes > 0) perMin = s.total / minutes;
    }

    const perMinText = `(${perMin.toFixed(2)}/min)`;

    const shinyOdds = s.shinies > 0
        ? Math.floor(s.total / s.shinies)
        : 0;

    const legendOdds = s.legendaries > 0
        ? Math.floor(s.total / s.legendaries)
        : 0;

    const sizeVariationOdds = s.size_variations > 0
        ? Math.floor(s.total / s.size_variations)
        : 0;

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
            <span class="secondary_value">${sizeVariationOdds}</span>
        </div>
        <div class="stat-card">
            <span class="label">Unique Species</span>
            <span class="value">${s.uniqueSpecies.toLocaleString()}</span>
        </div>
    `;
}

// ── Timeline chart ────────────────────────────────────────────────────────────

let timelineChart;

async function loadTimeline() {
    const data = await api(withFrom('/api/timeline'));

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
    const data = await api(withFrom('/api/species'));

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
    const data = await api(withFrom('/api/biomes'));

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
    const data = await api(withFrom('/api/encounters'));
    const tbody = document.getElementById('enc-tbody');

    if (!data.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No encounters recorded yet.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(e => {

        let special_indicators = '';
        if (e.shiny) {
            special_indicators += ' <span style="color:var(--shiny)" title="Shiny">★</span>';
        }
        if (e.legendary) {
            special_indicators += ' <span style="color:var(--legendary)" title="Legendary">★</span>';
        }

        // --- Gender icon ---
        let genderIcon = '';
        if (e.gender === 'MALE') {
            genderIcon = '<span title="Male" style="color:#2D73B0;margin-right:6px">♂</span>';
        } else if (e.gender === 'FEMALE') {
            genderIcon = '<span title="Female" style="color:#F46997;margin-right:6px">♀</span>';
        } else {
            genderIcon = '<span title="Genderless" style="color:var(--muted);margin-right:6px">•</span>';
        }

        // --- Size variation display ---
        let scaleDisplay = '';
        const scale = parseFloat(e.scale);

        if (scale !== 1.0) {
            scaleDisplay = ` <span style="color:var(--size_variation)">(${scale.toFixed(2)})</span>`;
        }

        // --- Snack icon ---
        const cake = e.snack
            ? '<span title="From snack" style="margin-right:6px">🎂</span>'
            : '';

        return `<tr>
            <td>${cake}${genderIcon}<strong>${e.species}</strong>${scaleDisplay}${special_indicators}</td>
            <td style="color:var(--muted)">${e.form || '–'}</td>
            <td>${e.level}</td>
            <td style="color:var(--muted)">${e.block_name.replace('minecraft:', '')}</td>
            <td style="color:var(--muted)">${fmtBiome(e.biome)}</td>
            <td style="color:var(--muted)">${e.dimension.replace('minecraft:', '')}</td>
            <td style="color:var(--muted)">${fmtTime(e.ms)}</td>
        </tr>`;
    }).join('');
}

// ── Heatmap ───────────────────────────────────────────────────────────────────

const HEATMAP_RESOLUTION = 80;
const CHUNKS_TO_BLOCKS = 16;

// Retained after last paint so the hover handler can read them
let _hmPokGrid = null, _hmPlyGrid = null, _hmCells = 0, _hmRadius = 0;

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
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();
    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.width * dpr);

    const size = canvas.width;
    const cell = size / cells;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, size, size);
    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);

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

            if (plyVal > 0) {
                ctx.fillStyle = `rgba(77,201,240,${Math.pow(plyVal, 0.5) * 0.6})`;
                ctx.fillRect(x, y, cell + 0.5, cell + 0.5);
            }
            if (pokVal > 0) {
                ctx.fillStyle = `rgba(249,199,79,${Math.pow(pokVal, 0.5) * 0.75})`;
                ctx.fillRect(x, y, cell + 0.5, cell + 0.5);
            }
        }
    }

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

function paintLegend(canvas, pokMax, plyMax) {
    const dpr = window.devicePixelRatio || 1;

    canvas.width = canvas.offsetWidth * dpr;
    canvas.height = canvas.offsetHeight * dpr;

    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const h = canvas.height;

    const barH = Math.round(10 * dpr);
    const y = Math.round((h - barH) / 2);
    const gap = 8 * dpr;
    const labelGap = 6 * dpr; // small breathing room between text and gradient

    // Font setup FIRST so measurement is correct
    ctx.font = `${Math.round(9 * dpr)}px 'Space Mono', monospace`;

    const leftZero = '0';
    const rightZero = '0';
    const pokLabel = `${pokMax}`;
    const plyLabel = `${plyMax}`;

    // Measure text widths
    const leftZeroW = ctx.measureText(leftZero).width;
    const pokLabelW = ctx.measureText(pokLabel).width;
    const rightZeroW = ctx.measureText(rightZero).width;
    const plyLabelW = ctx.measureText(plyLabel).width;

    // Compute gradient bounds
    const leftStart = leftZeroW + labelGap;
    const leftEnd = (w / 2) - gap - pokLabelW - labelGap;

    const rightStart = (w / 2) + gap + rightZeroW + labelGap;
    const rightEnd = w - plyLabelW - labelGap;

    // --- Gradients ---

    // Pokemon gradient
    const pokGrad = ctx.createLinearGradient(leftStart, 0, leftEnd, 0);
    pokGrad.addColorStop(0, 'rgba(249,199,79,0)');
    pokGrad.addColorStop(1, 'rgba(249,199,79,0.85)');
    ctx.fillStyle = pokGrad;
    ctx.fillRect(leftStart, y, Math.max(0, leftEnd - leftStart), barH);

    // Player gradient
    const plyGrad = ctx.createLinearGradient(rightStart, 0, rightEnd, 0);
    plyGrad.addColorStop(0, 'rgba(77,201,240,0)');
    plyGrad.addColorStop(1, 'rgba(77,201,240,0.75)');
    ctx.fillStyle = plyGrad;
    ctx.fillRect(rightStart, y, Math.max(0, rightEnd - rightStart), barH);

    // --- Labels ---
    ctx.fillStyle = '#8b949e';
    ctx.textBaseline = 'middle';

    // Left half
    ctx.textAlign = 'left';
    ctx.fillText(leftZero, 0, h / 2);

    ctx.textAlign = 'right';
    ctx.fillText(pokLabel, w / 2 - gap, h / 2);

    // Right half
    ctx.textAlign = 'left';
    ctx.fillText(rightZero, w / 2 + gap, h / 2);

    ctx.textAlign = 'right';
    ctx.fillText(plyLabel, w, h / 2);
}

function initHeatmapHover(canvas, radiusBlocks, cells) {
    const tooltip = document.getElementById('hm-tooltip');
    const tileSizeBlocks = Math.max(1, Math.round((radiusBlocks * 2) / cells));

    canvas.addEventListener('mousemove', e => {
        if (!_hmPokGrid) return;
        const rect = canvas.getBoundingClientRect();
        const scaleX = canvas.width / rect.width;
        const scaleY = canvas.height / rect.height;
        const px = (e.clientX - rect.left) * scaleX;
        const py = (e.clientY - rect.top) * scaleY;
        const col = Math.floor(px / (canvas.width / cells));
        const row = Math.floor(py / (canvas.height / cells));

        if (col < 0 || col >= cells || row < 0 || row >= cells) {
            tooltip.style.display = 'none';
            return;
        }

        const idx = row * cells + col;
        const pok = _hmPokGrid[idx];
        const ply = _hmPlyGrid[idx];
        if (pok === 0 && ply === 0) {
            tooltip.style.display = 'none';
            return;
        }

        const lines = [];
        if (pok > 0) lines.push(`🟡 ${pok} spawn${pok !== 1 ? 's' : ''}`);
        if (ply > 0) lines.push(`🔵 ${ply} player pos${ply !== 1 ? 'itions' : 'ition'}`);
        lines.push(`tile size = ${tileSizeBlocks}×${tileSizeBlocks} blocks`);

        tooltip.innerHTML = lines.join('<br>');
        tooltip.style.display = 'block';
        // Position relative to hm-wrap
        const wrap = canvas.closest('.hm-wrap');
        const wrapRect = wrap.getBoundingClientRect();
        let tx = e.clientX - wrapRect.left + 12;
        let ty = e.clientY - wrapRect.top - 10;
        // Keep inside wrap
        if (tx + 160 > wrapRect.width) tx = e.clientX - wrapRect.left - 165;
        tooltip.style.left = tx + 'px';
        tooltip.style.top = ty + 'px';
    });

    canvas.addEventListener('mouseleave', () => {
        tooltip.style.display = 'none';
    });
}

async function loadHeatmap() {
    const cx = parseInt(document.getElementById('hm-cx').value) || 0;
    const cz = parseInt(document.getElementById('hm-cz').value) || 0;
    const chunkRadius = Math.min(16, Math.max(1,
        parseInt(document.getElementById('hm-radius').value) || 8));
    const radiusBlocks = chunkRadius * CHUNKS_TO_BLOCKS;

    const status = document.getElementById('hm-status');
    status.textContent = 'Loading…';

    let data;
    try {
        data = await api(withFrom(`/api/heatmap?cx=${cx}&cz=${cz}&radius=${radiusBlocks}`));
    } catch (err) {
        status.textContent = 'Error: ' + err.message;
        return;
    }

    const cells = HEATMAP_RESOLUTION;
    const pokGrid = buildGrid(data.pokemon, cx, cz, radiusBlocks, cells);
    const plyGrid = buildGrid(data.player, cx, cz, radiusBlocks, cells);

    // Store for hover handler
    _hmPokGrid = pokGrid;
    _hmPlyGrid = plyGrid;
    _hmCells = cells;
    _hmRadius = radiusBlocks;

    const canvas = document.getElementById('hm-canvas');
    paintHeatmap(canvas, pokGrid, plyGrid, cells);
    initHeatmapHover(canvas, radiusBlocks, cells);

    // Gradient legend
    const legendCanvas = document.getElementById('hm-legend-canvas');
    const pokMax = Math.max(0, ...pokGrid);
    const plyMax = Math.max(0, ...plyGrid);
    paintLegend(legendCanvas, pokMax, plyMax);

    document.getElementById('hm-label-tl').textContent =
        `${cx - radiusBlocks}, ${cz - radiusBlocks}`;
    document.getElementById('hm-label-br').textContent =
        `${cx + radiusBlocks}, ${cz + radiusBlocks}`;

    const total = data.pokemon.length;
    status.textContent = `${total.toLocaleString()} encounter${total !== 1 ? 's' : ''} in range`
        + ` · tile size = ${Math.max(1, Math.round(radiusBlocks * 2 / cells))}×${Math.max(1, Math.round(radiusBlocks * 2 / cells))} blocks`;
    heatmapLoaded = true;
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

initTimeRange();
loadInfo();
refresh();
setInterval(refresh, 30_000);