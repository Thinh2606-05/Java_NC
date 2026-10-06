package tfus.domain;

import java.util.List;
import java.util.Collections;

/**
 * A discovered frequent fuzzy utility pattern.
 * items: the fuzzy items forming the pattern
 * tFUR: the pattern's temporal fuzzy utility ratio (tFUR >= delta means it's a result)
 * startPeriod: earliest period (PS) from which utility is counted
 */
public final class Pattern {
    private final List<FuzzyItem> items;
    private final double tFUR;
    private final int startPeriod;

    public Pattern(List<FuzzyItem> items, double tFUR, int startPeriod) {
        this.items = Collections.unmodifiableList(List.copyOf(items));
        this.tFUR = tFUR;
        this.startPeriod = startPeriod;
    }

    public List<FuzzyItem> items() { return items; }
    public double tFUR() { return tFUR; }
    public int startPeriod() { return startPeriod; }

    public String label() {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(items.get(i).label());
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return label() + " tFUR=" + String.format("%.4f", tFUR);
    }
}
