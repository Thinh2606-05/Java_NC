package tfus.algorithm;

import tfus.algorithm.pruning.*;
import tfus.algorithm.result.*;
import tfus.algorithm.structure.*;
import tfus.domain.*;
import tfus.event.*;
import tfus.fuzzy.MembershipFunction;
import tfus.snapshot.ListsSnapshot;
import java.util.*;

/**
 * Concrete implementation of the TFUS sliding-window miner.
 * Implements all Template Method hooks from AbstractSlidingWindowMiner.
 *
 * Responsibilities:
 *   - insertBatch: scan transactions, compute U/Fv/FU/FUUB for each fuzzy item
 *   - removeBatch: decrement FU and FUUB when a batch expires
 *   - restructure: delegate to Restructurer
 *   - mine: delegate to PatternExtender
 */
public final class TFUSMiner extends AbstractSlidingWindowMiner {

    private final Dataset dataset;
    private final MiningConfig config;
    private final MembershipFunction membership;
    private final PruningStrategy pruningStrategy;

    // Core data structures
    private final GlobalListStore globalStore = new GlobalListStore();
    private final UBTable ubTable = new UBTable();
    private final SlidingWindow slidingWindow;

    // State between phases
    private Restructurer.RestructureResult lastRestructure;
    private int nextTidIndex = 0;

    public TFUSMiner(Dataset dataset, MiningConfig config,
                     MembershipFunction membership,
                     PruningStrategy pruningStrategy,
                     EventPublisher publisher) {
        super(publisher);
        this.dataset = dataset;
        this.config = config;
        this.membership = membership;
        this.pruningStrategy = pruningStrategy;
        this.slidingWindow = new SlidingWindow(config.windowSize());
    }

    @Override
    protected String datasetName() { return dataset.name(); }

    @Override
    protected boolean isWindowFull() {
        return slidingWindow.isFull();
    }

    @Override
    protected Batch oldestBatch() {
        return slidingWindow.batches().peekFirst();
    }

    // -------------------------------------------------------------------------
    // Template Method hooks
    // -------------------------------------------------------------------------

    @Override
    protected void removeBatch(Batch batch) {
        int batchNumber = batch.batchNumber();
        List<MiningEvent.BatchRemoved.RemovedEntry> removedEntries = new ArrayList<>();

        // Remove entries from all global lists
        for (TFUSList list : globalStore.all()) {
            List<Entry> removed = list.removeBatch(batchNumber, ubTable);
            for (Entry e : removed) {
                removedEntries.add(new MiningEvent.BatchRemoved.RemovedEntry(
                    list.fuzzyItem(), e.tid(), e.fuzzyUtility()
                ));
            }
        }

        // Remove from UBTable
        ubTable.removeBatch(batchNumber);

        // Remove empty lists
        globalStore.removeEmptyLists();

        // Remove from sliding window
        slidingWindow.removeOldest();

        publisher.publish(new MiningEvent.BatchRemoved(batchNumber, removedEntries));
    }

    @Override
    protected void insertBatch(Batch batch) {
        slidingWindow.addBatch(batch);
        for (Transaction txn : batch.transactions()) {
            scanTransaction(txn);
        }
    }

    /**
     * Scan a single transaction:
     * - Compute U(i,T) = qty * EU(i) for each item                [CT1]
     * - Compute Fv via membership function                          [CT2]
     * - For each fuzzy item, create/update TFUSList entry           [CT3-CT5]
     * - Compute TFU (sum of Fv*U) and MTFU (sum of max-Fv*U)       [CT6-CT7]
     * - Update FUUB of each list by MTFU of this transaction        [CT8]
     */
    private void scanTransaction(Transaction txn) {
        int tidIndex = nextTidIndex++;
        List<MiningEvent.TransactionScanned.ItemScannedDetail> details = new ArrayList<>();

        double tfu = 0.0;
        double mtfu = 0.0;

        // Track which lists were touched (for FUUB update)
        Set<TFUSList> touchedLists = new LinkedHashSet<>();

        // Sort items by name for determinism
        List<ItemQuantity> sortedItems = new ArrayList<>(txn.items());
        sortedItems.sort(Comparator.comparing(ItemQuantity::item));

        for (ItemQuantity iq : sortedItems) {
            String item = iq.item();
            int qty = iq.quantity();
            int maxQty = dataset.m(item);
            double eu = dataset.eu(item);

            // CT1: utility U(i,T) = quantity * EU
            double utility = qty * eu;

            // CT2: fuzzy membership values
            Map<String, Double> fvMap = membership.fuzzify(qty, maxQty);

            // CT6-CT7: TFU contribution and MTFU contribution for this item
            double maxFv = 0.0;
            Map<String, Double> fuMap = new LinkedHashMap<>();
            for (Map.Entry<String, Double> fvEntry : fvMap.entrySet()) {
                String term = fvEntry.getKey();
                double fv = fvEntry.getValue();

                // CT3: fuzzy utility FU contribution = Fv * U
                double fu = fv * utility;
                fuMap.put(term, fu);

                // CT6: add to TFU
                tfu += fu;

                // Track for max-fv MTFU
                if (fv > maxFv) maxFv = fv;

                // CT5: create entry and add to list
                FuzzyItem fi = new FuzzyItem(item, term);
                TFUSList list = globalStore.getOrCreate(fi);
                Entry entry = new Entry(tidIndex, txn.tid(),
                    txn.batchNumber(), txn.periodNumber(),
                    utility, fv);
                list.addEntry(entry);
                touchedLists.add(list);
            }

            // CT7: MTFU += max(Fv) * U for this item
            mtfu += maxFv * utility;

            details.add(new MiningEvent.TransactionScanned.ItemScannedDetail(
                item, qty, utility, fvMap, fuMap
            ));
        }

        // Store TFU and MTFU in UBTable
        ubTable.put(tidIndex, txn.tid(), txn.batchNumber(), txn.periodNumber(), tfu, mtfu);

        // CT8: update FUUB of all touched lists by this transaction's MTFU
        for (TFUSList list : touchedLists) {
            list.addToFuub(mtfu);
        }

        publisher.publish(new MiningEvent.TransactionScanned(
            txn.tid(), tidIndex, txn.batchNumber(), txn.periodNumber(),
            tfu, mtfu, details
        ));
    }

    @Override
    protected void restructure(int windowNumber) {
        Restructurer restructurer = new Restructurer(
            globalStore, ubTable, slidingWindow, publisher
        );
        lastRestructure = restructurer.restructure(windowNumber, config.delta());
    }

    @Override
    protected WindowResult mine(int windowNumber) {
        long start = System.currentTimeMillis();

        PatternExtender extender = new PatternExtender(
            pruningStrategy, config.delta(),
            ubTable, slidingWindow,
            config.recursionRule(),
            publisher
        );

        extender.extend(lastRestructure.orderedLists());

        long elapsed = System.currentTimeMillis() - start;
        return new WindowResult(
            windowNumber,
            extender.results(),
            extender.evaluations(),
            lastRestructure.snapshot(),
            elapsed
        );
    }
}
