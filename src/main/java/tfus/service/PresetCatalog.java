package tfus.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Discovers available dataset presets from the data/ directory.
 */
public final class PresetCatalog {
    private final Path dataDir;

    public PresetCatalog(Path dataDir) {
        this.dataDir = dataDir;
    }

    public PresetCatalog() {
        this(Path.of("data"));
    }

    /**
     * List all available preset IDs (dataset names without extension).
     */
    public List<String> listPresetIds() {
        try {
            if (!Files.isDirectory(dataDir)) return List.of();
            return Files.list(dataDir)
                .filter(p -> p.toString().endsWith(".txt") && !p.toString().endsWith("_handrun.txt"))
                .map(p -> p.getFileName().toString().replaceFirst("\\.txt$", ""))
                .sorted()
                .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    /**
     * Get the path to a dataset file by ID.
     */
    public Path datasetPath(String id) {
        return dataDir.resolve(id + ".txt");
    }

    /**
     * Get the path to the hand-run verification file for a dataset ID.
     */
    public Path handRunPath(String id) {
        return dataDir.resolve(id + "_handrun.txt");
    }

    /**
     * Check if a hand-run file exists for the given ID.
     */
    public boolean hasHandRun(String id) {
        return Files.exists(handRunPath(id));
    }

    /**
     * Read the content of a dataset file.
     */
    public String readDataset(String id) throws IOException {
        return Files.readString(datasetPath(id));
    }
}
