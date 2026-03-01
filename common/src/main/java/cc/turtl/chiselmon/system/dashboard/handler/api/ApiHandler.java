package cc.turtl.chiselmon.system.dashboard.handler.api;

import cc.turtl.chiselmon.system.tracker.EncounterDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public abstract class ApiHandler implements HttpHandler {

    protected final EncounterDatabase db;

    protected ApiHandler(EncounterDatabase db) {
        this.db = db;
    }

    /**
     * Minimal JSON string escaping to avoid GSON overhead.
     */
    protected static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    protected void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    protected void sendError(HttpExchange exchange, String message) throws IOException {
        sendJson(exchange, 500, "{\"error\":\"" + escape(message) + "\"}");
    }

    /**
     * Parses the {@code from} query parameter as epoch milliseconds.
     * Returns {@code 0} (representing all-time) if absent or unparseable.
     */
    protected static long parseFrom(HttpExchange exchange) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null) return 0;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && kv[0].equals("from")) {
                try {
                    return Long.parseLong(kv[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return 0;
    }

    /**
     * Parses all query parameters into a map, URL-decoding both keys and values.
     * This is required for dimension strings like {@code minecraft:the_nether}
     * which arrive percent-encoded as {@code minecraft%3Athe_nether}.
     */
    protected static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(
                            URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                    );
                } catch (IllegalArgumentException e) {
                    // Fallback to raw value if decoding fails
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }
}