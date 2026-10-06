package tfus.algorithm;

import tfus.algorithm.pruning.*;
import tfus.algorithm.result.PatternEvaluation;
import tfus.algorithm.structure.*;
import tfus.domain.*;
import tfus.event.*;
import java.util.*;

/**
 * Recursive pattern extension implementing Algorithm 2 from the TFUS paper.
 *
 * Key insight: we pass the accumulated prefix items alongside the prefix list,
 * so each recursive call knows the full pattern context.
 *
 * For each list X in orderedLists:
 *   1. pattern = prefixItems + X.fuzzyItem()
 *   2. Compute rTFUUBR(X); if < delta AND pruning enabled → skip (prune branch)
 *   3. Compute tFUR(X); if >= delta → record as result
 *   4. For each Y after X (different base item): combine(X, Y, puTable) → conditionalList
 *   5. If enough conditional lists → recurse with prefix = X, prefixItems = pattern
 */
public final class PatternExtender {
    private final PruningStrategy pruning;
    private final double delta;
    private final UBTable ubTable;
    private final SlidingWindow window;
    private final MiningConfig.RecursionRule recursionRule;
    private final EventPublisher publisher;

    private final List<Pattern> results = new ArrayList<>();
    private final List<PatternEvaluation> evaluations = new ArrayList<>();

    public PatternExtender(PruningStrategy pruning, double delta,
                           UBTable ubTable, SlidingWindow window,
                           MiningConfig.RecursionRule recursionRule,
                           EventPublisher publisher) {
        this.pruning = pruning;
        this.delta = delta;
        this.ubTable = ubTable;
        this.window = window;
        this.recursionRule = recursionRule;
        this.publisher = publisher;
    }

    public List<Pattern> results() { return Collections.unmodifiableList(results); }
    public List<PatternEvaluation> evaluations() { return Collections.unmodifiableList(evaluations); }

    /**
     * Entry point: called from TFUSMiner.mine() with the top-level list ordering.
     */
    public void extend(List<TFUSList> orderedLists) {
        extendRecursive(orderedLists, Collections.emptyList(), null);
    }

    /**
     * Recursive extension.
     * @param candidates    lists to consider extending from
     * @param prefixItems   accumulated fuzzy items of the prefix pattern
     * @param prefixList    the TFUS-list of the last prefix item (for PUTable)
     */
    private void extendRecursive(List<TFUSList> candidates,
                                  List<FuzzyItem> prefixItems,
                                  TFUSList prefixList) {
        // Build PUTable for the prefix
        PUTable puTable = (prefixList == null) ? PUTable.empty() : PUTable.of(prefixList);

        for (int i = 0; i < candidates.size(); i++) {
            TFUSList X = candidates.get(i);

            // Build the full pattern for this candidate
            List<FuzzyItem> pattern = new ArrayList<>(prefixItems);
            pattern.add(X.fuzzyItem());

            double mstfu = X.mstfu();

            // CT13: rTFUUBR(X) = (FU(X) + RFU(X)) / mSTFU(X)
            double rTFUUBR = (mstfu > 0) ? (X.fu() + X.rfu()) / mstfu : 0.0;

            // Check pruning: if rTFUUBR < delta, skip this and all extensions
            boolean pruned = pruning.shouldPrune(X, delta, mstfu);

            // CT12: tFUR(X) = FU(X) / ΣTFU from PS(pattern)
            int startPeriod = computeStartPeriod(pattern);
            double sigmaTfu = ubTable.sumTfuFromPeriod(startPeriod);
            double tFUR = (sigmaTfu > 0) ? X.fu() / sigmaTfu : 0.0;
            boolean isResult = Thresholds.atLeast(tFUR, delta);

            // Record evaluation regardless of pruning decision
            PatternEvaluation eval = new PatternEvaluation(
                Collections.unmodifiableList(pattern),
                X.fu(), X.rfu(), mstfu, rTFUUBR, tFUR,
                startPeriod, pruned, isResult
            );
            evaluations.add(eval);

            // Publish event
            publisher.publish(new MiningEvent.PatternEvaluated(
                Collections.unmodifiableList(pattern),
                X.fu(), X.rfu(), mstfu, rTFUUBR, tFUR, pruned, isResult
            ));

            if (isResult) {
                results.add(new Pattern(Collections.unmodifiableList(pattern), tFUR, startPeriod));
            }

            // If pruned, skip all extensions of this branch
            if (pruned) continue;

            // Build conditional lists: combine X with each Y after position i
            List<TFUSList> conditionalLists = new ArrayList<>();

            for (int j = i + 1; j < candidates.size(); j++) {
                TFUSList Y = candidates.get(j);

                // Skip if Y's base item is already in the current pattern
                if (containsBaseItem(pattern, Y.fuzzyItem().item())) continue;

                TFUSList combined = combine(X, Y, puTable);
                if (!combined.isEmpty()) {
                    conditionalLists.add(combined);
                }
            }

            // Recurse based on recursion rule
            boolean shouldRecurse = switch (recursionRule) {
                case AT_LEAST_ONE -> !conditionalLists.isEmpty();
                case AT_LEAST_TWO -> conditionalLists.size() >= 2;
            };

            if (shouldRecurse) {
                extendRecursive(conditionalLists, pattern, X);
            }
        }
    }

