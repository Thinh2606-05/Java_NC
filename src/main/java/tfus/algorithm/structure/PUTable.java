package tfus.algorithm.structure;

import java.util.*;

/**
 * PU-Table: Prefix Utility Table.
 * For a given prefix list X, stores the fuzzy utility of X in each transaction
 * (indexed by tidIndex). Used during pattern extension to compute combined
 * utility for patterns X∪Y.
 *
 * CT for combined utility: U(X∪Y, T) = U(X, T) + U(Y, T) - U(prefix, T)
 * where U(prefix, T) is looked up from PUTable.
 */
public final class PUTable {
    private final Map<Integer, Double> prefixUtility = new HashMap<>();

    private PUTable() {}

    /**
     * Build PUTable from the entries of the prefix list X.
     * prefixUtility[tidIndex] = fuzzyUtility of the entry
     */
    public static PUTable of(TFUSList prefixList) {
        PUTable t = new PUTable();
        for (Entry e : prefixList.entries()) {
            t.prefixUtility.put(e.tidIndex(), e.fuzzyUtility());
        }
        return t;
    }

    /**
     * Build empty PUTable (when there is no prefix, i.e., we are at the root).
     */
    public static PUTable empty() {
        return new PUTable();
    }

    /** Get prefix utility for a transaction. Returns 0.0 if not present. */
    public double get(int tidIndex) {
        return prefixUtility.getOrDefault(tidIndex, 0.0);
    }

    public boolean contains(int tidIndex) {
        return prefixUtility.containsKey(tidIndex);
    }
}
