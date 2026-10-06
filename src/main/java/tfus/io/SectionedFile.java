package tfus.io;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reads a sectioned text file format:
 *
 * [section_name]
 * key=value
 * key2=value2
 *
 * [another_section]
 * ...
 *
 * Lines starting with '#' are comments. Blank lines are ignored.
 */
public final class SectionedFile {

    private final Map<String, Map<String, String>> sections = new LinkedHashMap<>();

    private SectionedFile() {}

    public static SectionedFile read(Path path) throws IOException {
        return read(Files.readAllLines(path));
    }

    public static SectionedFile read(List<String> lines) {
        SectionedFile sf = new SectionedFile();
        String currentSection = null;
        Map<String, String> currentMap = null;

        for (String raw : lines) {
            String line = raw.strip();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1).strip();
                currentMap = sf.sections.computeIfAbsent(currentSection, k -> new LinkedHashMap<>());
            } else if (currentMap != null) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).strip();
                    String val = line.substring(eq + 1).strip();
                    currentMap.put(key, val);
                } else {
                    // Line without '=' treated as key with empty value
                    currentMap.put(line, "");
                }
            }
        }
        return sf;
    }

    public boolean hasSection(String name) {
        return sections.containsKey(name);
    }

    public Map<String, String> section(String name) {
        return sections.getOrDefault(name, Collections.emptyMap());
    }

    public Set<String> sectionNames() {
        return sections.keySet();
    }

    public Optional<String> get(String section, String key) {
        Map<String, String> sec = sections.get(section);
        if (sec == null) return Optional.empty();
        return Optional.ofNullable(sec.get(key));
    }

    public String getOrDefault(String section, String key, String def) {
        return get(section, key).orElse(def);
    }
}