    /**
     * Combine lists X and Y to produce a conditional list for pattern (prefix ∪ X ∪ Y).
     *
     * For each transaction appearing in both X and Y:
     *   combinedU = U(X,T) + U(Y,T) - prefixU(T)
     *   combinedFv = min(Fv(X,T), Fv(Y,T))
     *   combinedRU = min(RU(X,T), RU(Y,T))
     *
     * The resulting list has Y's fuzzyItem as label (will be accessed via pattern context).
     */
    private TFUSList combine(TFUSList X, TFUSList Y, PUTable puTable) {
        // We create a new list labeled with Y's fuzzyItem
        TFUSList result = new TFUSList(Y.fuzzyItem());

        List<Entry> xEntries = X.entries();
        List<Entry> yEntries = Y.entries();

        // Two-pointer intersection on tidIndex (entries are sorted by insertion order = tidIndex order)
        int xi = 0, yi = 0;
        while (xi < xEntries.size() && yi < yEntries.size()) {
            Entry ex = xEntries.get(xi);
            Entry ey = yEntries.get(yi);

            int cmp = Integer.compare(ex.tidIndex(), ey.tidIndex());
            if (cmp == 0) {
                double prefixU = puTable.get(ex.tidIndex());
                double combinedU = ex.utility() + ey.utility() - prefixU;
                double combinedFv = Math.min(ex.minFv(), ey.minFv());

                Entry combined = new Entry(
                    ex.tidIndex(), ex.tid(), ex.batchNumber(), ex.periodNumber(),
                    combinedU, combinedFv
                );
                // RU: min of RU from both entries (conservative)
                combined.setRemainingUtility(Math.min(ex.remainingUtility(), ey.remainingUtility()));
                result.addEntry(combined);
                xi++; yi++;
            } else if (cmp < 0) {
                xi++;
            } else {
                yi++;
            }
        }

        // mSTFU of conditional = min of X.mSTFU and Y.mSTFU
        result.setMstfu(Math.min(X.mstfu(), Y.mstfu()));

        // Recompute RFU
        result.recomputeRfu();

        return result;
    }

    /** Compute start period PS for a pattern = max startPeriod of base items */
    private int computeStartPeriod(List<FuzzyItem> pattern) {
        Set<String> baseItems = new HashSet<>();
        for (FuzzyItem fi : pattern) baseItems.add(fi.item());
        return window.startPeriodOf(baseItems);
    }

    private boolean containsBaseItem(List<FuzzyItem> pattern, String baseItem) {
        for (FuzzyItem fi : pattern) {
            if (fi.item().equals(baseItem)) return true;
        }
        return false;
    }
}
