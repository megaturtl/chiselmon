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

        // Populate dimension dropdown
        const select = document.getElementById('hm-dimension');
        select.innerHTML = (info.dimensions ?? ['minecraft:overworld']).map(d =>
            `<option value="${d}">${d.replace('minecraft:', '')}</option>`
        ).join('');
        if (info.lastDimension) select.value = info.lastDimension;
        select.addEventListener('change', loadHeatmap);

        // Seed heatmap at last known player position
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

async function loadDimensions() {
    try {
        const dims = await api('/api/dimensions');
        const select = document.getElementById('hm-dimension');
        select.innerHTML = dims.map(d =>
            `<option value="${d}">${d.replace('minecraft:', '')}</option>`
        ).join('')
    } catch (_) {
    }
}

// ── Time range & granularity state ───────────────────────────────────────────

const TIME_RANGES = [
    {label: '30m', ms: 1_800_000},
    {label: '1h', ms: 3_600_000},
    {label: '3h', ms: 10_800_000},
    {label: '6h', ms: 21_600_000},
    {label: '24h', ms: 86_400_000},
    {label: '7d', ms: 604_800_000},
    {label: '30d', ms: 2_592_000_000},
    {label: 'All', ms: 0},
];

// Granularity: 'hour' or 'minute'
let currentGranularity = 'hour';

// Default: 24h
let currentFromMs = Date.now() - 86_400_000;

function getFrom() {
    return currentFromMs;
}

function getGranularity() {
    return currentGranularity;
}

function initTimeRange() {
    const bar = document.getElementById('time-range');

    // ── Time range pills ──
    TIME_RANGES.forEach(({label, ms}, i) => {
        const btn = document.createElement('button');
        // Default active: 24h (index 4)
        btn.className = 'tr-btn' + (i === 4 ? ' active' : '');
        btn.textContent = label;
        btn.dataset.ms = ms;
        bar.appendChild(btn);
    });

    bar.addEventListener('click', e => {
        const btn = e.target.closest('.tr-btn');
        if (!btn || btn.classList.contains('gran-btn')) return;
        bar.querySelectorAll('.tr-btn:not(.gran-btn)').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const ms = parseInt(btn.dataset.ms);
        currentFromMs = ms === 0 ? 0 : Date.now() - ms;

        // Auto-switch granularity: short windows → minute, long → hour
        const autoGran = ms > 0 && ms <= 10_800_000 ? 'minute' : 'hour';
        if (autoGran !== currentGranularity) {
            currentGranularity = autoGran;
            updateGranularityButtons();
        }

        refresh();
        loadHeatmap();
    });

    // ── Separator ──
    const sep = document.createElement('div');
    sep.className = 'tr-sep';
    bar.appendChild(sep);

    // ── Granularity pills ──
    ['hour', 'minute'].forEach(gran => {
        const btn = document.createElement('button');
        btn.className = 'tr-btn gran-btn' + (gran === currentGranularity ? ' active' : '');
        btn.textContent = gran === 'hour' ? '1H' : '1m';
        btn.title = gran === 'hour' ? 'Hourly buckets' : 'Minutely buckets';
        btn.dataset.gran = gran;
        bar.appendChild(btn);
    });

    bar.addEventListener('click', e => {
        const btn = e.target.closest('.gran-btn');
        if (!btn) return;
        currentGranularity = btn.dataset.gran;
        updateGranularityButtons();
        loadTimeline();
    });
}

