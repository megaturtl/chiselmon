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
        loadHeatmap();
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

// ── Heatmap Configuration ─────────────────────────────────────────────────────

const CHUNKS_TO_BLOCKS = 16;
const OVERSCAN_FACTOR = 2.0;
let _hmPokGrid = null, _hmPlyGrid = null, _hmCells = 0, _hmRadius = 0, _hmTileSize = 1;

function getBestPowerOfTwoTileSize(radiusBlocks) {
    const totalBlocks = radiusBlocks * 2;
    const sizes = [16, 8, 4, 2, 1];
    for (let s of sizes) {
        // aims to fit around 32x32 tiles on the heatmap
        if (totalBlocks / s >= 32) return s;
    }
    return 1;
}

function buildGrid(points, minX, minZ, cells, tileSize) {
    const grid = new Float32Array(cells * cells);
    for (const [x, z] of points) {
        const col = Math.floor((x - minX) / tileSize);
        const row = Math.floor((z - minZ) / tileSize);
        if (col >= 0 && col < cells && row >= 0 && row < cells) {
            grid[row * cells + col]++;
        }
    }
    return grid;
}

// ── Rendering ────────────────────────────────────────────────────────────────

function paintHeatmap(canvas, pokemonGrid, playerGrid, cells, minX, minZ, tileSize, cx, cz, visibleRadius) {
    const dpr = window.devicePixelRatio || 1;
    const rect = canvas.getBoundingClientRect();

    canvas.width = Math.round(rect.width * dpr);
    canvas.height = Math.round(rect.height * dpr);

    const ctx = canvas.getContext('2d');
    const size = canvas.width;

    ctx.fillStyle = '#0d1117';
    ctx.fillRect(0, 0, size, size);

    const blocksOnCanvas = visibleRadius * 2 * OVERSCAN_FACTOR;
    const canvasLeftWorld = cx - (blocksOnCanvas / 2);
    const canvasTopWorld = cz - (blocksOnCanvas / 2);
    const pxPerBlock = size / blocksOnCanvas;

    // 1. Draw Absolute Chunk Grid
    ctx.strokeStyle = 'rgba(255,255,255,0.04)';
    ctx.lineWidth = 1 * dpr;
    const gridStartX = Math.ceil(canvasLeftWorld / 16) * 16;
    const gridStartZ = Math.ceil(canvasTopWorld / 16) * 16;

    for (let x = gridStartX; x <= canvasLeftWorld + blocksOnCanvas; x += 16) {
        const lx = (x - canvasLeftWorld) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(lx, 0);
        ctx.lineTo(lx, size);
        ctx.stroke();
    }
    for (let z = gridStartZ; z <= canvasTopWorld + blocksOnCanvas; z += 16) {
        const lz = (z - canvasTopWorld) * pxPerBlock;
        ctx.beginPath();
        ctx.moveTo(0, lz);
        ctx.lineTo(size, lz);
        ctx.stroke();
    }

    // 2. Draw Data Cells
    const pokMax = Math.max(1, ...pokemonGrid);
    const plyMax = Math.max(1, ...playerGrid);
    const cellPx = tileSize * pxPerBlock;

    for (let row = 0; row < cells; row++) {
        for (let col = 0; col < cells; col++) {
            const idx = row * cells + col;
            const pokVal = pokemonGrid[idx] / pokMax;
            const plyVal = playerGrid[idx] / plyMax;
            if (pokVal <= 0 && plyVal <= 0) continue;

            const worldX = minX + (col * tileSize);
            const worldZ = minZ + (row * tileSize);

            const x = (worldX - canvasLeftWorld) * pxPerBlock;
            const y = (worldZ - canvasTopWorld) * pxPerBlock;

            if (plyVal > 0) {
                ctx.fillStyle = `rgba(77,201,240,${Math.pow(plyVal, 0.4) * 0.7})`;
                ctx.fillRect(x, y, cellPx + 0.5, cellPx + 0.5);
            }
            if (pokVal > 0) {
                ctx.fillStyle = `rgba(249,199,79,${Math.pow(pokVal, 0.4) * 0.8})`;
                ctx.fillRect(x, y, cellPx + 0.5, cellPx + 0.5);
            }
        }
    }
}

