package tfus.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import tfus.io.json.Json;
import tfus.service.PresetCatalog;
import tfus.web.command.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * WebServer tự xây dựng sử dụng com.sun.net.httpserver.HttpServer có sẵn trong JDK tiêu chuẩn.
 * Tích hợp Router, Json parser/serializer nội bộ và StaticFileHandler.
 */
public final class WebServer {

    private final int port;
    private final Path staticDir;
    private final Path dataDir;
    private final Router router = new Router();
    private HttpServer server;

    public WebServer(int port, Path staticDir, Path dataDir) {
        this.port = port;
        this.staticDir = staticDir;
        this.dataDir = dataDir;
        registerRoutes();
    }

    private void registerRoutes() {
        PresetCatalog catalog = new PresetCatalog(dataDir);

        router.add("GET", "/api/presets", new ListPresetsCommand(catalog));
        router.add("GET", "/api/presets/{id}", new GetPresetCommand(catalog, dataDir));
        router.add("POST", "/api/mine", new RunMiningCommand());
        router.add("GET", "/api/verify/{id}", new VerifyCommand(catalog, dataDir));
        router.add("GET", "/api/membership-functions", new MembershipFunctionsCommand());
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.setExecutor(Executors.newFixedThreadPool(4));

        // API Handler
        server.createContext("/api/", this::handleApi);

        // Static Files Handler
        server.createContext("/", new StaticFileHandler(staticDir));

        server.start();
        System.out.printf("TFUS WebServer started on http://127.0.0.1:%d%n", port);
    }

    public void stop(int delaySeconds) {
        if (server != null) {
            server.stop(delaySeconds);
            System.out.println("TFUS WebServer stopped.");
        }
    }

    private void handleApi(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String uriPath = exchange.getRequestURI().getPath();

        // Parse query params
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        // Parse body if present
        String body = "";
        if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
            try (InputStream is = exchange.getRequestBody()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }

        Router.MatchResult match = router.match(method, uriPath);

        ApiResponse response;
        if (match.methodMismatch()) {
            response = new ApiResponse(405, Map.of("error", "Method Not Allowed"));
        } else if (match.command() == null) {
            response = ApiResponse.notFound("Endpoint not found: " + uriPath);
        } else {
            ApiRequest request = new ApiRequest(method, uriPath, match.pathParams(), queryParams, body);
            try {
                response = match.command().execute(request);
            } catch (IllegalArgumentException | IllegalStateException e) {
                response = ApiResponse.badRequest(e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                response = ApiResponse.serverError("Internal server error: " + e.getMessage());
            }
        }

        sendJsonResponse(exchange, response);
    }

    private void sendJsonResponse(HttpExchange exchange, ApiResponse response) throws IOException {
        String json = Json.write(response.body());
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(response.statusCode(), bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return map;

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int eq = pair.indexOf('=');
            if (eq > 0) {
                map.put(pair.substring(0, eq), pair.substring(eq + 1));
            } else {
                map.put(pair, "");
            }
        }
        return map;
    }
}
