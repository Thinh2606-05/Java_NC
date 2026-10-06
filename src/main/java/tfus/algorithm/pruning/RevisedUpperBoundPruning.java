package tfus.algorithm.pruning;

import tfus.algorithm.structure.TFUSList;

/**
 * Revised Upper Bound Pruning: prune if rTFUUBR < delta.
 * rTFUUBR = (FU + RFU) / mSTFU  [CT13]
 */
public final class RevisedUpperBoundPruning implements PruningStrategy {

    @Override
    public String name() { return "REVISED_UPPER_BOUND"; }

    @Override
    public boolean shouldPrune(TFUSList list, double delta, double mstfu) {
        if (mstfu <= 0) return true; // No valid denominator → prune
        double rTFUUBR = (list.fu() + list.rfu()) / mstfu;
        return Thresholds.lessThan(rTFUUBR, delta);
    }
}