function updateGranularityButtons() {
    document.querySelectorAll('.gran-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.gran === currentGranularity);
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

    // --- Compute encounters per minute using only active minutes ---
    // The backend now returns activeMinutes: count of distinct minutes that had data.
    // This avoids penalising offline/AFK time.
    let perMin = 0;

    if (s.total > 0) {
        if (s.activeMinutes && s.activeMinutes > 0) {
            // Preferred: use server-reported active minutes
            perMin = s.total / s.activeMinutes;
        } else if (getFrom() > 0) {
            // Fallback: wall-clock minutes (old behaviour)
            const minutes = (Date.now() - getFrom()) / 60000;
            if (minutes > 0) perMin = s.total / minutes;
        }
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
    const gran = getGranularity();
    const url = withFrom('/api/timeline') + (withFrom('/api/timeline').includes('?') ? '&' : '?') + 'granularity=' + gran;
    const data = await api(url);

    const isMinute = gran === 'minute';

    const labels = data.map(d => {
        const dt = new Date(d.bucket);
        if (isMinute) {
            return dt.toLocaleDateString(undefined, {month: 'short', day: 'numeric'})
                + ' ' + String(dt.getHours()).padStart(2, '0')
                + ':' + String(dt.getMinutes()).padStart(2, '0');
        }
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
                pointHoverRadius: 5,
                pointHitRadius: 10,
                tension: 0.3,
                fill: true,
            }]
        },
        options: {
            responsive: true,
            plugins: {legend: {display: false}},
            scales: {
                x: {
                    ticks: {
                        maxTicksLimit: 12,
                        maxRotation: 0,
                        callback(val) {
                            // val is the index into the labels array
                            const raw = this.getLabelForValue(val);
                            if (!raw) return '';
                            // labels are like "Feb 28 14:30" or "Feb 28 14h"
                            // split on the last space to get date v time parts
                            const lastSpace = raw.lastIndexOf(' ');
                            if (lastSpace === -1) return raw;
                            return [raw.slice(0, lastSpace), raw.slice(lastSpace + 1)];
                        }
                    },
                    grid: {display: false}
                },
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

const CHUNKS_TO_BLOCKS = 16;
const OVERSCAN_FACTOR = 2.0;

let _hm = {pokGrid: null, plyGrid: null, pokMax: 1, plyMax: 1, cells: 0, radius: 0, tileSize: 1};

// ── Grid Utilities ────────────────────────────────────────────────────────────

function bestTileSize(radiusBlocks) {
    const span = radiusBlocks * 2;
    if (span >= 512) return 16;
    if (span >= 256) return 8;
    if (span >= 128) return 4;
    if (span >= 64) return 2;
    return 1;
}

function gridGeometry(cx, cz, visibleRadius, tileSize) {
    const span = visibleRadius * 2 * OVERSCAN_FACTOR;
    const canvasLeft = cx - span / 2;
    const canvasTop = cz - span / 2;
    return {
        span, canvasLeft, canvasTop,
        cells: Math.ceil(span / tileSize),
        minX: Math.floor(canvasLeft / tileSize) * tileSize,
        minZ: Math.floor(canvasTop / tileSize) * tileSize,
    };
}

function gridMax(grid) {
    let max = 0;
    for (let i = 0; i < grid.length; i++) if (grid[i] > max) max = grid[i];
    return max || 1;
}

function buildGrid(points, {minX, minZ, cells}, tileSize) {
    const grid = new Float32Array(cells * cells);
    for (const [x, z] of points) {
        const col = Math.floor((x - minX) / tileSize);
        const row = Math.floor((z - minZ) / tileSize);
        if (col >= 0 && col < cells && row >= 0 && row < cells)
            grid[row * cells + col]++;
    }
    return grid;
}

// Returns the max count considering only points within the visible (non-overscan) radius
function visibleGridMax(grid, geom, cx, cz, visibleRadius, tileSize) {
    let max = 0;
    const {minX, minZ, cells} = geom;
    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const worldX = minX + (col + 0.5) * tileSize;
            const worldZ = minZ + (row + 0.5) * tileSize;
            if (Math.abs(worldX - cx) > visibleRadius || Math.abs(worldZ - cz) > visibleRadius) continue;
            const v = grid[row * cells + col];
            if (v > max) max = v;
        }
    }
    return max || 1;
}

// ── Canvas Helpers ────────────────────────────────────────────────────────────

function setupCanvas(canvas) {
    const dpr = window.devicePixelRatio || 1;
    const size = Math.round(canvas.getBoundingClientRect().width * dpr);
    canvas.width = canvas.height = size;
    return {ctx: canvas.getContext('2d'), size, dpr};
}

function blendCellColor(pokVal, plyVal) {
    let r = 0, g = 0, b = 0, a = 0;

    if (plyVal > 0) {
        const pa = Math.pow(plyVal, 0.4) * 0.7;
        [r, g, b, a] = [77 * pa, 201 * pa, 240 * pa, pa];
    }
    if (pokVal > 0) {
        const pa = Math.pow(pokVal, 0.4) * 0.8;
        const outA = pa + a * (1 - pa);
        if (outA > 0) {
            r = (249 * pa + r * a * (1 - pa)) / outA;
            g = (199 * pa + g * a * (1 - pa)) / outA;
            b = (79 * pa + b * a * (1 - pa)) / outA;
            a = outA;
        }
    }

    return [r + 0.5 | 0, g + 0.5 | 0, b + 0.5 | 0, Math.min(255, a * 255 + 0.5 | 0)];
}

function stampCell(px, size, x0, y0, cellPx, R, G, B, A) {
    const x1 = Math.min(size, x0 + Math.ceil(cellPx));
    const y1 = Math.min(size, y0 + Math.ceil(cellPx));
    for (let py = Math.max(0, y0); py < y1; py++) {
        const base = py * size * 4;
        for (let qx = Math.max(0, x0); qx < x1; qx++) {
            const i = base + qx * 4;
            px[i] = R;
            px[i + 1] = G;
            px[i + 2] = B;
            px[i + 3] = A;
        }
    }
}

// ── Paint Functions ───────────────────────────────────────────────────────────

function drawChunkGrid(ctx, size, canvasLeft, canvasTop, span, dpr) {
    ctx.strokeStyle = 'rgba(255,255,255,0.04)';
    ctx.lineWidth = dpr;
    const pxPerBlock = size / span;

    for (let x = Math.ceil(canvasLeft / 16) * 16; x <= canvasLeft + span; x += 16) {
        const lx = (x - canvasLeft) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(lx, 0);
        ctx.lineTo(lx, size);
        ctx.stroke();
    }
    for (let z = Math.ceil(canvasTop / 16) * 16; z <= canvasTop + span; z += 16) {
        const lz = (z - canvasTop) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(0, lz);
        ctx.lineTo(size, lz);
        ctx.stroke();
    }
}

function drawDataCells(ctx, size, geom, pxPerBlock) {
    const {pokGrid, plyGrid, pokMax, plyMax, tileSize} = _hm;
    const {cells, minX, minZ, canvasLeft, canvasTop} = geom;
    const cellPx = tileSize * pxPerBlock;
    const imgData = ctx.getImageData(0, 0, size, size);

    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const idx = row * cells + col;
            // clamp to 1 — overscan cells can legitimately exceed the visible max
            const pokVal = Math.min(1, pokGrid[idx] / pokMax);
            const plyVal = Math.min(1, plyGrid[idx] / plyMax);
            if (!pokVal && !plyVal) continue;

            const x0 = Math.floor((minX + col * tileSize - canvasLeft) * pxPerBlock);
            const y0 = Math.floor((minZ + row * tileSize - canvasTop) * pxPerBlock);
            const [R, G, B, A] = blendCellColor(pokVal, plyVal);
            stampCell(imgData.data, size, x0, y0, cellPx, R, G, B, A);
        }
    }

    ctx.putImageData(imgData, 0, 0);
}

