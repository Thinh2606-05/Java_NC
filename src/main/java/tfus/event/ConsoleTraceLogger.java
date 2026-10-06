package tfus.event;

/**
 * Logs mining events to the console for debugging.
 */
public final class ConsoleTraceLogger implements MiningListener {

    @Override
    public void onEvent(MiningEvent event) {
        switch (event) {
            case MiningEvent.BatchArrived e ->
                System.out.printf("[BATCH] Batch B%d arrived (window #%d)%n",
                    e.batchNumber(), e.windowNumber());

            case MiningEvent.TransactionScanned e ->
                System.out.printf("[SCAN] %s: TFU=%.4f, MTFU=%.4f%n",
                    e.tid(), e.tfu(), e.mtfu());

            case MiningEvent.BatchRemoved e ->
                System.out.printf("[REMOVE] Batch B%d removed (%d entries)%n",
                    e.batchNumber(), e.removedEntries().size());

            case MiningEvent.RestructureCompleted e ->
                System.out.printf("[RESTRUCTURE] W%d: order=%s%n",
                    e.windowNumber(), e.processingOrder());

            case MiningEvent.PatternEvaluated e ->
                System.out.printf("[PATTERN] %s tFUR=%.4f rTFUUBR=%.4f pruned=%b result=%b%n",
                    e.pattern(), e.tFUR(), e.rTFUUBR(), e.pruned(), e.isResult());

            case MiningEvent.WindowMined e ->
                System.out.printf("[MINED] W%d: %d results%n",
                    e.windowNumber(), e.results().size());
        }
    }
}
