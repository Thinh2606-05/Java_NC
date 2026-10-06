package tfus.event;

import tfus.domain.FuzzyItem;
import tfus.domain.Pattern;
import java.util.*;

/**
 * Sealed hierarchy of mining events for the Observer pattern.
 */
public sealed interface MiningEvent
    permits MiningEvent.BatchArrived,
            MiningEvent.TransactionScanned,
            MiningEvent.BatchRemoved,
            MiningEvent.RestructureCompleted,
            MiningEvent.PatternEvaluated,
            MiningEvent.WindowMined {

    record BatchArrived(int batchNumber, int windowNumber) implements MiningEvent {}

    record TransactionScanned(
        String tid, int tidIndex, int batchNumber, int periodNumber,
        double tfu, double mtfu,
        List<ItemScannedDetail> details
    ) implements MiningEvent {
        public record ItemScannedDetail(
            String item, int quantity,
            double utility,   // U(i,T) = qty * EU
            Map<String, Double> fvMap,  // term -> Fv
            Map<String, Double> fuMap   // term -> FU contribution (Fv*U)
        ) {}
    }

    record BatchRemoved(
        int batchNumber,
        List<RemovedEntry> removedEntries
    ) implements MiningEvent {
        public record RemovedEntry(FuzzyItem fuzzyItem, String tid, double fuzzyUtility) {}
    }

    record RestructureCompleted(
        int windowNumber,
        double sigmaTfu,
        Map<FuzzyItem, Double> fuubMap,
        Map<FuzzyItem, Double> tfuubrMap,
        List<FuzzyItem> prunedItems,
        List<FuzzyItem> processingOrder,
        Map<FuzzyItem, Double> rfuMap,
        Map<FuzzyItem, Double> mstfuMap
    ) implements MiningEvent {}

    record PatternEvaluated(
        List<FuzzyItem> pattern,
        double fu, double rfu, double mstfu,
        double rTFUUBR, double tFUR,
        boolean pruned, boolean isResult
    ) implements MiningEvent {}

    record WindowMined(
        int windowNumber,
        List<Pattern> results,
        int totalEvaluated
    ) implements MiningEvent {}
}
