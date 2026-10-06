package tfus.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Phục vụ static files từ thư mục web/ (HTML, CSS, JS).
 * Có kiểm tra chống directory traversal.
 */
public final class StaticFileHandler implements HttpHandler {

    private final Path rootDir;

    public StaticFileHandler(Path rootDir) {
        this.rootDir = rootDir.toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String rawPath = exchange.getRequestURI().getPath();
        if (rawPath.startsWith("/api/")) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String reqPath = rawPath.equals("/") ? "/index.html" : rawPath;
        if (reqPath.startsWith("/")) reqPath = reqPath.substring(1);

        Path filePath = rootDir.resolve(reqPath).normalize();
        if (!filePath.startsWith(rootDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            byte[] notFound = "404 Not Found".getBytes();
            exchange.sendResponseHeaders(404, notFound.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(notFound);
            }
            return;
        }

        String mime = getMimeType(filePath);
        exchange.getResponseHeaders().set("Content-Type", mime);

        byte[] bytes = Files.readAllBytes(filePath);
        exchange.sendResponseHeaders(200, bytes.length);
        if ("GET".equalsIgnoreCase(method)) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    private String getMimeType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".html") || name.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (name.endsWith(".css")) return "text/css; charset=UTF-8";
        if (name.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (name.endsWith(".json")) return "application/json; charset=UTF-8";
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".svg")) return "image/svg+xml";
        if (name.endsWith(".ico")) return "image/x-icon";
        if (name.endsWith(".txt")) return "text/plain; charset=UTF-8";
        return "application/octet-stream";
    }
}
