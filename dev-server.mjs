/**
 * Chiselmon dashboard dev proxy. Edit and test static html/css/js files without needing the rebuild and restart mc.
 *
 * Usage:
 *   node dev-server.mjs [game-port] [dev-port]
 *
 * Defaults:
 *   game-port 7890
 *   dev-port 7891
 *
 */

import http from 'http';
import fs from 'fs';
import path from 'path';
import {fileURLToPath} from 'url';

const GAME_PORT = parseInt(process.argv[2]) || 7890;
const DEV_PORT = parseInt(process.argv[3]) || 7891;

// Root of the static assets on disk (from repository root)
const ASSETS_ROOT = path.resolve(
    path.dirname(fileURLToPath(import.meta.url)),
    'common/src/main/resources/assets/chiselmon/dashboard'
);

const CONTENT_TYPES = {
    '.html': 'text/html; charset=utf-8',
    '.css': 'text/css; charset=utf-8',
    '.js': 'text/javascript; charset=utf-8',
    '.json': 'application/json; charset=utf-8',
    '.png': 'image/png',
    '.ico': 'image/x-icon',
};

function serveStatic(req, res) {
    const urlPath = req.url.split('?')[0];
    const filePath = path.join(ASSETS_ROOT, urlPath === '/' ? 'index.html' : urlPath);

    // Stay inside ASSETS_ROOT - block traversal attempts
    if (!filePath.startsWith(ASSETS_ROOT)) {
        res.writeHead(400);
        res.end('Bad request');
        return;
    }

    fs.readFile(filePath, (err, data) => {
        if (err) {
            res.writeHead(404, {'Content-Type': 'text/plain'});
            res.end(`Not found: ${urlPath}`);
            return;
        }

        const ext = path.extname(filePath);
        const contentType = CONTENT_TYPES[ext] ?? 'application/octet-stream';
        res.writeHead(200, {'Content-Type': contentType});
        res.end(data);
    });
}

function proxyToGame(req, res) {
    const options = {
        hostname: '127.0.0.1',
        port: GAME_PORT,
        path: req.url,
        method: req.method,
        headers: req.headers,
    };

    const proxy = http.request(options, gameRes => {
        res.writeHead(gameRes.statusCode, gameRes.headers);
        gameRes.pipe(res);
    });

    proxy.on('error', err => {
        res.writeHead(502, {'Content-Type': 'text/plain'});
        res.end(`Proxy error - is the backend running on port ${GAME_PORT}?\n${err.message}`);
    });

    req.pipe(proxy);
}

const server = http.createServer((req, res) => {
    // API calls go to the live Java server; everything else is served from disk
    if (req.url.startsWith('/api/')) {
        proxyToGame(req, res);
    } else {
        serveStatic(req, res);
    }
});

server.listen(DEV_PORT, '127.0.0.1', () => {
    console.log(`Chiselmon dev server running at http://localhost:${DEV_PORT}/`);
    console.log(`  Static files: ${ASSETS_ROOT}`);
    console.log(`  API proxy -> http://127.0.0.1:${GAME_PORT}/api/`);
    console.log();
    console.log('Edit any JS/CSS/HTML file and refresh the browser to test - no rebuild needed.');
});
