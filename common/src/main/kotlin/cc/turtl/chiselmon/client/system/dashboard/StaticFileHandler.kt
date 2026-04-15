package cc.turtl.chiselmon.client.system.dashboard

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler

/**
 * Serves static files from `/assets/chiselmon/dashboard/`.
 *
 * Resolves paths dynamically so that new JS/CSS modules can be added without touching this
 * class. Only files under the resource root with known extensions are served. Path traversal
 * (`..`) is rejected.
 */
class StaticFileHandler : HttpHandler {

    override fun handle(exchange: HttpExchange) {
        if (!"GET".equals(exchange.requestMethod, ignoreCase = true)) {
            exchange.sendResponseHeaders(405, -1)
            return
        }

        val path = exchange.requestURI.path

        // Root resolves to index.html
        if (path == "/" || path == "/index.html") {
            serveResource(exchange, "$RESOURCE_ROOT/index.html", "text/html; charset=utf-8")
            return
        }

        // Block path traversal
        if (".." in path) {
            exchange.sendResponseHeaders(400, -1)
            return
        }

        // Derive file extension and content type
        val ext = path.substringAfterLast('.', "")
        val contentType = CONTENT_TYPES[ext]
        if (ext.isEmpty() || contentType == null) {
            exchange.sendResponseHeaders(404, -1)
            return
        }

        serveResource(exchange, "$RESOURCE_ROOT$path", contentType)
    }

    private fun serveResource(exchange: HttpExchange, resourcePath: String, contentType: String) {
        val stream = StaticFileHandler::class.java.getResourceAsStream(resourcePath)
        if (stream == null) {
            val body = "Resource not found: $resourcePath"
            exchange.responseHeaders.set("Content-Type", "text/plain")
            exchange.sendResponseHeaders(404, body.length.toLong())
            exchange.responseBody.write(body.toByteArray())
            return
        }

        stream.use { s ->
            val data = s.readAllBytes()
            exchange.responseHeaders.set("Content-Type", contentType)
            exchange.sendResponseHeaders(200, data.size.toLong())
            exchange.responseBody.use { it.write(data) }
        }
    }

    companion object {
        private const val RESOURCE_ROOT = "/assets/chiselmon/dashboard"

        private val CONTENT_TYPES = mapOf(
            "html" to "text/html; charset=utf-8",
            "css" to "text/css; charset=utf-8",
            "js" to "text/javascript; charset=utf-8",
        )
    }
}