function paintHeatmap(canvas, cx, cz) {
    const {ctx, size, dpr} = setupCanvas(canvas);
    const geom = gridGeometry(cx, cz, _hm.radius, _hm.tileSize);
    const pxPerBlock = size / geom.span;

    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);

    drawDataCells(ctx, size, geom, pxPerBlock);
    drawChunkGrid(ctx, size, geom.canvasLeft, geom.canvasTop, geom.span, dpr);
}

function drawLegendBar(ctx, x, width, color, label, labelX, midY) {
    const grad = ctx.createLinearGradient(x, 0, x + width, 0);
    grad.addColorStop(0, color.replace('rgb', 'rgba').replace(')', ',0)'));
    grad.addColorStop(1, color.replace('rgb', 'rgba').replace(')', ',0.85)'));
    ctx.fillStyle = grad;
    ctx.fillRect(x, midY - 5, width, 10);
    ctx.fillStyle = '#8b949e';
    ctx.fillText(label, labelX, midY);
}

function paintLegend(canvas) {
    const dpr = window.devicePixelRatio || 1;
    canvas.width = canvas.offsetWidth * dpr;
    canvas.height = canvas.offsetHeight * dpr;
    const ctx = canvas.getContext('2d');
    const w = canvas.width;
    const midY = canvas.height / 2;
    const gap = 8 * dpr;
    const labelGap = 6 * dpr;

    ctx.font = `${9 * dpr | 0}px 'Space Mono', monospace`;
    ctx.textBaseline = 'middle';
    ctx.textAlign = 'right';

    // pokMax/plyMax on _hm are already computed from visible tiles only
    const {pokMax, plyMax} = _hm;
    const pokLabelW = ctx.measureText(pokMax).width;
    const plyLabelW = ctx.measureText(plyMax).width;

    drawLegendBar(ctx, 0, w / 2 - gap - pokLabelW - labelGap, 'rgb(249,199,79)', pokMax, w / 2 - gap, midY);
    drawLegendBar(ctx, w / 2 + gap, w - (w / 2 + gap) - plyLabelW - labelGap, 'rgb(77,201,240)', plyMax, w, midY);
}

