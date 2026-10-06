package tfus.algorithm;

import tfus.algorithm.result.WindowResult;
import tfus.domain.Batch;
import tfus.domain.Dataset;
import tfus.event.EventPublisher;
import tfus.event.MiningEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method: defines the skeleton of the sliding-window mining lifecycle.
 * Subclasses override hook methods without changing the overall flow.
 *
 * processBatch() sequence:
 *   1. Announce batch arrival (BatchArrived event)
 *   2. If window is full: remove oldest batch (removeBatch hook)
 *   3. Insert new batch (insertBatch hook)
 *   4. If window is full: restructure + mine (restructure + mine hooks)
 */
public abstract class AbstractSlidingWindowMiner {
    protected final EventPublisher publisher;
    protected int windowCounter = 0;

    protected AbstractSlidingWindowMiner(EventPublisher publisher) {
        this.publisher = publisher;
    }

    /**
     * Process the entire stream of batches.
     * @return results for each window that was mined
     */
    public final List<WindowResult> processStream(Dataset dataset) {
        List<WindowResult> allResults = new ArrayList<>();
        for (Batch batch : dataset.batches()) {
            WindowResult r = processBatch(batch);
            if (r != null) allResults.add(r);
        }
        return allResults;
    }

    /**
     * Template method: process one batch.
     * @return WindowResult if a complete window was mined; null otherwise
     */
    protected final WindowResult processBatch(Batch batch) {
        // Step 1: announce arrival
        publisher.publish(new MiningEvent.BatchArrived(batch.batchNumber(), windowCounter + 1));

        // Step 2: if window is full, remove oldest batch first
        if (isWindowFull()) {
            removeBatch(oldestBatch());
        }

        // Step 3: insert new batch into lists
        insertBatch(batch);

        // Step 4: check if window is now full → restructure + mine
        if (isWindowFull()) {
            windowCounter++;
            long start = System.currentTimeMillis();
            restructure(windowCounter);
            WindowResult result = mine(windowCounter);
            publisher.publish(new MiningEvent.WindowMined(
                windowCounter, result.patterns(), result.allEvaluations().size()
            ));
            return result;
        }

        return null; // window not yet full
    }

    // -------------------------------------------------------------------------
    // Hooks for subclasses

    /** @return true if the sliding window is at capacity */
    protected abstract boolean isWindowFull();

    /** @return the oldest batch currently in the window */
    protected abstract Batch oldestBatch();

    /** Remove the given batch from all data structures */
    protected abstract void removeBatch(Batch batch);

    /** Insert a new batch into all data structures */
    protected abstract void insertBatch(Batch batch);

    /** Perform restructuring for the given window number */
    protected abstract void restructure(int windowNumber);

    /** Perform mining and return window results */
    protected abstract WindowResult mine(int windowNumber);

    /** Dataset name (for reporting) */
    protected abstract String datasetName();
}
