package tfus.snapshot;

import tfus.domain.FuzzyItem;
import java.util.*;

/**
 * Immutable snapshot of TFUS-list states (Memento pattern).
 * Captures the state of all lists at a particular point in time
 * (e.g., after restructure) without exposing internal implementation.
 */
public final class ListsSnapshot {
    private final int windowNumber;
    private final String label;
    private final List<ListSnapshot> lists;

    public ListsSnapshot(int windowNumber, String label, List<ListSnapshot> lists) {
        this.windowNumber = windowNumber;
        this.label = label;
        this.lists = Collections.unmodifiableList(List.copyOf(lists));
    }

    public int windowNumber() { return windowNumber; }
    public String label() { return label; }
    public List<ListSnapshot> lists() { return lists; }

    // -------------------------------------------------------------------------
    /**
     * Snapshot of a single TFUS-list.
     */
    public record ListSnapshot(
        FuzzyItem fuzzyItem,
        double fu,
        double fuub,
        double rfu,
        double mstfu,
        double tfuubr,
        List<EntrySnapshot> entries
    ) {}

    /**
     * Snapshot of a single entry within a TFUS-list.
     */
    public record EntrySnapshot(
        String tid, int batchNumber, int periodNumber,
        double utility, double minFv, double remainingUtility
    ) {}
}
