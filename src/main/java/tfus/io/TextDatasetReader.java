package tfus.io;

import tfus.domain.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reads dataset from a plain-text file in the TFUS format:
 *
 * [config]
 * windowSize=3
 * batchSize=2
 * delta=0.2
 *
 * [items]
 * # item : EU : M(optional)
 * A:4:6
 * B:5:8
 * ...
 *
 * [transactions]
 * # TID : item1(qty1) item2(qty2) ...
 * T1:A(4) B(6) C(2) D(2)
 * T2:A(2) C(6) D(5)
 * ...
 */
public final class TextDatasetReader implements DatasetReader {
    private final Path basePath;

    public TextDatasetReader(Path basePath) {
        this.basePath = basePath;
    }

    public TextDatasetReader() {
        this(Path.of("."));
    }

    @Override
    public MiningInput read(String source) throws Exception {
        Path path = basePath.resolve(source);
        SectionedFile sf = SectionedFile.read(path);
        return parse(sf, path.getFileName().toString().replaceFirst("\\.txt$", ""));
    }

    public static MiningInput parseText(String content, String name) throws Exception {
        List<String> lines = Arrays.asList(content.split("\\r?\\n"));
        SectionedFile sf = SectionedFile.read(lines);
        return parse(sf, name);
    }

    private static MiningInput parse(SectionedFile sf, String name) {
        // Config
        Map<String, String> cfg = sf.section("config");
        int windowSize = Integer.parseInt(cfg.getOrDefault("windowSize", "3"));
        int batchSize  = Integer.parseInt(cfg.getOrDefault("batchSize", "2"));
        int periodSize = Integer.parseInt(cfg.getOrDefault("periodSize", String.valueOf(batchSize)));
        double delta   = Double.parseDouble(cfg.getOrDefault("delta", "0.2"));
        String pruning = cfg.getOrDefault("pruningStrategy", "REVISED_UPPER_BOUND");

        MiningConfig config = MiningConfig.builder()
            .windowSize(windowSize)
            .delta(delta)
            .pruningStrategy(pruning)
            .build();

        // Items: EU and optional M
        Dataset.Builder db = new Dataset.Builder()
            .name(name)
            .batchSize(batchSize)
            .periodSize(periodSize);

        for (Map.Entry<String, String> e : sf.section("items").entrySet()) {
            // Key is the item name; value is "EU" or "EU:M"
            String item = e.getKey();
            String val  = e.getValue();
            String[] parts = val.split(":");
            double eu = Double.parseDouble(parts[0].trim());
            db.eu(item, eu);
            if (parts.length >= 2) {
                db.maxQuantity(item, Integer.parseInt(parts[1].trim()));
            }
        }

        // Transactions
        for (Map.Entry<String, String> e : sf.section("transactions").entrySet()) {
            String tid = e.getKey();
            String rest = e.getValue();
            List<ItemQuantity> items = new ArrayList<>();
            for (String token : rest.trim().split("\\s+")) {
                if (token.isEmpty()) continue;
                // Format: A(4) or A:4
                int lp = token.indexOf('(');
                if (lp >= 0) {
                    String item = token.substring(0, lp);
                    int qty = Integer.parseInt(token.substring(lp + 1, token.length() - 1));
                    items.add(new ItemQuantity(item, qty));
                } else if (token.contains(":")) {
                    String[] p = token.split(":");
                    items.add(new ItemQuantity(p[0], Integer.parseInt(p[1])));
                }
            }
            db.addTransaction(tid, items);
        }

        return new MiningInput(db.build(), config);
    }
}