function paintLegend(canvas, pokMax, plyMax) {
    const dpr = window.devicePixelRatio || 1;
    canvas.width = canvas.offsetWidth * dpr;
    canvas.height = canvas.offsetHeight * dpr;
    const ctx = canvas.getContext('2d');
    const w = canvas.width, h = canvas.height;
    const barH = Math.round(10 * dpr), y = Math.round((h - barH) / 2);
    const gap = 8 * dpr, labelGap = 6 * dpr;

    ctx.font = `${Math.round(9 * dpr)}px 'Space Mono', monospace`;
    ctx.textBaseline = 'middle';

    const pokLabel = `${pokMax}`, plyLabel = `${plyMax}`;
    const pokLabelW = ctx.measureText(pokLabel).width;
    const plyLabelW = ctx.measureText(plyLabel).width;

    // --- Pokemon Side (Left) ---
    // Bar ends before the label in the center
    const pokBarWidth = w / 2 - gap - pokLabelW - labelGap;
    const pokGrad = ctx.createLinearGradient(0, 0, pokBarWidth, 0);
    pokGrad.addColorStop(0, 'rgba(249,199,79,0)');
    pokGrad.addColorStop(1, 'rgba(249,199,79,0.85)');
    ctx.fillStyle = pokGrad;
    ctx.fillRect(0, y, pokBarWidth, barH);

    // Pokemon Label (Centered-right)
    ctx.fillStyle = '#8b949e';
    ctx.textAlign = 'right';
    ctx.fillText(pokLabel, w / 2 - gap, h / 2);

    // --- Player Side (Right) ---
    // Bar starts at center and ends before the label on the far right
    const plyStartX = w / 2 + gap;
    const plyBarWidth = (w - plyStartX) - plyLabelW - labelGap;

    const plyGrad = ctx.createLinearGradient(plyStartX, 0, plyStartX + plyBarWidth, 0);
    plyGrad.addColorStop(0, 'rgba(77,201,240,0)');
    plyGrad.addColorStop(1, 'rgba(77,201,240,0.75)');
    ctx.fillStyle = plyGrad;
    ctx.fillRect(plyStartX, y, plyBarWidth, barH);

    // Player Label (Far right)
    ctx.fillStyle = '#8b949e';
    ctx.textAlign = 'right'; // Set to right to align with the canvas edge
    ctx.fillText(plyLabel, w, h / 2);
}

// ── Interaction Logic ────────────────────────────────────────────────────────

