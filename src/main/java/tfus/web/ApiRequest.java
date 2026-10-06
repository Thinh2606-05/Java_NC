package tfus.web;

import java.util.*;

/**
 * Encapsulates an incoming HTTP API request.
 */
public record ApiRequest(
    String method,
    String path,
    Map<String, String> pathParams,
    Map<String, String> queryParams,
    String body
) {
    public String pathParam(String name) {
        return pathParams.getOrDefault(name, "");
    }

    public String queryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }
}
