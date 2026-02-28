/**
 * Recent encounters table renderer.
 */

import { api, withFrom } from '../core/api.js';
import { fmtTime, fmtBiome, stripNamespace } from '../core/format.js';

export async function loadEncounters() {
    const data  = await api(withFrom('/api/encounters'));
    const tbody = document.getElementById('enc-tbody');

    if (!data.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No encounters recorded yet.</td></tr>';
        return;
    }

    tbody.innerHTML = data.map(e => {
        let specials = '';
        if (e.shiny)     specials += ' <span style="color:var(--shiny)" title="Shiny">★</span>';
        if (e.legendary) specials += ' <span style="color:var(--legendary)" title="Legendary">★</span>';

        let gender = '<span title="Genderless" style="color:var(--muted);margin-right:6px">•</span>';
        if (e.gender === 'MALE')   gender = '<span title="Male" style="color:#2D73B0;margin-right:6px">♂</span>';
        if (e.gender === 'FEMALE') gender = '<span title="Female" style="color:#F46997;margin-right:6px">♀</span>';

        const scale = parseFloat(e.scale);
        const scaleHtml = scale !== 1.0
            ? ` <span style="color:var(--size_variation)">(${scale.toFixed(2)})</span>` : '';

        const cake = e.snack ? '<span title="From snack" style="margin-right:6px">🎂</span>' : '';

        return `<tr>
            <td>${cake}${gender}<strong>${e.species}</strong>${scaleHtml}${specials}</td>
            <td style="color:var(--muted)">${e.form || '–'}</td>
            <td>${e.level}</td>
            <td style="color:var(--muted)">${stripNamespace(e.block_name)}</td>
            <td style="color:var(--muted)">${fmtBiome(e.biome)}</td>
            <td style="color:var(--muted)">${stripNamespace(e.dimension)}</td>
            <td style="color:var(--muted)">${fmtTime(e.ms)}</td>
        </tr>`;
    }).join('');
}