// ── Interactions ──────────────────────────────────────────────────────────────

function getCxCz() {
    return {
        cx: parseInt(document.getElementById('hm-cx').value) || 0,
        cz: parseInt(document.getElementById('hm-cz').value) || 0,
    };
}

function setCxCz(cx, cz) {
    document.getElementById('hm-cx').value = cx;
    document.getElementById('hm-cz').value = cz;
}

function worldCoordsAtMouse(e, canvas) {
    const {cx, cz} = getCxCz();
    const geom = gridGeometry(cx, cz, _hm.radius, _hm.tileSize);
    const rect = canvas.getBoundingClientRect();
    const pxPerBlock = rect.width / geom.span;
    return {
        worldX: geom.canvasLeft + (e.clientX - rect.left) / pxPerBlock,
        worldZ: geom.canvasTop + (e.clientY - rect.top) / pxPerBlock,
        geom,
    };
}

function initHeatmapHover(canvas) {
    const tooltip = document.getElementById('hm-tooltip');

    canvas.onmousemove = e => {
        if (!_hm.pokGrid || canvas._dragging) {
            tooltip.style.display = 'none';
            return;
        }

        const {worldX, worldZ, geom} = worldCoordsAtMouse(e, canvas);
        const col = Math.floor((worldX - geom.minX) / _hm.tileSize);
        const row = Math.floor((worldZ - geom.minZ) / _hm.tileSize);

        if (col < 0 || col >= geom.cells || row < 0 || row >= geom.cells) {
            tooltip.style.display = 'none';
            return;
        }

        const idx = row * geom.cells + col;
        const pok = _hm.pokGrid[idx];
        const ply = _hm.plyGrid[idx];

        if (!pok && !ply) {
            tooltip.style.display = 'none';
            return;
        }

        const dispX = Math.floor(geom.minX + (col + 0.5) * _hm.tileSize);
        const dispZ = Math.floor(geom.minZ + (row + 0.5) * _hm.tileSize);
        tooltip.innerHTML = `
            <span style="color:#8b949e">${dispX}, ${dispZ}</span><br>
            ${pok ? `🟡 ${pok} spawns<br>` : ''}
            ${ply ? `🔵 ${ply} player pos<br>` : ''}
            <small>tile size: ${_hm.tileSize}x${_hm.tileSize}</small>
        `;
        tooltip.style.display = 'block';
        const wrap = canvas.closest('.hm-wrap').getBoundingClientRect();
        tooltip.style.left = (e.clientX - wrap.left + 15) + 'px';
        tooltip.style.top = (e.clientY - wrap.top - 15) + 'px';
    };

    canvas.onmouseleave = () => tooltip.style.display = 'none';
}

function initHeatmapDrag(canvas) {
    if (canvas._dragInit) return;
    canvas._dragInit = true;

    let dragStart = null;

    canvas.addEventListener('mousedown', e => {
        if (e.button !== 0) return;
        e.preventDefault();
        dragStart = {x: e.clientX, y: e.clientY};
        canvas.style.transition = 'none';
        canvas._dragging = false;
    });

    window.addEventListener('mousemove', e => {
        if (!dragStart) return;
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;
        if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
            canvas._dragging = true;
            canvas.style.transform = `translate(${dx}px, ${dy}px)`;
        }
    });

    window.addEventListener('mouseup', async e => {
        if (!dragStart) return;
        const dx = e.clientX - dragStart.x;
        const dy = e.clientY - dragStart.y;
        const wasDragging = canvas._dragging;
        dragStart = null;

        if (wasDragging) {
            const {cx, cz} = getCxCz();
            const blocksPerPixel = (_hm.radius * 2) / (canvas.getBoundingClientRect().width / OVERSCAN_FACTOR);
            setCxCz(cx + Math.round(-dx * blocksPerPixel), cz + Math.round(-dy * blocksPerPixel));
            await loadHeatmap();
            canvas._dragging = false;
        }
    });
}

