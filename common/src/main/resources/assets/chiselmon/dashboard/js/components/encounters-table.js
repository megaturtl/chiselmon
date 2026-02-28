/**
 * Recent encounters table renderer.
 * Fetches sprite + type data from PokeAPI with a persistent cache.
 */

import {api, withFrom} from '../core/api.js';
import {fmtBiome, fmtTime, stripNamespace} from '../core/format.js';

// ── PokeAPI cache ─────────────────────────────────────────────────────────────

const _pokeCache = new Map();

/**
 * Fetches sprite URL and types for a species name.
 * Results are cached so each species is only fetched once per session.
 * Returns { sprite, types } or a fallback on failure.
 */
async function fetchPokeData(species) {
    const key = species.toLowerCase();
    if (_pokeCache.has(key)) return _pokeCache.get(key);

    const promise = (async () => {
        try {
            const r = await fetch(`https://pokeapi.co/api/v2/pokemon/${key}`);
            if (!r.ok) throw new Error(r.status);
            const data = await r.json();
            return {
                sprite: data.sprites?.front_default ?? null,
                types: data.types?.map(t => t.type.name) ?? [],
            };
        } catch (_) {
            return {sprite: null, types: []};
        }
    })();

    _pokeCache.set(key, promise);
    return promise;
}

// ── Type colours ──────────────────────────────────────────────────────────────

const TYPE_COLORS = {
    normal: {bg: '#a8a878', text: '#fff'},
    fire: {bg: '#f08030', text: '#fff'},
    water: {bg: '#6890f0', text: '#fff'},
    electric: {bg: '#f8d030', text: '#333'},
    grass: {bg: '#78c850', text: '#fff'},
    ice: {bg: '#98d8d8', text: '#333'},
    fighting: {bg: '#c03028', text: '#fff'},
    poison: {bg: '#a040a0', text: '#fff'},
    ground: {bg: '#e0c068', text: '#333'},
    flying: {bg: '#a890f0', text: '#fff'},
    psychic: {bg: '#f85888', text: '#fff'},
    bug: {bg: '#a8b820', text: '#fff'},
    rock: {bg: '#b8a038', text: '#fff'},
    ghost: {bg: '#705898', text: '#fff'},
    dragon: {bg: '#7038f8', text: '#fff'},
    dark: {bg: '#705848', text: '#fff'},
    steel: {bg: '#b8b8d0', text: '#333'},
    fairy: {bg: '#ee99ac', text: '#333'},
};

function typeTag(typeName) {
    const c = TYPE_COLORS[typeName] ?? {bg: '#8b949e', text: '#fff'};
    return `<span class="type-tag" style="background:${c.bg};color:${c.text}">${typeName}</span>`;
}

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Builds the display name: "Pikachu" if form is empty/normal,
 * "Pikachu-Alola" otherwise.
 */
function displayName(species, form) {
    if (!form || form.toLowerCase() === 'normal' || form === '–') {
        return species;
    }
    return `${species}-${form}`;
}

// ── Table rendering ───────────────────────────────────────────────────────────

export async function loadEncounters() {
    const data  = await api(withFrom('/api/encounters'));
    const tbody = document.getElementById('enc-tbody');

    if (!data.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No encounters recorded yet.</td></tr>';
        return;
    }

    // Kick off all PokeAPI fetches in parallel
    const pokeDataPromises = data.map(e => fetchPokeData(e.species));

    tbody.innerHTML = data.map((e, i) => {
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

        const name = displayName(e.species, e.form);

        return `<tr>
            <td class="enc-sprite" data-row="${i}">
                <div class="sprite-placeholder"></div>
            </td>
            <td>${cake}${gender}<strong>${name}</strong>${scaleHtml}${specials}</td>
            <td class="enc-types" data-row="${i}"></td>
            <td>${e.level}</td>
            <td style="color:var(--muted)">${stripNamespace(e.block_name)}</td>
            <td style="color:var(--muted)">${fmtBiome(e.biome)}</td>
            <td style="color:var(--muted)">${fmtTime(e.ms)}</td>
        </tr>`;
    }).join('');

    // Fill in sprites + types as they resolve
    const resolved = await Promise.all(pokeDataPromises);
    resolved.forEach((poke, i) => {
        const spriteCell = tbody.querySelector(`.enc-sprite[data-row="${i}"]`);
        if (spriteCell) {
            if (poke.sprite) {
                spriteCell.innerHTML = `<img class="enc-sprite-img" src="${poke.sprite}" alt="" loading="lazy"/>`;
            } else {
                spriteCell.innerHTML = '<div class="sprite-placeholder sprite-missing">?</div>';
            }
        }

        const typesCell = tbody.querySelector(`.enc-types[data-row="${i}"]`);
        if (typesCell && poke.types.length) {
            typesCell.innerHTML = poke.types.map(typeTag).join('');
        } else if (typesCell) {
            typesCell.innerHTML = '<span style="color:var(--muted)">–</span>';
        }
    });
}