function initHeatmapHover(canvas, minX, minZ, cells, tileSize, visibleRadius) {
    const tooltip = document.getElementById('hm-tooltip');

    // Replace the listener to avoid duplicates
    canvas.onmousemove = e => {
        if (!_hmPokGrid || canvas._dragging) {
            tooltip.style.display = 'none';
            return;
        }

        const rect = canvas.getBoundingClientRect();
        const sizeOnCanvas = visibleRadius * 2 * OVERSCAN_FACTOR;
        const pxPerBlock = rect.width / sizeOnCanvas;

        const cx = parseInt(document.getElementById('hm-cx').value) || 0;
        const cz = parseInt(document.getElementById('hm-cz').value) || 0;
        const canvasLeftWorld = cx - (sizeOnCanvas / 2);
        const canvasTopWorld = cz - (sizeOnCanvas / 2);

        const worldX = canvasLeftWorld + (e.clientX - rect.left) / pxPerBlock;
        const worldZ = canvasTopWorld + (e.clientY - rect.top) / pxPerBlock;

        const col = Math.floor((worldX - minX) / tileSize);
        const row = Math.floor((worldZ - minZ) / tileSize);

        if (col < 0 || col >= cells || row < 0 || row >= cells) {
            tooltip.style.display = 'none';
            return;
        }

        const idx = row * cells + col;
        const pok = _hmPokGrid[idx];
        const ply = _hmPlyGrid[idx];

        if (!pok && !ply) {
            tooltip.style.display = 'none';
            return;
        }

        const dispX = Math.floor(minX + (col + 0.5) * tileSize);
        const dispZ = Math.floor(minZ + (row + 0.5) * tileSize);

        tooltip.innerHTML = `
            <span style="color:#8b949e">${dispX}, ${dispZ}</span><br>
            ${pok > 0 ? `🟡 ${pok} spawns<br>` : ''}
            ${ply > 0 ? `🔵 ${ply} player pos<br>` : ''}
            <small>tile: ${tileSize}x${tileSize}</small>
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
        e.preventDefault(); // CRITICAL: Stops browser from trying to "drag" the canvas as an image
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
            const rect = canvas.getBoundingClientRect();
            const blocksPerPixel = (_hmRadius * 2) / (rect.width / OVERSCAN_FACTOR);

            const cxInput = document.getElementById('hm-cx');
            const czInput = document.getElementById('hm-cz');
            cxInput.value = parseInt(cxInput.value || 0) + Math.round(-dx * blocksPerPixel);
            czInput.value = parseInt(czInput.value || 0) + Math.round(-dy * blocksPerPixel);

            await loadHeatmap();

            // RESET FLAG AFTER LOAD
            canvas._dragging = false;
        }
    });
}

// ── Main Entry Point ─────────────────────────────────────────────────────────

async function loadHeatmap() {
    const cx = parseInt(document.getElementById('hm-cx').value) || 0;
    const cz = parseInt(document.getElementById('hm-cz').value) || 0;
    const chunkRadius = Math.min(32, Math.max(2, parseInt(document.getElementById('hm-radius').value) || 8));
    const visibleRadius = chunkRadius * CHUNKS_TO_BLOCKS;

    // Check Tile Size Selector
    const tileSizeSetting = document.getElementById('hm-tile-size').value;
    const tileSize = tileSizeSetting === 'auto'
        ? getBestPowerOfTwoTileSize(visibleRadius)
        : parseInt(tileSizeSetting);

    const status = document.getElementById('hm-status');

    const fetchRadius = Math.round(visibleRadius * OVERSCAN_FACTOR);

    try {
        const data = await api(withFrom(`/api/heatmap?cx=${cx}&cz=${cz}&radius=${fetchRadius}`));

        // visible encounters excluding overscan
        const visibleEncounters = data.pokemon.filter(([x, z]) => {
            return Math.abs(x - cx) <= visibleRadius && Math.abs(z - cz) <= visibleRadius;
        });

        const gridBlocks = visibleRadius * 2 * OVERSCAN_FACTOR;
        const cells = Math.ceil(gridBlocks / tileSize);

        // Standardize the grid alignment based on tileSize
        const minX = Math.floor((cx - gridBlocks / 2) / tileSize) * tileSize;
        const minZ = Math.floor((cz - gridBlocks / 2) / tileSize) * tileSize;

        _hmPokGrid = buildGrid(data.pokemon, minX, minZ, cells, tileSize);
        _hmPlyGrid = buildGrid(data.player, minX, minZ, cells, tileSize);
        _hmCells = cells;
        _hmRadius = visibleRadius;
        _hmTileSize = tileSize;

        const canvas = document.getElementById('hm-canvas');

        requestAnimationFrame(() => {
            // Force reset the visual offset because paintHeatmap is
            // now drawing the canvas based on the NEW cx/cz values.
            canvas.style.transition = 'none';
            canvas.style.transform = 'translate(0, 0)';

            paintHeatmap(canvas, _hmPokGrid, _hmPlyGrid, cells, minX, minZ, tileSize, cx, cz, visibleRadius);

            const legendCanvas = document.getElementById('hm-legend-canvas');
            if (legendCanvas) {
                const pMax = _hmPokGrid.length ? Math.max(..._hmPokGrid) : 0;
                const plMax = _hmPlyGrid.length ? Math.max(..._hmPlyGrid) : 0;
                paintLegend(legendCanvas, pMax, plMax);
            }

            initHeatmapHover(canvas, minX, minZ, cells, tileSize, visibleRadius);
            initHeatmapDrag(canvas);

            document.getElementById('hm-label-tl').textContent = `${cx - visibleRadius}, ${cz - visibleRadius}`;
            document.getElementById('hm-label-br').textContent = `${cx + visibleRadius}, ${cz + visibleRadius}`;
            status.textContent = `${visibleEncounters.length.toLocaleString()} encounters · tile: ${tileSize}x${tileSize}`;
        });

    } catch (err) {
        status.textContent = 'Error: ' + err.message;
        console.error(err);
    }
}

async function resetHeatmap() {
    const status = document.getElementById('hm-status');

    try {
        // Re-fetch world info to get the latest player position
        const info = await api('/api/info');

        // Reset inputs to the player's last known location
        document.getElementById('hm-cx').value = info.lastX ?? 0;
        document.getElementById('hm-cz').value = info.lastZ ?? 0;
        document.getElementById('hm-radius').value = 8;

        // Force reload the heatmap with these new values
        await loadHeatmap();
    } catch (err) {
        status.textContent = 'Reset failed: ' + err.message;
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
refresh();
setInterval(refresh, 30_000);