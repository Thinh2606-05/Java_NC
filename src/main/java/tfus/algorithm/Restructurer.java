package tfus.algorithm;

import tfus.algorithm.pruning.Thresholds;
import tfus.algorithm.structure.*;
import tfus.domain.FuzzyItem;
import tfus.event.*;
import tfus.snapshot.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the restructuring phase of the TFUS algorithm.
 *
 * Steps:
 * 1. Compute FUUB/TFUUBR for each singleton list; remove non-promising items.
 * 2. Determine processing order (ascending FUUB).
 * 3. Compute RU/RFU via reverse traversal with temporary table (CT10).
 * 4. Compute mSTFU for each list (CT11).
 */
public final class Restructurer {

    private final GlobalListStore store;
    private final UBTable ubTable;
    private final SlidingWindow window;
    private final EventPublisher publisher;

    public Restructurer(GlobalListStore store, UBTable ubTable,
                        SlidingWindow window, EventPublisher publisher) {
        this.store = store;
        this.ubTable = ubTable;
        this.window = window;
        this.publisher = publisher;
    }

    /**
     * Perform full restructure and return the snapshot + processing order.
     * @return RestructureResult with promising lists in processing order
     */
    public RestructureResult restructure(int windowNumber, double delta) {
        // Step 1: Compute sigmaTFU (denominator for TFUUBR)
        // STP = max start period of all items in the window
        Map<String, Integer> itemStartPeriods = window.startPeriodOfItems();
        int globalSTP = itemStartPeriods.values().stream()
            .mapToInt(Integer::intValue).max().orElse(1);
        double sigmaTfu = ubTable.sumTfuFromPeriod(globalSTP);

        // Step 2: Compute TFUUBR for each list; collect promising lists
        Map<FuzzyItem, Double> fuubMap = new LinkedHashMap<>();
        Map<FuzzyItem, Double> tfuubrMap = new LinkedHashMap<>();
        List<FuzzyItem> prunedItems = new ArrayList<>();
        List<TFUSList> promisingLists = new ArrayList<>();

        for (TFUSList list : store.all()) {
            double fuub = list.fuub();
            double tfuubr = (sigmaTfu > 0) ? fuub / sigmaTfu : 0.0;
            fuubMap.put(list.fuzzyItem(), fuub);
            tfuubrMap.put(list.fuzzyItem(), tfuubr);

            if (Thresholds.atLeast(tfuubr, delta)) {
                promisingLists.add(list);
            } else {
                prunedItems.add(list.fuzzyItem());
                // Zero out values for pruned items
                list.setRfu(0.0);
                list.setMstfu(0.0);
            }
        }

        // Step 3: Sort promising lists by FUUB ascending (ties broken by FuzzyItem order)
        promisingLists.sort(Comparator
            .comparingDouble(TFUSList::fuub)
            .thenComparing(TFUSList::fuzzyItem));

        List<FuzzyItem> processingOrder = promisingLists.stream()
            .map(TFUSList::fuzzyItem).collect(Collectors.toList());

        // Step 4: Compute RU/RFU by traversing entries in reverse processing order
        computeRemainingUtilities(promisingLists);

        // Step 5: Compute mSTFU for each promising list
        computeMaxStartTransactionFuzzyUtility(promisingLists, itemStartPeriods);

        // Build snapshot (Memento)
        ListsSnapshot snapshot = buildSnapshot(windowNumber, "post-restructure",
            promisingLists, fuubMap, tfuubrMap, sigmaTfu);

        // Publish event
        Map<FuzzyItem, Double> rfuMap = new LinkedHashMap<>();
        Map<FuzzyItem, Double> mstfuMap = new LinkedHashMap<>();
        for (TFUSList l : promisingLists) {
            rfuMap.put(l.fuzzyItem(), l.rfu());
            mstfuMap.put(l.fuzzyItem(), l.mstfu());
        }

        publisher.publish(new MiningEvent.RestructureCompleted(
            windowNumber, sigmaTfu, fuubMap, tfuubrMap,
            prunedItems, processingOrder, rfuMap, mstfuMap
        ));

        return new RestructureResult(promisingLists, processingOrder, sigmaTfu, snapshot);
    }

