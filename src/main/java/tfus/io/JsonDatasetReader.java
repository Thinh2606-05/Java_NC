package tfus.io;

import tfus.domain.*;
import tfus.io.json.Json;
import java.util.*;

/**
 * Reads dataset from a JSON structure:
 * {
 *   "name": "lab1",
 *   "config": { "windowSize": 3, "batchSize": 2, "delta": 0.2, "pruningStrategy": "REVISED_UPPER_BOUND" },
 *   "items": [ {"item": "A", "eu": 4, "m": 6}, ... ],
 *   "transactions": [
 *     {"tid": "T1", "items": [{"item": "A", "qty": 4}, ...]},
 *     ...
 *   ]
 * }
 */
public final class JsonDatasetReader implements DatasetReader {

    @Override
    public MiningInput read(String jsonSource) throws Exception {
        Map<String, Object> root = Json.parseObject(jsonSource);
        return parse(root);
    }

    @SuppressWarnings("unchecked")
    public static MiningInput parse(Map<String, Object> root) {
        String name = str(root, "name", "dataset");

        // Config
        Map<String, Object> cfg = (Map<String, Object>) root.getOrDefault("config", Map.of());
        int windowSize = num(cfg, "windowSize", 3);
        int batchSize  = num(cfg, "batchSize", 2);
        int periodSize = num(cfg, "periodSize", batchSize);
        double delta   = dbl(cfg, "delta", 0.2);
        String pruning = str(cfg, "pruningStrategy", "REVISED_UPPER_BOUND");
        String recursionStr = str(cfg, "recursionRule", "AT_LEAST_ONE");
        MiningConfig.RecursionRule recursion;
        try {
            recursion = MiningConfig.RecursionRule.valueOf(recursionStr);
        } catch (IllegalArgumentException e) {
            recursion = MiningConfig.RecursionRule.AT_LEAST_ONE;
        }

        MiningConfig config = MiningConfig.builder()
            .windowSize(windowSize)
            .delta(delta)
            .pruningStrategy(pruning)
            .recursionRule(recursion)
            .build();

        Dataset.Builder db = new Dataset.Builder()
            .name(name)
            .batchSize(batchSize)
            .periodSize(periodSize);

        // Items
        List<Object> items = (List<Object>) root.getOrDefault("items", List.of());
        for (Object o : items) {
            Map<String, Object> im = (Map<String, Object>) o;
            String item = str(im, "item", "");
            double eu = dbl(im, "eu", 1.0);
            db.eu(item, eu);
            if (im.containsKey("m")) {
                db.maxQuantity(item, num(im, "m", 1));
            }
        }

        // Transactions
        List<Object> txns = (List<Object>) root.getOrDefault("transactions", List.of());
        for (Object o : txns) {
            Map<String, Object> tm = (Map<String, Object>) o;
            String tid = str(tm, "tid", "");
            List<Object> its = (List<Object>) tm.getOrDefault("items", List.of());
            List<ItemQuantity> iqs = new ArrayList<>();
            for (Object io : its) {
                Map<String, Object> iq = (Map<String, Object>) io;
                String item = str(iq, "item", "");
                int qty = num(iq, "qty", 1);
                iqs.add(new ItemQuantity(item, qty));
            }
            db.addTransaction(tid, iqs);
        }

        return new MiningInput(db.build(), config);
    }

    // -------------------------------------------------------------------------
    private static String str(Map<String, Object> m, String k, String def) {
        Object v = m.get(k);
        return v instanceof String s ? s : def;
    }

    private static int num(Map<String, Object> m, String k, int def) {
        Object v = m.get(k);
        if (v instanceof Number n) return n.intValue();
        return def;
    }

    private static double dbl(Map<String, Object> m, String k, double def) {
        Object v = m.get(k);
        if (v instanceof Number n) return n.doubleValue();
        return def;
    }
}
