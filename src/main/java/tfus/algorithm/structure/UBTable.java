package tfus.algorithm.structure;

import java.util.*;

/**
 * Stores per-transaction utility bounds used for FUUB/MTFU computations.
 *
 * For each transaction (by tidIndex):
 *   - TFU: transaction fuzzy utility (sum of Fv_j * U_j over all fuzzy items in T)
 *   - MTFU: max-term transaction fuzzy utility (sum of max_term(Fv_j) * U_j)
 *   - periodNumber: the period this transaction belongs to
 *   - batchNumber: the batch this transaction belongs to
 */
public final class UBTable {

    public record Row(int tidIndex, String tid, int batchNumber, int periodNumber,
                      double tfu, double mtfu) {}

    private final Map<Integer, Row> rows = new LinkedHashMap<>();
    // period -> list of tidIndex sorted
    private final Map<Integer, List<Integer>> byPeriod = new TreeMap<>();

    public void put(int tidIndex, String tid, int batchNumber, int periodNumber,
                    double tfu, double mtfu) {
        rows.put(tidIndex, new Row(tidIndex, tid, batchNumber, periodNumber, tfu, mtfu));
        byPeriod.computeIfAbsent(periodNumber, k -> new ArrayList<>()).add(tidIndex);
    }

    public Row getRow(int tidIndex) {
        return rows.get(tidIndex);
    }

    public double getMtfu(int tidIndex) {
        Row r = rows.get(tidIndex);
        return r != null ? r.mtfu() : 0.0;
    }

    public double getTfu(int tidIndex) {
        Row r = rows.get(tidIndex);
        return r != null ? r.tfu() : 0.0;
    }

    /** Sum of TFU for all transactions with period >= fromPeriod */
    public double sumTfuFromPeriod(int fromPeriod) {
        double sum = 0.0;
        for (Map.Entry<Integer, List<Integer>> e : byPeriod.entrySet()) {
            if (e.getKey() >= fromPeriod) {
                for (int idx : e.getValue()) {
                    sum += getTfu(idx);
                }
            }
        }
        return sum;
    }

    /** Remove all rows belonging to a given batch. */
    public void removeBatch(int batchNumber) {
        Iterator<Map.Entry<Integer, Row>> it = rows.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, Row> e = it.next();
            if (e.getValue().batchNumber() == batchNumber) {
                int period = e.getValue().periodNumber();
                List<Integer> plist = byPeriod.get(period);
                if (plist != null) {
                    plist.remove((Integer) e.getKey());
                    if (plist.isEmpty()) byPeriod.remove(period);
                }
                it.remove();
            }
        }
    }

    public Collection<Row> allRows() {
        return Collections.unmodifiableCollection(rows.values());
    }

    public int size() { return rows.size(); }
}
