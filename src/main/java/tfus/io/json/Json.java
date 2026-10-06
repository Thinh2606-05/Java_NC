package tfus.io.json;

import java.util.*;

/**
 * Minimal, self-contained JSON parser and serializer.
 * Supports: objects (Map), arrays (List), strings, numbers (Double/Long), booleans, null.
 * No external dependencies.
 */
public final class Json {

    private Json() {}

    // -------------------------------------------------------------------------
    // PARSING
    // -------------------------------------------------------------------------

    public static Object parse(String json) {
        if (json == null) return null;
        Parser p = new Parser(json.trim());
        return p.parseValue();
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        Object v = parse(json);
        if (v instanceof Map<?,?> m) return (Map<String, Object>) m;
        throw new JsonException("Expected JSON object, got: " + (v == null ? "null" : v.getClass().getSimpleName()));
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseArray(String json) {
        Object v = parse(json);
        if (v instanceof List<?> l) return (List<Object>) l;
        throw new JsonException("Expected JSON array, got: " + (v == null ? "null" : v.getClass().getSimpleName()));
    }

    // -------------------------------------------------------------------------
    // SERIALIZATION
    // -------------------------------------------------------------------------

    public static String write(Object value) {
        StringBuilder sb = new StringBuilder();
        writeValue(value, sb);
        return sb.toString();
    }

    private static void writeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Boolean b) {
            sb.append(b);
        } else if (value instanceof Number n) {
            // Avoid scientific notation for integers
            if (n instanceof Double d) {
                if (d == Math.floor(d) && !Double.isInfinite(d) && Math.abs(d) < 1e15) {
                    sb.append(d.longValue());
                } else {
                    sb.append(d);
                }
            } else if (n instanceof Float f) {
                sb.append(f);
            } else {
                sb.append(n.longValue());
            }
        } else if (value instanceof String s) {
            writeString(s, sb);
        } else if (value instanceof Map<?,?> map) {
            writeObject(map, sb);
        } else if (value instanceof List<?> list) {
            writeArray(list, sb);
        } else if (value instanceof Object[] arr) {
            writeArray(Arrays.asList(arr), sb);
        } else {
            // Fallback: treat as string
            writeString(value.toString(), sb);
        }
    }

    private static void writeString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void writeObject(Map<?,?> map, StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<?,?> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            writeString(entry.getKey().toString(), sb);
            sb.append(':');
            writeValue(entry.getValue(), sb);
        }
        sb.append('}');
    }

    private static void writeArray(List<?> list, StringBuilder sb) {
        sb.append('[');
        boolean first = true;
        for (Object item : list) {
            if (!first) sb.append(',');
            first = false;
            writeValue(item, sb);
        }
        sb.append(']');
    }

    // -------------------------------------------------------------------------
    // INNER PARSER
    // -------------------------------------------------------------------------

    private static final class Parser {
        private final String src;
        private int pos;

        Parser(String src) {
            this.src = src;
            this.pos = 0;
        }

        Object parseValue() {
            skipWhitespace();
            if (pos >= src.length()) throw new JsonException("Unexpected end of input");
            char c = src.charAt(pos);
            return switch (c) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> {
                    if (c == '-' || Character.isDigit(c)) yield parseNumber();
                    throw new JsonException("Unexpected character '" + c + "' at pos " + pos);
                }
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') { pos++; return map; }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                map.put(key, value);
                skipWhitespace();
                char sep = src.charAt(pos++);
                if (sep == '}') break;
                if (sep != ',') throw new JsonException("Expected ',' or '}' at pos " + (pos - 1));
            }
            return map;
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') { pos++; return list; }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                char sep = src.charAt(pos++);
                if (sep == ']') break;
                if (sep != ',') throw new JsonException("Expected ',' or ']' at pos " + (pos - 1));
            }
            return list;
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < src.length()) {
                char c = src.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= src.length()) break;
                    char esc = src.charAt(pos++);
                    switch (esc) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            String hex = src.substring(pos, pos + 4);
                            sb.append((char) Integer.parseInt(hex, 16));
                            pos += 4;
                        }
                        default -> sb.append(esc);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw new JsonException("Unterminated string");
        }

        private Number parseNumber() {
            int start = pos;
            if (peek() == '-') pos++;
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            boolean isFloat = false;
            if (pos < src.length() && src.charAt(pos) == '.') {
                isFloat = true;
                pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            if (pos < src.length() && (src.charAt(pos) == 'e' || src.charAt(pos) == 'E')) {
                isFloat = true;
                pos++;
                if (pos < src.length() && (src.charAt(pos) == '+' || src.charAt(pos) == '-')) pos++;
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) pos++;
            }
            String numStr = src.substring(start, pos);
            if (isFloat) return Double.parseDouble(numStr);
            try {
                return Long.parseLong(numStr);
            } catch (NumberFormatException e) {
                return Double.parseDouble(numStr);
            }
        }

        private Object parseLiteral(String expected, Object result) {
            if (src.startsWith(expected, pos)) {
                pos += expected.length();
                return result;
            }
            throw new JsonException("Unknown literal at pos " + pos);
        }

        private void expect(char c) {
            if (pos >= src.length() || src.charAt(pos) != c) {
                throw new JsonException("Expected '" + c + "' at pos " + pos
                    + " but got '" + (pos < src.length() ? src.charAt(pos) : "EOF") + "'");
            }
            pos++;
        }

        private char peek() {
            return pos < src.length() ? src.charAt(pos) : '\0';
        }

        private void skipWhitespace() {
            while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
        }
    }

    public static final class JsonException extends RuntimeException {
        public JsonException(String msg) { super(msg); }
    }
}
