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
 * GET / -> index.html, GET /index.html -> index.html
 */
public class StaticFileHandler implements HttpHandler {
    private static final String RESOURCE_ROOT = "/assets/chiselmon/dashboard";

    private static final Map<String, String> ROUTES = Map.of(
            "/", "index.html",
            "/index.html", "index.html",
            "/dashboard.css", "dashboard.css",
            "/dashboard.js", "dashboard.js"
    );

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "html", "text/html; charset=utf-8",
            "css", "text/css; charset=utf-8",
            "js", "text/javascript; charset=utf-8"
    );

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String filename = ROUTES.get(path);

        if (filename == null) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        String contentType = CONTENT_TYPES.getOrDefault(ext, "application/octet-stream");
        serveResource(exchange, RESOURCE_ROOT + "/" + filename, contentType);
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