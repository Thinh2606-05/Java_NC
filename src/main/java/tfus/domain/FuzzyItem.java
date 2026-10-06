package tfus.domain;

/**
 * A fuzzy linguistic term applied to an item.
 * e.g. FuzzyItem("A", "H") means item A with High membership.
 */
public record FuzzyItem(String item, String term) implements Comparable<FuzzyItem> {
    public FuzzyItem {
        if (item == null || item.isBlank()) throw new IllegalArgumentException("Item must not be blank");
        if (term == null || term.isBlank()) throw new IllegalArgumentException("Term must not be blank");
    }

    /** Display name like "A.H" */
    public String label() {
        return item + "." + term;
    }

    @Override
    public int compareTo(FuzzyItem other) {
        int c = this.item.compareTo(other.item);
        return c != 0 ? c : this.term.compareTo(other.term);
    }

    @Override
    public String toString() {
        return label();
    }
}
