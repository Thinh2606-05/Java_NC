package tfus.fuzzy;

import java.util.Map;

/**
 * Strategy interface for fuzzy membership functions.
 * Given a crisp quantity q and max quantity M(i),
 * returns a map of term -> membership value (only non-zero values included).
 */
public interface MembershipFunction {
    /**
     * Compute fuzzy membership values.
     * @param quantity  crisp quantity q
     * @param maxQty    maximum quantity M(i) for this item
     * @return map of linguistic term -> membership value (Fv), only Fv > 0 included
     */
    Map<String, Double> fuzzify(int quantity, int maxQty);

    /** Human-readable name of this membership function type */
    String typeName();
}
