/**
 * Recent encounters table.
 */

import {api, withFrom} from '../core/api.js';
import {fmtBiome, fmtTime, stripNamespace} from '../core/format.js';

const _pokeCache = new Map();

/** Returns and caches species name -> { sprite, types } */
async function fetchPokeData(species) {
    const key = species.toLowerCase();

    // Cache hit
    if (_pokeCache.has(key)) return _pokeCache.get(key);

    // Define the logic: Fetch -> Transform -> Handle Errors
    const promise = fetch(`https://pokeapi.co/api/v2/pokemon/${key}`)
        .then(res => res.ok ? res.json() : Promise.reject())
        .then(data => ({
            sprite: data.sprites?.front_default ?? null,
            types: data.types?.map(t => t.type.name) ?? []
        }))
        .catch(() => {
            _pokeCache.delete(key); // Cleanup cache so we can retry later
            return {sprite: null, types: []};
        });

    // Save the promise and return it
    _pokeCache.set(key, promise);
    return promise;
}

const TYPE_COLORS = {
    normal: {bg: '#E8E8DA', text: '#333'},
    fire: {bg: '#FF6E21', text: '#333'},
    water: {bg: '#3FA5FF', text: '#333'},
    grass: {bg: '#62D14F', text: '#333'},
    electric: {bg: '#FFD314', text: '#333'},
    ice: {bg: '#54F2F2', text: '#333'},
    fighting: {bg: '#EF565D', text: '#333'},
    poison: {bg: '#D651FF', text: '#333'},
    ground: {bg: '#F4A453', text: '#333'},
    flying: {bg: '#B8B2FF', text: '#333'},
    psychic: {bg: '#FF5E9E', text: '#333'},
    bug: {bg: '#D3D319', text: '#333'},
    rock: {bg: '#B7A16E', text: '#333'},
    ghost: {bg: '#9C80F7', text: '#333'},
    dragon: {bg: '#7580FF', text: '#333'},
    dark: {bg: '#587DA0', text: '#333'},
    steel: {bg: '#ABD1F4', text: '#333'},
    fairy: {bg: '#FF7FE5', text: '#333'},
};

function typeTagHtml(typeName) {
    const {bg, text} = TYPE_COLORS[typeName] ?? {bg: '#8b949e', text: '#fff'};
    return `<span class="type-tag" style="background:${bg};color:${text}">${typeName}</span>`;
}

function speciesNameStr(species, form) {
    const isDefault = !form || form.toLowerCase() === 'normal' || form === '–';
    return isDefault ? species : `${species}-${form}`;
}

function genderHtml(gender) {
    if (gender === 'MALE') return '<span title="Male" style="color:#2D73B0;margin-right:6px">♂</span>';
    if (gender === 'FEMALE') return '<span title="Female" style="color:#F46997;margin-right:6px">♀</span>';
    return '<span title="Genderless" style="color:var(--muted);margin-right:6px">•</span>';
}

/** Builds a table row for one encounter with the available local data */
function buildRow(encounter, i) {
    const scale = parseFloat(encounter.scale);
    const scaleHtml = scale !== 1.0 ? ` <span style="color:var(--size_variation)">(${scale.toFixed(2)})</span>` : '';
    const specialsHtml = [
        encounter.shiny && '<span style="color:var(--shiny)" title="Shiny">★</span>',
        encounter.legendary && '<span style="color:var(--legendary)" title="Legendary">★</span>',
    ].filter(Boolean).join(' ');
    const cakeHtml = encounter.snack ? '<span title="From snack" style="margin-right:6px">🎂</span>' : '';

    return `<tr>
        <td class="enc-sprite" data-row="${i}"><div class="sprite-placeholder"></div></td>
        <td>${cakeHtml}${genderHtml(encounter.gender)}<strong>${speciesNameStr(encounter.species, encounter.form)}</strong>${scaleHtml}${specialsHtml}</td>
        <td class="enc-types" data-row="${i}"></td>
        <td>${encounter.level}</td>
        <td style="color:var(--muted)">${stripNamespace(encounter.blockName)}</td>
        <td style="color:var(--muted)">${fmtBiome(encounter.biome)}</td>
        <td style="color:var(--muted)">${fmtTime(encounter.ms)}</td>
    </tr>`;
}

/** Fills in the sprite and type cells for row i once PokeAPI data is available. */
function fillPokeCell(tbody, i, {sprite, types}) {
    const spriteCell = tbody.querySelector(`.enc-sprite[data-row="${i}"]`);
    if (spriteCell) {
        spriteCell.innerHTML = sprite
            ? `<img class="enc-sprite-img" src="${sprite}" alt="" loading="lazy"/>`
            : '<div class="sprite-placeholder sprite-missing">?</div>';
    }

    const typesCell = tbody.querySelector(`.enc-types[data-row="${i}"]`);
    if (typesCell) {
        typesCell.innerHTML = types.length
            ? types.map(typeTagHtml).join('')
            : '<span style="color:var(--muted)">–</span>';
    }
}

export async function loadRecentEncounters() {
    const encounters = await api(withFrom('/api/encounters'));
    const tbody = document.getElementById('enc-tbody');

    if (!encounters.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="loading">No encounters recorded yet.</td></tr>';
        return;
    }

    // requests for external api data (sprites and types)
    const pokeRequests = encounters.map(e => fetchPokeData(e.species));
    tbody.innerHTML = encounters.map(buildRow).join('');

    const pokeData = await Promise.all(pokeRequests);
    pokeData.forEach((poke, i) => fillPokeCell(tbody, i, poke));
}