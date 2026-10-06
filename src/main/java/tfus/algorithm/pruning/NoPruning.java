package tfus.algorithm.pruning;

import tfus.algorithm.structure.TFUSList;

/**
 * Null Object: never prunes. Used for "full" mode to generate all patterns.
 */
public final class NoPruning implements PruningStrategy {

    @Override
    public String name() { return "NONE"; }

    @Override
    public boolean shouldPrune(TFUSList list, double delta, double mstfu) {
        return false;
    }
}
