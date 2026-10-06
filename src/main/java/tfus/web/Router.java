package tfus.web;

import java.util.*;

/**
 * Router điều hướng request theo method và path template (hỗ trợ path param như {id}).
 */
public final class Router {

    public record RouteKey(String method, String template) {}

    public record MatchResult(ApiCommand command, Map<String, String> pathParams, boolean methodMismatch) {
        public static MatchResult matched(ApiCommand command, Map<String, String> pathParams) {
            return new MatchResult(command, pathParams, false);
        }

        public static MatchResult methodNotAllowed() {
            return new MatchResult(null, Collections.emptyMap(), true);
        }

        public static MatchResult notFound() {
            return new MatchResult(null, Collections.emptyMap(), false);
        }
    }

    private static final class RouteEntry {
        final String method;
        final String template;
        final String[] segments;
        final ApiCommand command;

        RouteEntry(String method, String template, ApiCommand command) {
            this.method = method.toUpperCase();
            this.template = template;
            this.segments = template.split("/");
            this.command = command;
        }
    }

    private final List<RouteEntry> routes = new ArrayList<>();

    public Router add(String method, String pathTemplate, ApiCommand command) {
        routes.add(new RouteEntry(method, pathTemplate, command));
        return this;
    }

    public MatchResult match(String method, String path) {
        String cleanPath = path;
        int q = cleanPath.indexOf('?');
        if (q >= 0) cleanPath = cleanPath.substring(0, q);
        String[] reqSegments = cleanPath.split("/");

        boolean pathMatchedDifferentMethod = false;

        for (RouteEntry entry : routes) {
            Map<String, String> params = matchSegments(entry.segments, reqSegments);
            if (params != null) {
                if (entry.method.equalsIgnoreCase(method)) {
                    return MatchResult.matched(entry.command, params);
                } else {
                    pathMatchedDifferentMethod = true;
                }
            }
        }

        if (pathMatchedDifferentMethod) {
            return MatchResult.methodNotAllowed();
        }
        return MatchResult.notFound();
    }

    private Map<String, String> matchSegments(String[] templateSegments, String[] reqSegments) {
        if (templateSegments.length != reqSegments.length) {
            return null;
        }

        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < templateSegments.length; i++) {
            String t = templateSegments[i];
            String r = reqSegments[i];

            if (t.startsWith("{") && t.endsWith("}")) {
                String paramName = t.substring(1, t.length() - 1);
                params.put(paramName, r);
            } else if (!t.equalsIgnoreCase(r)) {
                return null;
            }
        }
        return params;
    }
}
