package tfus.algorithm.structure;

import tfus.domain.Batch;
import java.util.*;

/**
 * Sliding window managing the set of active batches.
 * Tracks which batches are currently in scope and earliest periods of items.
 */
public final class SlidingWindow {
    private final int windowSize;
    private final Deque<Batch> batches = new ArrayDeque<>();

    // Item -> earliest period seen across all batches in window
    private final Map<String, Integer> itemStartPeriod = new LinkedHashMap<>();

    public SlidingWindow(int windowSize) {
        this.windowSize = windowSize;
    }

    public void addBatch(Batch batch) {
        batches.addLast(batch);
        for (var txn : batch.transactions()) {
            for (var iq : txn.items()) {
                itemStartPeriod.merge(iq.item(), txn.periodNumber(), Math::min);
            }
        }
    }

    /** Remove the oldest batch and recompute item start periods. */
    public Batch removeOldest() {
        Batch oldest = batches.pollFirst();
        if (oldest != null) {
            recomputeStartPeriods();
        }
        return oldest;
    }

    private void recomputeStartPeriods() {
        itemStartPeriod.clear();
        for (Batch b : batches) {
            for (var txn : b.transactions()) {
                for (var iq : txn.items()) {
                    itemStartPeriod.merge(iq.item(), txn.periodNumber(), Math::min);
                }
            }
        }
    }

    public boolean isFull() {
        return batches.size() >= windowSize;
    }

    public int currentSize() { return batches.size(); }

    public Deque<Batch> batches() { return new ArrayDeque<>(batches); }

    /**
     * Get earliest period across all items in the pattern (their start periods).
     * Used to compute PS for a set of items.
     * Returns the maximum start period (latest early period = most constraining).
     */
    public int startPeriodOf(Collection<String> items) {
        int maxStart = 1;
        for (String item : items) {
            Integer sp = itemStartPeriod.get(item);
            if (sp != null && sp > maxStart) maxStart = sp;
        }
        return maxStart;
    }

    /** Map of item -> earliest period for all items in window */
    public Map<String, Integer> startPeriodOfItems() {
        return Collections.unmodifiableMap(itemStartPeriod);
    }

    public int minPeriod() {
        return batches.stream()
            .flatMap(b -> b.transactions().stream())
            .mapToInt(t -> t.periodNumber())
            .min().orElse(1);
    }

    public int maxPeriod() {
        return batches.stream()
            .flatMap(b -> b.transactions().stream())
            .mapToInt(t -> t.periodNumber())
            .max().orElse(1);
    }
}