    /**
     * Compute RU and RFU for all promising lists.
     * Traverses entries in reverse processing order. Uses a temporary table
     * per transaction to accumulate utilities of items appearing after each position,
     * avoiding double-counting different linguistic terms of the same base item.
     *
     * CT10: RU(X, T) = sum of U(j, T) for items j after X in processing order
     *       (one contribution per base item, not per fuzzy term)
     * RFU(X) = sum over T of minFv(X,T) * RU(X,T)
     */
    private void computeRemainingUtilities(List<TFUSList> orderedLists) {
        // For each transaction: map of base-item -> utility already counted (as remaining for items before it)
        // We traverse from right to left: for position i, the RU is the sum of utilities of items at i+1..n
        // but only one utility per base item (avoid counting A.L + A.H as two different items)

        int n = orderedLists.size();

        // temp[tidIndex] -> {baseItem -> utility already summed from suffix}
        Map<Integer, Map<String, Double>> tempTable = new HashMap<>();

        for (int i = n - 1; i >= 0; i--) {
            TFUSList current = orderedLists.get(i);
            String baseItem = current.fuzzyItem().item();

            // For each entry in this list, set RU from tempTable
            for (Entry e : current.entries()) {
                Map<String, Double> rowTemp = tempTable.getOrDefault(e.tidIndex(), Collections.emptyMap());
                double ru = rowTemp.values().stream().mapToDouble(Double::doubleValue).sum();
                e.setRemainingUtility(ru);
            }

            // Now add this item's utility to tempTable for all transactions it appears in
            // (but only if this base item isn't already counted for this transaction)
            if (i > 0) { // only needed if there are items to the left
                for (Entry e : current.entries()) {
                    Map<String, Double> rowTemp = tempTable.computeIfAbsent(
                        e.tidIndex(), k -> new HashMap<>());
                    // Only count if this base item not already in temp (first term of this item from right)
                    rowTemp.merge(baseItem, e.utility(), (a, b) -> a); // keep first = rightmost
                }
            }

            // Recompute RFU
            current.recomputeRfu();
        }
    }

    /**
     * Compute mSTFU for each promising list.
     * mSTFU(X) = sum of TFU(T) for T with period >= maxStartPeriod(X and items after X in order)
     *
     * Traverses right-to-left; maintains a running max of start periods.
     * CT11: mSTFU(X) = sigma_TFU(PS(X ∪ {items after X in order}))
     */
    private void computeMaxStartTransactionFuzzyUtility(
        List<TFUSList> orderedLists,
        Map<String, Integer> itemStartPeriods)
    {
        int n = orderedLists.size();
        int runningMaxSP = 1; // will accumulate from right

        for (int i = n - 1; i >= 0; i--) {
            TFUSList list = orderedLists.get(i);
            String baseItem = list.fuzzyItem().item();
            int sp = itemStartPeriods.getOrDefault(baseItem, 1);
            if (sp > runningMaxSP) runningMaxSP = sp;

            // mSTFU = sum of TFU for transactions with period >= runningMaxSP
            double mstfu = ubTable.sumTfuFromPeriod(runningMaxSP);
            list.setMstfu(mstfu);
        }
    }

    private ListsSnapshot buildSnapshot(int windowNumber, String label,
                                        List<TFUSList> lists,
                                        Map<FuzzyItem, Double> fuubMap,
                                        Map<FuzzyItem, Double> tfuubrMap,
                                        double sigmaTfu) {
        List<ListsSnapshot.ListSnapshot> snapshots = new ArrayList<>();
        for (TFUSList l : lists) {
            List<ListsSnapshot.EntrySnapshot> entries = new ArrayList<>();
            for (Entry e : l.entries()) {
                entries.add(new ListsSnapshot.EntrySnapshot(
                    e.tid(), e.batchNumber(), e.periodNumber(),
                    e.utility(), e.minFv(), e.remainingUtility()
                ));
            }
            double tfuubr = tfuubrMap.getOrDefault(l.fuzzyItem(), 0.0);
            snapshots.add(new ListsSnapshot.ListSnapshot(
                l.fuzzyItem(), l.fu(), l.fuub(), l.rfu(), l.mstfu(),
                tfuubr, entries
            ));
        }
        return new ListsSnapshot(windowNumber, label, snapshots);
    }

    // -------------------------------------------------------------------------
    public record RestructureResult(
        List<TFUSList> orderedLists,
        List<FuzzyItem> processingOrder,
        double sigmaTfu,
        ListsSnapshot snapshot
    ) {}
}
