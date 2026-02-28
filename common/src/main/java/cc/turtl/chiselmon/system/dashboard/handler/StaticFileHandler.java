package cc.turtl.chiselmon.system.dashboard.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Serves static files from /assets/chiselmon/dashboard/.
 * <p>
 * Resolves paths dynamically so that new JS/CSS modules can be added
 * without touching this class.
 * <p>
 * Only files under the resource root with known extensions are served.
 * Path traversal (../) is rejected.
 */
public class StaticFileHandler implements HttpHandler {
    private static final String RESOURCE_ROOT = "/assets/chiselmon/dashboard";

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css",  "text/css; charset=utf-8",
            "js",   "text/javascript; charset=utf-8"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();

        // Root → index.html
        if ("/".equals(path) || "/index.html".equals(path)) {
            serveResource(exchange, RESOURCE_ROOT + "/index.html", "text/html; charset=utf-8");
            return;
        }

        // Block path traversal
        if (path.contains("..")) {
            exchange.sendResponseHeaders(400, -1);
            return;
        }

        // Derive file extension and content type
        int dot = path.lastIndexOf('.');
        if (dot == -1) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String ext = path.substring(dot + 1);
        String contentType = CONTENT_TYPES.get(ext);
        if (contentType == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        serveResource(exchange, RESOURCE_ROOT + path, contentType);
    }

    private void serveResource(HttpExchange exchange, String resourcePath, String contentType) throws IOException {
        try (InputStream in = StaticFileHandler.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                String body = "Resource not found: " + resourcePath;
                exchange.getResponseHeaders().set("Content-Type", "text/plain");
                exchange.sendResponseHeaders(404, body.length());
                exchange.getResponseBody().write(body.getBytes());
                return;
            }

            byte[] data = in.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        }
    }
}