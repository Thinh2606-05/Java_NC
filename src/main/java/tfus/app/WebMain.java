package tfus.app;

import tfus.web.WebServer;

import java.nio.file.Path;

/**
 * Entry point khởi chạy Web Server thuần Java cho ứng dụng TFUS.
 */
public final class WebMain {

    public static void main(String[] args) {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        Path staticDir = Path.of("web");
        Path dataDir = Path.of("data");

        WebServer server = new WebServer(port, staticDir, dataDir);
        try {
            server.start();
            System.out.println("TFUS Application is running.");
            System.out.printf("Open your browser at: http://localhost:%d%n", port);
            System.out.println("Press Ctrl+C to terminate.");
        } catch (Exception e) {
            System.err.println("Failed to start web server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
