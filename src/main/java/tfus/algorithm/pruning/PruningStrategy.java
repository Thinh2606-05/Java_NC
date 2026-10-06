package tfus.algorithm.pruning;

import tfus.algorithm.structure.TFUSList;

/**
 * Strategy interface for pruning decisions during pattern extension.
 * Implements the Strategy design pattern.
 */
public interface PruningStrategy {
    /**
     * Decide whether to prune (skip) this list during extension.
     * @param list    the TFUS-list being evaluated
     * @param delta   the minimum utility threshold
     * @param mstfu   the effective mSTFU denominator for this list
     * @return true if the list should be pruned (rTFUUBR < delta)
     */
    boolean shouldPrune(TFUSList list, double delta, double mstfu);

    /** Human-readable name */
    String name();
}
