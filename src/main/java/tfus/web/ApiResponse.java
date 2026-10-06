package tfus.web;

/**
 * Encapsulates an HTTP API response.
 */
public record ApiResponse(int statusCode, Object body) {

    public static ApiResponse ok(Object body) {
        return new ApiResponse(200, body);
    }

    public static ApiResponse badRequest(String message) {
        return new ApiResponse(400, java.util.Map.of("error", message));
    }

    public static ApiResponse notFound(String message) {
        return new ApiResponse(404, java.util.Map.of("error", message));
    }

    public static ApiResponse serverError(String message) {
        return new ApiResponse(500, java.util.Map.of("error", message));
    }
}