function initHeatmapZoom(canvas) {
    if (canvas._zoomInit) return;
    canvas._zoomInit = true;

    canvas.addEventListener('wheel', e => {
        e.preventDefault();
        const input = document.getElementById('hm-radius');
        const current = parseInt(input.value) || 8;
        const delta = e.deltaY > 0 ? 1 : -1; // scroll down = zoom out = bigger radius
        input.value = Math.min(32, Math.max(2, current + delta));
        loadHeatmap();
    }, {passive: false});
}

// ── Load & Reset ──────────────────────────────────────────────────────────────

function readHeatmapInputs() {
    const {cx, cz} = getCxCz();
    const chunkRadius = Math.min(32, Math.max(2, parseInt(document.getElementById('hm-radius').value) || 8));
    const visibleRadius = chunkRadius * CHUNKS_TO_BLOCKS;
    const tileSetting = document.getElementById('hm-tile-size').value;
    const tileSize = tileSetting === 'auto' ? bestTileSize(visibleRadius) : parseInt(tileSetting);
    const dimension = document.getElementById('hm-dimension').value || 'minecraft:overworld';
    return {cx, cz, visibleRadius, tileSize, dimension};
}

function countVisibleEncounters(points, cx, cz, visibleRadius) {
    return points.filter(([x, z]) =>
        Math.abs(x - cx) <= visibleRadius && Math.abs(z - cz) <= visibleRadius
    ).length;
}

function updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount) {
    document.getElementById('hm-label-tl').textContent = `${cx - visibleRadius}, ${cz - visibleRadius}`;
    document.getElementById('hm-label-br').textContent = `${cx + visibleRadius}, ${cz + visibleRadius}`;
    document.getElementById('hm-status').textContent = `Showing ${encounterCount.toLocaleString()} encounters`;
}

async function loadHeatmap() {
    const {cx, cz, visibleRadius, tileSize} = readHeatmapInputs();
    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);
    const status = document.getElementById('hm-status');

    try {
        const data = await api(withFrom(`/api/heatmap?cx=${cx}&cz=${cz}&radius=${fetchRadius}`));
        const geom = gridGeometry(cx, cz, visibleRadius, tileSize);

        const pokGrid = buildGrid(data.pokemon, geom, tileSize);
        const plyGrid = buildGrid(data.player, geom, tileSize);

        _hm = {
            pokGrid, plyGrid,
            // compute max from visible tiles only — overscan must not wash out colours
            pokMax: visibleGridMax(pokGrid, geom, cx, cz, visibleRadius, tileSize),
            plyMax: visibleGridMax(plyGrid, geom, cx, cz, visibleRadius, tileSize),
            cells: geom.cells,
            radius: visibleRadius,
            tileSize,
        };

        const canvas = document.getElementById('hm-canvas');
        const encounterCount = countVisibleEncounters(data.pokemon, cx, cz, visibleRadius);

        requestAnimationFrame(() => {
            canvas.style.transition = 'none';
            canvas.style.transform = 'translate(0, 0)';

            paintHeatmap(canvas, cx, cz);
            paintLegend(document.getElementById('hm-legend-canvas'));
            initHeatmapHover(canvas);
            initHeatmapDrag(canvas);
            initHeatmapZoom(canvas);
            updateHeatmapLabels(cx, cz, visibleRadius, tileSize, encounterCount);
        });

    } catch (err) {
        status.textContent = 'Error: ' + err.message;
        console.error(err);
    }
}

async function resetHeatmap() {
    try {
        const info = await api('/api/info');
        setCxCz(info.lastX ?? 0, info.lastZ ?? 0);
        document.getElementById('hm-radius').value = 8;
        await loadHeatmap();
    } catch (err) {
        document.getElementById('hm-status').textContent = 'Reset failed: ' + err.message;
    }
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
loadDimensions();
refresh();
setInterval(refresh, 30_000);